package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.matchLatLonZName
import page.ooooo.geoshare.lib.extensions.matchZ
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.position.buildPosition

object OsmAndInput : Input {
    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?osmand\.net/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.OSM_AND,
        nameResId = R.string.converter_osm_and_name,
        items = listOf(
            InputDocumentationItem.Url(20, "https://osmand.net/map"),
        ),
    )

    override suspend fun parseUri(uri: Uri): ParseUriResult? {
        val position = buildPosition(srs) {
            uri.run {
                setPointIfNull { LAT_LON_PATTERN matchLatLonZName queryParams["pin"] }
                setPointIfNull { """$Z/$LAT/$LON.*""" matchLatLonZName fragment }
                setZIfNull { """$Z/.*""" matchZ fragment }
            }
        }
        return ParseUriResult.from(position)
    }
}
