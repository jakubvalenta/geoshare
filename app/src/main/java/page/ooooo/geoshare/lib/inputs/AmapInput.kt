package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.matchNaivePoint
import page.ooooo.geoshare.lib.point.asGCJ02
import page.ooooo.geoshare.lib.point.buildPoints
import page.ooooo.geoshare.lib.point.toParseUriResult

object AmapInput : Input.HasShortUri {
    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(surl|wb)\.amap\.com/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.AMAP,
        nameResId = R.string.converter_amap_name,
        items = listOf(
            InputDocumentationItem.Url(27, "https://surl.amap.com/"),
            InputDocumentationItem.Url(27, "https://wb.amap.com/"),
        ),
    )

    @Suppress("SpellCheckingInspection")
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?surl\.amap\.com/\S+""")
    override val shortUriMethod = Input.ShortUriMethod.HEAD

    override suspend fun parseUri(uri: Uri): ParseUriResult? =
        buildPoints {
            uri.run {
                setPointIfNull { """\w+,$LAT,$LON.+""" matchNaivePoint queryParams["p"] }
                setPointIfNull { """$LAT,$LON.+""" matchNaivePoint queryParams["q"] }
            }
        }
            .asGCJ02()
            .toParseUriResult()

    @StringRes
    override val permissionTitleResId = R.string.converter_amap_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_amap_loading_indicator_title
}
