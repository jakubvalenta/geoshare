package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.*

object OsmAndInput : Input {
    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?osmand\.net/\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_osm_and_name,
        inputs = listOf(
            Input.DocumentationInput.Url(20, "https://osmand.net/map"),
        ),
    )

    override fun parseUri(uri: Uri) = uri.run {
        PositionBuilder(srs).apply {
            setPointFromMatcher { LAT_LON_PATTERN match queryParams["pin"] }
            setPointAndZoomFromMatcher { """$Z/$LAT/$LON.*""" match fragment }
            setZoomFromMatcher { """$Z/.*""" match fragment }
        }
    }
}
