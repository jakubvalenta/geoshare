package page.ooooo.geoshare.lib

import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Locale

fun File.deleteAllAndWriteFile(filename: String, block: Appendable.() -> Unit): File? {
    deleteRecursively()
    try {
        mkdirs()
    } catch (_: SecurityException) {
        return null
    }
    val file = File(this, filename)
    try {
        file.printWriter().use { writer ->
            writer.block()
        }
    } catch (_: FileNotFoundException) {
        return null
    }
    return file
}

fun getTimestamp(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
        .format(System.currentTimeMillis())
