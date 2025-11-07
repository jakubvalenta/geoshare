package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionMatch
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.conversionPattern
import page.ooooo.geoshare.lib.extensions.matches

class OsmAndUrlConverter : UrlConverter.WithUriPattern {
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?osmand\.net/\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_osm_and_name,
        inputs = listOf(
            DocumentationInput.Url(20, "https://osmand.net/map"),
        ),
    )

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        all {
            on { fragment matches """$Z/$LAT/$LON.*""" } doReturn { PositionMatch(it) }
            on { queryParams["pin"]?.let { it matches """$LAT,$LON""" } } doReturn { PositionMatch(it) }
        }
        on { fragment matches """$Z/$LAT/$LON.*""" } doReturn { PositionMatch(it) }
        on { queryParams["pin"]?.let { it matches """$LAT,$LON""" } } doReturn { PositionMatch(it) }
    }
}
