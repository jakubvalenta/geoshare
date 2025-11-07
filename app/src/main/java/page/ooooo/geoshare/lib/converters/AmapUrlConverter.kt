package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import com.lbt05.evil_transform.GCJPointer
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matches

@Suppress("SpellCheckingInspection")
class AmapUrlConverter : UrlConverter.WithUriPattern, UrlConverter.WithShortUriPattern {
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(surl|wb)\.amap\.com/\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_amap_name,
        inputs = listOf(
            DocumentationInput.Url(26, "https://surl.amap.com/"),
            DocumentationInput.Url(26, "https://wb.amap.com/"),
        ),
    )
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?surl\.amap\.com/\S+""")
    override val shortUriMethod = ShortUriMethod.HEAD

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        on { queryParams["p"]?.let { it matches """\w+,$LAT,$LON.+""" } } doReturn { GCJPositionMatch(it) }
        on { queryParams["q"]?.let { it matches """$LAT,$LON.+""" } } doReturn { GCJPositionMatch(it) }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_amap_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_amap_loading_indicator_title

    private class GCJPositionMatch(matcher: Matcher) : PositionMatch(matcher) {
        override val points
            get() = matcher.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
                matcher.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
                    GCJPointer(lat, lon).toExactWGSPointer().let { wGSPointer ->
                        listOf(
                            Point(
                                wGSPointer.latitude,
                                wGSPointer.longitude,
                                desc = "WGS 84", // TODO Replace point description with custom output parameters
                            )
                        )
                    }
                }
            }
    }
}
