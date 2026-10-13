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
import java.util.concurrent.atomic.AtomicLong

object SmbParallelUpload {
    private const val TAG = "SmbParallelUpload"
    const val PARALLEL_MIN_SIZE = 4 * 1024 * 1024L // 4 MB threshold
    private const val CHUNK_SIZE = 1024 * 1024     // 1 MB chunk

    suspend fun uploadFileInParallel(
        src: File,
        destShare: NetworkShare,
        destPath: String,
        threads: Int,
        onProgress: ((bytesCopied: Long, totalBytes: Long) -> Unit)? = null,
        onConnectionReady: ((AutoCloseable) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val totalSize = src.length()
        if (threads <= 1 || totalSize < PARALLEL_MIN_SIZE) {
            return@withContext false
        }

        val actualThreads = threads.coerceIn(2, 8)
        val (shareName, innerPath) = splitSharePath(destShare.remotePath, destPath)

        // Step 1: Pre-create / truncate destination file on the SMB share with mode "w"
        val initHandleId = try {
            Smbclient.smbOpenFile(
                destShare.host, destShare.effectivePort.toLong(), destShare.username, destShare.password, destShare.domain,
                shareName, innerPath, "w", true
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize destination file on SMB: ${e.message}")
            return@withContext false
        }
        // Close init handle so parallel workers can open it with "rw"
        try {
            Smbclient.smbCloseFile(initHandleId)
        } catch (_: Exception) {}

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
                                destShare.host, destShare.effectivePort.toLong(), destShare.username, destShare.password, destShare.domain,
                                shareName, innerPath, "rw", true
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Worker $index failed to open SMB handle for parallel write: ${e.message}")
                            throw e
                        }

                        synchronized(handlesLock) {
                            activeHandles.add(handleId)
                        }

                        try {
                            RandomAccessFile(src, "r").use { raf ->
                                raf.seek(startOffset)
                                val buffer = ByteArray(CHUNK_SIZE)
                                var segmentRemaining = expectedBytes
                                var currentWriteOffset = startOffset

                                while (segmentRemaining > 0) {
                                    ensureActive()
                                    val toRead = minOf(buffer.size.toLong(), segmentRemaining).toInt()
                                    val read = raf.read(buffer, 0, toRead)
                                    if (read < 0) break

                                    val payload = if (read == buffer.size) buffer else buffer.copyOfRange(0, read)
                                    val written = Smbclient.smbWriteAt(handleId, currentWriteOffset, payload)
                                    if (written < 0) {
                                        throw java.io.IOException("smbWriteAt failed at offset $currentWriteOffset")
                                    }

                                    currentWriteOffset += written
                                    segmentRemaining -= written
                                    val currentTotal = totalBytesCopied.addAndGet(written)
                                    onProgress?.invoke(currentTotal, totalSize)
                                }

                                if (segmentRemaining > 0) {
                                    throw java.io.IOException("Incomplete write for segment $index: remaining $segmentRemaining bytes")
                                }
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
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Parallel SMB upload encountered error: ${e.message}", e)
            throw e
        } finally {
            closeable.close()
        }
    }
}
