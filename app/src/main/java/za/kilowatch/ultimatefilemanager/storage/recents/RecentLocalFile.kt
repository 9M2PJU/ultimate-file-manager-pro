package za.kilowatch.ultimatefilemanager.storage.recents

import java.io.File

/**
 * A [File] implementation for local filesystem entries in the Recent Files view that pre-populates
 * file metadata ([isDirectory], [isFile], [length], [lastModified]) from cached database values.
 * This completely avoids blocking disk I/O / stat calls on the main thread during RecyclerView list submission.
 */
class RecentLocalFile(
    pathname: String,
    private val isDir: Boolean = false,
    private val fileLength: Long = 0L,
    private val fileLastModified: Long = 0L
) : File(pathname) {

    override fun isDirectory(): Boolean = isDir

    override fun isFile(): Boolean = !isDir

    override fun length(): Long = fileLength

    override fun lastModified(): Long = fileLastModified

    override fun exists(): Boolean = true

    override fun canRead(): Boolean = true

    override fun canWrite(): Boolean = true
}
