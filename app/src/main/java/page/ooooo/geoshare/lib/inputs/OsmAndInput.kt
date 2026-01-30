package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.matchNaivePoint
import page.ooooo.geoshare.lib.extensions.matchZ
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints
import page.ooooo.geoshare.lib.point.toParseUriResult

object OsmAndInput : Input {
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?osmand\.net/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.OSM_AND,
        nameResId = R.string.converter_osm_and_name,
        items = listOf(
            InputDocumentationItem.Url(20, "https://osmand.net/map"),
        ),
    )

    override suspend fun parseUri(uri: Uri): ParseUriResult? =
        buildPoints {
            uri.run {
                (LAT_LON_PATTERN matchNaivePoint queryParams["pin"])?.also { points.add(it) }
                    ?: ("""$Z/$LAT/$LON.*""" matchNaivePoint fragment)?.also { points.add(it) }

                ("""$Z/.*""" matchZ fragment)?.also { defaultZ = it }
            }
        }
            .asWGS84()
            .toParseUriResult()
}
