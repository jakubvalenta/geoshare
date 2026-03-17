package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonNamePoint
import page.ooooo.geoshare.lib.point.Point

object AmapInput : ShortUriInput, Input.HasRandomUri {
    override val uriPattern = Regex("""(?:https?://)?(?:surl|wb)\.amap\.com/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.AMAP,
        nameResId = R.string.converter_amap_name,
        items = listOf(
            InputDocumentationItem.Url(27, "https://surl.amap.com/"),
            InputDocumentationItem.Url(27, "https://wb.amap.com/"),
        ),
    )

    override val shortUriPattern = Regex("""(?:https?://)?surl\.amap\.com/\S+""")
    override val shortUriMethod = ShortUriInput.Method.HEAD

    override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = buildParseUriResult {
        uri.run {
            // Query param p
            // https://wb.amap.com/?p=<id>,<lat>,<lon>,<name>
            Regex("""\w+,$LAT,$LON,?(?:$NAME_PARAM)?.*""").matchEntire(queryParams["p"])?.toLatLonNamePoint()?.let {
                points = persistentListOf(it.asGCJ02())
                return@run
            }

            // Query param q
            // https://wb.amap.com/?q=<lat>,<lon>,<name>
            Regex("""$LAT,$LON,?(?:$NAME_PARAM)?.*""").matchEntire(queryParams["q"])?.toLatLonNamePoint()?.let {
                points = persistentListOf(it.asGCJ02())
                return@run
            }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_amap_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_amap_loading_indicator_title

    override fun genRandomUri(point: Point) =
        point.formatUriString("https://wb.amap.com/?q={lat}%2C{lon}")
}
