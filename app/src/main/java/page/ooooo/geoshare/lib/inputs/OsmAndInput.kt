package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ConversionPattern
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LAT
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LAT_LON_PATTERN
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LON
import page.ooooo.geoshare.lib.ConversionPattern.Companion.Z
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.position.*

object OsmAndInput : Input.HasUri {
    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?osmand\.net/\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_osm_and_name,
        inputs = listOf(
            Input.DocumentationInput.Url(20, "https://osmand.net/map"),
        ),
    )

    override val conversionUriPattern = ConversionPattern.first<Uri, Position> {
        all {
            optional {
                pattern { (fragment matches """$Z/.*""")?.toZ(srs) }
            }
            first {
                pattern { queryParams["pin"]?.let { it matches LAT_LON_PATTERN }?.toLatLon(srs) }
                pattern { (fragment matches """$Z/$LAT/$LON.*""")?.toLatLonZ(srs) }
            }
        }
    }
}
