package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matches

class MapyComUrlConverter : UrlConverter.WithUriPattern, UrlConverter.WithShortUriPattern {
    companion object {
        const val COORDS = """(?P<lat>\d{1,2}(\.\d{1,16})?)[NS], (?P<lon>\d{1,3}(\.\d{1,16})?)[WE]"""
    }

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern =
        Pattern.compile("""$COORDS|(https?://)?((hapticke|www)\.)?mapy\.[a-z]{2,3}[/?]\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_mapy_com_name,
        inputs = listOf(
            DocumentationInput.Url(23, "https://mapy.com"),
            DocumentationInput.Url(23, "https://mapy.cz"),
            DocumentationInput.Url(23, "https://www.mapy.com"),
            DocumentationInput.Url(23, "https://www.mapy.cz"),
        ),
    )
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?mapy\.[a-z]{2,3}/s/\S+""")
    override val shortUriMethod = ShortUriMethod.GET

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        on { path matches COORDS } doReturn { NorthSouthWestEastPositionMatch(it) }
        all {
            optional {
                on { queryParams["z"]?.let { it matches Z } } doReturn { PositionMatch(it) }
            }
            on { queryParams["x"]?.let { it matches LON } } doReturn { PositionMatch(it) }
            on { queryParams["y"]?.let { it matches LAT } } doReturn { PositionMatch(it) }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_mapy_com_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_mapy_com_loading_indicator_title

    private class NorthSouthWestEastPositionMatch(matcher: Matcher) : PositionMatch(matcher) {
        override val points: List<Point>?
            get() {
                val lat = matcher.groupOrNull("lat")?.toDoubleOrNull() ?: return null
                val lon = matcher.groupOrNull("lon")?.toDoubleOrNull() ?: return null
                val latSig = if (matcher.groupOrNull()?.contains('S') == true) -1 else 1
                val lonSig = if (matcher.groupOrNull()?.contains('W') == true) -1 else 1
                return persistentListOf(Point(latSig * lat, lonSig * lon))
            }
    }
}
