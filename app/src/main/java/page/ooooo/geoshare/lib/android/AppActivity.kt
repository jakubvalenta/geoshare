package page.ooooo.geoshare.lib.android

import android.content.Context
import page.ooooo.geoshare.lib.Log
import java.io.File

enum class FileType {
    GPX,
    GPX_ONE_POINT,
}

enum class UriScheme {
    CARTES_IGN,
    GEO,
    GOOGLE_NAVIGATION,
    GOOGLE_STREET_VIEW,
    MAGIC_EARTH,
    UNKNOWN,
}

enum class MimeType(val value: String) {
    TEXT_PLAIN("text/plain")
}

sealed interface AppActivity {
    val packageName: String
}

data class FileActivity(override val packageName: String, val fileType: FileType) : AppActivity {
    fun launch(context: Context, file: File, log: Log): Boolean =
        context.openFileInApp(file, packageName, log)
}

data class TextActivity(override val packageName: String, val mimeType: MimeType) : AppActivity {
    fun launch(context: Context, text: String): Boolean =
        context.sendTextViaApp(text, packageName, mimeType)
}

data class UriActivity(override val packageName: String, val uriScheme: UriScheme) : AppActivity {
    fun launch(context: Context, uriString: String): Boolean =
        context.openUriInApp(uriString, packageName)
}

fun AppActivity.isMessagingApp(): Boolean =
    this is TextActivity

fun Iterable<AppActivity>.getPackageNames(): Set<String> =
    map { it.packageName }.toSet()

fun Iterable<AppActivity>.sorted(): List<AppActivity> =
    sortedWith(
        compareBy<AppActivity> { activity ->
            when (activity) {
                is FileActivity -> 2
                is TextActivity -> 1
                is UriActivity -> 0
            }
        }
            .thenBy { activity ->
                when (activity) {
                    is FileActivity -> when (activity.fileType) {
                        FileType.GPX -> 0
                        FileType.GPX_ONE_POINT -> 1
                    }

                    is TextActivity -> null

                    is UriActivity -> when (activity.uriScheme) {
                        UriScheme.CARTES_IGN -> 1
                        UriScheme.GEO -> 0
                        UriScheme.GOOGLE_NAVIGATION -> 3
                        UriScheme.GOOGLE_STREET_VIEW -> 4
                        UriScheme.MAGIC_EARTH -> 2
                        UriScheme.UNKNOWN -> null
                    }
                }
            }
    )
