package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.point.asGCJ02
import page.ooooo.geoshare.lib.point.buildPoints

object AmapInput : Input.HasShortUri {
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
    override val shortUriMethod = Input.ShortUriMethod.HEAD

    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        points = buildPoints {
            uri.run {
                Regex("""\w+,$LAT,$LON.+""").matchEntire(queryParams["p"])?.toLatLonPoint()?.also { points.add(it) }
                    ?: Regex("""$LAT,$LON.+""").matchEntire(queryParams["q"])?.toLatLonPoint()?.also { points.add(it) }
            }
        }.asGCJ02()
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_amap_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_amap_loading_indicator_title
}
