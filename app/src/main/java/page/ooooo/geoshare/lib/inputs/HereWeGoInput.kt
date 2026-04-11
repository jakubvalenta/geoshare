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
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Singleton
class HereWeGoInput @Inject constructor(
    private val uriFormatter: UriFormatter,
) : Input, Input.HasRandomUri {
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
                Regex("""$LAT,$LON,$Z""").matchEntire(queryParams["map"])?.toLatLonZPoint(Source.MAP_CENTER)?.let {
                    points = persistentListOf(WGS84Point(it))
                }
            } else {
                val secondPart = parts.getOrNull(1)
                if (secondPart != null) {
                    val z = Regex(""".*,$Z""").matchEntire(queryParams["map"])?.doubleGroupOrNull()
                    if (firstPart == "l") {
                        LAT_LON_PATTERN.matchEntire(secondPart)?.toLatLonPoint(Source.URI)?.let {
                            points = persistentListOf(WGS84Point(it).copy(z = z))
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
        uriFormatter.formatUriString(point, "https://wego.here.com/?map={lat}%2C{lon},{z}")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is HereWeGoInput
    }

    override fun hashCode() = javaClass.hashCode()

    private companion object {
        private const val SIMPLIFIED_BASE64 = """[A-Za-z0-9+/]+=*"""
    }
}
