package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionMatch
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.conversionPattern

class OsmAndUrlConverter : UrlConverter.WithUriPattern {
    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?osmand\.net/\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_osm_and_name,
        inputs = listOf(
            DocumentationInput.Url(20, "https://osmand.net/map"),
        ),
    )

    override val conversionUriPattern = conversionPattern {
        all {
            fragment("""$Z/$LAT/$LON.*""") { PositionMatch(it) }
            query("pin", """$LAT,$LON""") { PositionMatch(it) }
        }
        fragment("""$Z/$LAT/$LON.*""") { PositionMatch(it) }
        query("pin", """$LAT,$LON""") { PositionMatch(it) }
    }
}
