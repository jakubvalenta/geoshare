package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.matchLatLonZ
import page.ooooo.geoshare.lib.extensions.matchZ
import page.ooooo.geoshare.lib.position.PositionBuilder
import page.ooooo.geoshare.lib.position.Srs

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
            setPointIfNull { LAT_LON_PATTERN matchLatLonZ queryParams["pin"] }
            setPointIfNull { """$Z/$LAT/$LON.*""" matchLatLonZ fragment }
            setZIfNull { """$Z/.*""" matchZ fragment }
        }.toPair()
    }
}
