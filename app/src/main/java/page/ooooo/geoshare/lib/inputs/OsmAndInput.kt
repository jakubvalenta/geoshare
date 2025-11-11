package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionMatch
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.conversionPattern
import page.ooooo.geoshare.lib.extensions.matches

object OsmAndInput : Input.HasUri {
    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?osmand\.net/\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_osm_and_name,
        inputs = listOf(
            Input.DocumentationInput.Url(20, "https://osmand.net/map"),
        ),
    )

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        all {
            optional {
                on { fragment matches """$Z/.*""" } doReturn { PositionMatch(it, srs) }
            }
            first {
                on { queryParams["pin"]?.let { it matches """$LAT,$LON""" } } doReturn { PositionMatch(it, srs) }
                on { fragment matches """$Z/$LAT/$LON.*""" } doReturn { PositionMatch(it, srs) }
            }
        }
    }
}
