package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.conversion.ConversionPattern
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LAT
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LON
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.position.toLatLon

object AmapInput : Input.HasUri, Input.HasShortUri {
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

    override val conversionUriPattern = ConversionPattern.first<Uri, Position> {
        pattern { ("""\w+,$LAT,$LON.+""" match queryParams["p"])?.toLatLon(srs) }
        pattern { ("""$LAT,$LON.+""" match queryParams["q"])?.toLatLon(srs) }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_amap_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_amap_loading_indicator_title
}
