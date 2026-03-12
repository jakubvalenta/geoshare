package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toZLatLonPoint
import page.ooooo.geoshare.lib.point.Point

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
        // TODO Extract both start and finish
        uri.run {
            val z = Regex("""$Z/.*""").matchEntire(fragment)?.doubleGroupOrNull()

            // Pin
            // https://osmand.net/map?pin={lat},{lon}
            // Directions
            // https://osmand.net/map?start={lat},{lon}&finish={lat},{lon}
            listOf("pin", "finish", "start")
                .firstNotNullOfOrNull { key -> LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint() }?.also {
                    points = persistentListOf(it.asWGS84().copy(z = z))
                    return@run
                }

            // View
            // https://osmand.net/map#{z}/{lat}/{lon}
            Regex("""$Z/$LAT/$LON.*""").matchEntire(fragment)?.toZLatLonPoint()?.also {
                points = persistentListOf(it.asWGS84().copy(z = z))
                return@run
            }
        }
    }

    override fun genRandomUri(point: Point) =
        point.formatUriString("https://osmand.net/map?pin={lat}%2C{lon}")
}
