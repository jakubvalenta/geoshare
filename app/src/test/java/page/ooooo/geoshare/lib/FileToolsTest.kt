package page.ooooo.geoshare.lib

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.createTempDirectory

class FileToolsTest {
    @Test
    fun writeFile_whenParentDirIsNotWritable_returnsNull() {
        val parentDir = createTempDirectory(
            null,
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("r--------")),
        ).toFile()
        val res = writeFile(parentDir, "my_dir", "new_file.txt") {
            append("new_content")
        }
        assertNull(res)
    }

    @Test
    fun writeFile_whenParentDirIsWritable_deletesExistingFilesAndWritesNewFile() {
        val parentDir = createTempDirectory().toFile()

        // Write old files
        val childDir = File(parentDir, "my_dir")
        childDir.mkdirs()
        val oldFile = File(childDir, "old_file.txt")
        oldFile.writeText("old_content")

        // Assert old file was written
        assertEquals(
            listOf("my_dir/old_file.txt"),
            childDir.listFiles()?.map { it.relativeTo(parentDir).path },
        )

        val res = writeFile(parentDir, "my_dir", "new_file.txt") {
            append("new_content")
        }

        // Assert return value
        assertEquals("new_content", res?.readText())

        // Assert old file was deleted and new file was written
        assertEquals(
            listOf("my_dir/new_file.txt"),
            childDir.listFiles()?.map { it.relativeTo(parentDir).path },
        )
        assertEquals(
            "new_content",
            File(childDir, "new_file.txt").readText(),
        )
    }
}
