package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toLatLonZPoint
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.Source
import page.ooooo.geoshare.lib.point.WGS84Point
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object HereWeGoInput : Input, Input.HasRandomUri {
    private const val SIMPLIFIED_BASE64 = """[A-Za-z0-9+/]+=*"""

    override val uriPattern = Regex("""(?:https?://)?(?:share|wego)\.here\.com/$URI_REST""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.HERE_WEGO,
        nameResId = R.string.converter_here_wego_name,
        items = listOf(
            InputDocumentationItem.Url(20, "https://share.here.com/l/"),
            InputDocumentationItem.Url(20, "https://share.here.com/p/"),
            InputDocumentationItem.Url(20, "https://wego.here.com/"),
            InputDocumentationItem.Url(20, "https://wego.here.com/p/"),
        ),
    )

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = buildParseUriResult {
        uri.run {
            val parts = uri.pathParts.drop(1)
            val firstPart = parts.firstOrNull() ?: return@run
            if (firstPart == "") {
                Regex("""$LAT,$LON,$Z""").matchEntire(queryParams["map"])?.toLatLonZPoint()?.let {
                    points = persistentListOf(it.asWGS84(Source.MAP_CENTER))
                }
            } else {
                val secondPart = parts.getOrNull(1)
                if (secondPart != null) {
                    val z = Regex(""".*,$Z""").matchEntire(queryParams["map"])?.doubleGroupOrNull()
                    if (firstPart == "l") {
                        LAT_LON_PATTERN.matchEntire(secondPart)?.toLatLonPoint()?.let {
                            points = persistentListOf(it.asWGS84(Source.URI).copy(z = z))
                        }
                    } else if (firstPart == "p") {
                        Regex("""[a-z]-($SIMPLIFIED_BASE64)""").matchEntire(secondPart)
                            ?.groupOrNull()
                            ?.let { encoded -> Base64.decode(encoded).decodeToString() }
                            ?.let { decoded ->
                                Regex("""(?:lat=|"latitude":)$LAT""").find(decoded)
                                    ?.doubleGroupOrNull()
                                    ?.let { lat ->
                                        Regex("""(?:lon=|"longitude":)$LON""").find(decoded)
                                            ?.doubleGroupOrNull()
                                            ?.let { lon ->
                                                points = persistentListOf(WGS84Point(lat, lon, z, source = Source.HASH))
                                            }
                                    }
                            }
                    }
                }
            }
        }
    }

    override fun genRandomUri(point: Point) =
        point.formatUriString("https://wego.here.com/?map={lat}%2C{lon},{z}")
}
