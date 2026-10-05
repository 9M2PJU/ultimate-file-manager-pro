package za.kilowatch.ultimatefilemanager.storage.recents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RecentLocalFileTest {

    @Test
    fun cachedMetadataReturnedWithoutDiskIo() {
        val path = "/nonexistent/test/path/sample_doc.pdf"
        val expectedLength = 1048576L
        val expectedModified = 1728123456000L

        val recentFile = RecentLocalFile(
            pathname = path,
            isDir = false,
            fileLength = expectedLength,
            fileLastModified = expectedModified
        )

        // Metadata getters should return cached values
        assertFalse("isFile should be true and isDirectory false", recentFile.isDirectory)
        assertTrue("isFile should be true", recentFile.isFile)
        assertEquals("length should match cached size", expectedLength, recentFile.length())
        assertEquals("lastModified should match cached mtime", expectedModified, recentFile.lastModified())
        assertTrue("exists should return true for valid recents item", recentFile.exists())
        assertTrue("canRead should return true", recentFile.canRead())
        assertTrue("canWrite should return true", recentFile.canWrite())

        // File path properties
        assertEquals("sample_doc.pdf", recentFile.name)
        assertEquals("pdf", recentFile.extension)

        // Equality and path resolution with standard File having same path
        val standardFile = File(path)
        assertEquals(standardFile.absolutePath, recentFile.absolutePath)
        assertEquals(standardFile.parent, recentFile.parent)
        assertEquals(standardFile, recentFile)
        assertEquals(standardFile.hashCode(), recentFile.hashCode())
    }
}
