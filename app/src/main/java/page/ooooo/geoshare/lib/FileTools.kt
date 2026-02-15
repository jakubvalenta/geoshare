package page.ooooo.geoshare.lib

import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Locale

fun writeFile(parentDir: File, childDir: String, filename: String, block: Appendable.() -> Unit): File? {
    val dir = File(parentDir, childDir)
    dir.deleteRecursively()
    try {
        dir.mkdirs()
    } catch (_: SecurityException) {
        return null
    }
    val file = File(dir, filename)
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
    SimpleDateFormat(@Suppress("SpellCheckingInspection") "yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
        .format(System.currentTimeMillis())
