package page.ooooo.geoshare.lib.inputs

import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toZLatLonPoint
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints

object OsmAndInput : Input, Input.HasRandomUri {
    override val uriPattern = Regex("""(?:https?://)?(?:www\.)?osmand\.net/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.OSM_AND,
        nameResId = R.string.converter_osm_and_name,
        items = listOf(
            InputDocumentationItem.Url(20, "https://osmand.net/map"),
        ),
    )

    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        points = buildPoints {
            uri.run {
                listOf("pin", "finish", "start").firstNotNullOfOrNull { key ->
                    LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint()?.also { points.add(it) }
                } ?: Regex("""$Z/$LAT/$LON.*""").matchEntire(fragment)?.toZLatLonPoint()?.also { points.add(it) }

                Regex("""$Z/.*""").matchEntire(fragment)?.doubleGroupOrNull()?.also { defaultZ = it }
            }
        }.asWGS84()
    }

    override fun genRandomUri(point: Point) =
        point.formatUriString("https://osmand.net/map?pin={lat}%2C{lon}")
}
