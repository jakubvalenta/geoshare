package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionMatch
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.Srs
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.conversionPattern
import page.ooooo.geoshare.lib.extensions.matches

class AmapUrlConverter : UrlConverter.WithUriPattern, UrlConverter.WithShortUriPattern {
    private val srs = Srs.GCJ02

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(surl|wb)\.amap\.com/\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_amap_name,
        inputs = listOf(
            DocumentationInput.Url(26, "https://surl.amap.com/"),
            DocumentationInput.Url(26, "https://wb.amap.com/"),
        ),
    )

    @Suppress("SpellCheckingInspection")
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?surl\.amap\.com/\S+""")
    override val shortUriMethod = ShortUriMethod.HEAD

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        on { queryParams["p"]?.let { it matches """\w+,$LAT,$LON.+""" } } doReturn { PositionMatch(it, srs) }
        on { queryParams["q"]?.let { it matches """$LAT,$LON.+""" } } doReturn { PositionMatch(it, srs) }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_amap_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_amap_loading_indicator_title
}
