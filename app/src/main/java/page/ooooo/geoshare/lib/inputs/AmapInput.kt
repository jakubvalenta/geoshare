package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.matchLatLonZ
import page.ooooo.geoshare.lib.position.PositionBuilder
import page.ooooo.geoshare.lib.position.Srs

object AmapInput : Input.HasShortUri {
    private val srs = Srs.GCJ02

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(surl|wb)\.amap\.com/\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_amap_name,
        inputs = listOf(
            Input.DocumentationInput.Url(27, "https://surl.amap.com/"),
            Input.DocumentationInput.Url(27, "https://wb.amap.com/"),
        ),
    )

    @Suppress("SpellCheckingInspection")
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?surl\.amap\.com/\S+""")
    override val shortUriMethod = Input.ShortUriMethod.HEAD

    override fun parseUri(uri: Uri) = uri.run {
        PositionBuilder(srs).apply {
            setPointIfNull { """\w+,$LAT,$LON.+""" matchLatLonZ queryParams["p"] }
            setPointIfNull { """$LAT,$LON.+""" matchLatLonZ queryParams["q"] }
        }.toPair()
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_amap_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_amap_loading_indicator_title
}
