package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import io.ktor.utils.io.*
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.position.Position

interface Input {

    enum class ShortUriMethod { GET, HEAD }

    val uriPattern: Pattern
    val documentation: InputDocumentation

    suspend fun parseUri(uri: Uri): ParseUriResult?

    interface HasShortUri : Input {
        val shortUriPattern: Pattern
        val shortUriMethod: ShortUriMethod
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }

    interface HasHtml : Input {
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int

        suspend fun parseHtml(
            channel: ByteReadChannel,
            positionFromUri: Position,
            log: ILog = DefaultLog,
        ): ParseHtmlResult?
    }
}
