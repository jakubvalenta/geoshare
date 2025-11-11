package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionMatch
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.conversionPattern
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Srs

object MapyComInput : Input.HasUri, Input.HasShortUri {
    private const val COORDS = """(?P<lat>\d{1,2}(\.\d{1,16})?)[NS], (?P<lon>\d{1,3}(\.\d{1,16})?)[WE]"""

    private val srs = Srs.WGS84

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern =
        Pattern.compile("""$COORDS|(https?://)?((hapticke|www)\.)?mapy\.[a-z]{2,3}[/?]\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_mapy_com_name,
        inputs = listOf(
            Input.DocumentationInput.Url(23, "https://mapy.com"),
            Input.DocumentationInput.Url(23, "https://mapy.cz"),
            Input.DocumentationInput.Url(23, "https://www.mapy.com"),
            Input.DocumentationInput.Url(23, "https://www.mapy.cz"),
        ),
    )
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?mapy\.[a-z]{2,3}/s/\S+""")
    override val shortUriMethod = Input.ShortUriMethod.GET

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        on { path matches COORDS } doReturn { NorthSouthWestEastPositionMatch(it, srs) }
        all {
            optional {
                on { queryParams["z"]?.let { it matches Z } } doReturn { PositionMatch.Zoom(it, srs) }
            }
            on { queryParams["x"]?.let { it matches LON } } doReturn { PositionMatch.Lon(it, srs) }
            on { queryParams["y"]?.let { it matches LAT } } doReturn { PositionMatch.Lat(it, srs) }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_mapy_com_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_mapy_com_loading_indicator_title

    private class NorthSouthWestEastPositionMatch(matcher: Matcher, srs: Srs) : PositionMatch.LatLon(matcher, srs) {
        override val latLon
            get() = matcher.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
                matcher.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
                    val latSig = if (matcher.groupOrNull()?.contains('S') == true) -1 else 1
                    val lonSig = if (matcher.groupOrNull()?.contains('W') == true) -1 else 1
                    Pair(latSig * lat, lonSig * lon)
                }
            }
    }
}
