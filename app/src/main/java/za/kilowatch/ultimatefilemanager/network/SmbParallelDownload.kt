package za.kilowatch.ultimatefilemanager.network

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import smbclient.Smbclient
import za.kilowatch.ultimatefilemanager.network.SmbShareClient.splitSharePath
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong

object SmbParallelDownload {
    private const val TAG = "SmbParallelDownload"
    const val PARALLEL_MIN_SIZE = 4 * 1024 * 1024L // 4 MB threshold
    private const val CHUNK_SIZE = 1024 * 1024     // 1 MB chunk

    suspend fun downloadFileParallel(
        share: NetworkShare,
        remotePath: String,
        destFile: File,
        totalSize: Long,
        threads: Int,
        onProgress: ((bytesCopied: Long, totalBytes: Long) -> Unit)? = null,
        onConnectionReady: ((AutoCloseable) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        if (threads <= 1 || totalSize < PARALLEL_MIN_SIZE) {
            return@withContext false
        }

        val actualThreads = threads.coerceIn(2, 8)
        val (shareName, innerPath) = splitSharePath(share.remotePath, remotePath)

        val raf = RandomAccessFile(destFile, "rw")
        val channel = raf.channel
        raf.setLength(totalSize)

        val segmentSize = (totalSize + actualThreads - 1) / actualThreads
        val totalBytesCopied = AtomicLong(0L)
        val activeHandles = mutableListOf<Long>()
        val handlesLock = Any()

        val closeable = AutoCloseable {
            synchronized(handlesLock) {
                for (h in activeHandles) {
                    runCatching { Smbclient.smbCloseFile(h) }
                }
                activeHandles.clear()
            }
        }
        onConnectionReady?.invoke(closeable)

        try {
            coroutineScope {
                val jobs = (0 until actualThreads).map { index ->
                    val startOffset = index * segmentSize
                    if (startOffset >= totalSize) return@map null
                    val endOffset = minOf((index + 1) * segmentSize - 1, totalSize - 1)
                    val expectedBytes = endOffset - startOffset + 1

                    async(Dispatchers.IO) {
                        val handleId = try {
                            Smbclient.smbOpenFile(
                                share.host, share.effectivePort.toLong(), share.username, share.password, share.domain,
                                shareName, innerPath, "r", true
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Worker $index failed to open SMB handle for parallel read: ${e.message}")
                            throw e
                        }

                        synchronized(handlesLock) {
                            activeHandles.add(handleId)
                        }

                        try {
                            var segmentRemaining = expectedBytes
                            var currentOffset = startOffset

                            while (segmentRemaining > 0) {
                                ensureActive()
                                val toRead = minOf(CHUNK_SIZE.toLong(), segmentRemaining).toInt()
                                val bytes = Smbclient.smbReadAt(handleId, currentOffset, toRead.toLong())
                                if (bytes == null || bytes.isEmpty()) break

                                val byteBuffer = ByteBuffer.wrap(bytes)
                                var writtenTotal = 0
                                while (byteBuffer.hasRemaining()) {
                                    writtenTotal += channel.write(byteBuffer, currentOffset + writtenTotal)
                                }

                                currentOffset += bytes.size
                                segmentRemaining -= bytes.size
                                val currentTotal = totalBytesCopied.addAndGet(bytes.size.toLong())
                                onProgress?.invoke(currentTotal, totalSize)
                            }

                            if (segmentRemaining > 0) {
                                throw java.io.IOException("Incomplete SMB read for segment $index: remaining $segmentRemaining bytes")
                            }
                        } finally {
                            synchronized(handlesLock) {
                                activeHandles.remove(handleId)
                            }
                            runCatching { Smbclient.smbCloseFile(handleId) }
                        }
                    }
                }.filterNotNull()

                jobs.awaitAll()
            }
            channel.force(true)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Parallel SMB download failed: ${e.message}", e)
            false
        } finally {
            closeable.close()
            runCatching { channel.close() }
            runCatching { raf.close() }
        }
    }
}
