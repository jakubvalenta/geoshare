package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import io.ktor.utils.io.*
import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.point.Point

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
            pointsFromUri: ImmutableList<Point>,
            log: ILog = DefaultLog,
        ): ParseHtmlResult?
    }
}
