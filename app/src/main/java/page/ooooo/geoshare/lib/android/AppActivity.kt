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
}

sealed interface AppActivity {
    val packageName: String
}

data class FileActivity(override val packageName: String, val fileType: FileType) : AppActivity {
    fun launch(context: Context, file: File, log: Log): Boolean =
        context.openFileInApp(file, packageName, log)
}

data class TextActivity(override val packageName: String, val mimeType: String) : AppActivity {
    fun launch(context: Context, text: String): Boolean =
        context.sendTextViaApp(text, packageName, mimeType = mimeType)
}

data class UriActivity(override val packageName: String, val uriScheme: UriScheme) : AppActivity {
    fun launch(context: Context, uriString: String): Boolean =
        context.openUriInApp(uriString, packageName)
}
