package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.data.extensions.doubleGroupOrNull
import page.ooooo.geoshare.data.extensions.groupOrNull
import page.ooooo.geoshare.data.extensions.matchEntire
import page.ooooo.geoshare.data.extensions.toLatLonPoint
import page.ooooo.geoshare.data.extensions.toLatLonZPoint
import page.ooooo.geoshare.data.formatters.UriFormatter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.uri.Uri
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64

@Singleton
class HereWeGoUriInput @Inject constructor(
    private val uriQuote: UriQuote,
) : BasicOfflineInput, Input.HasPattern, Input.HasRandomUri {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.HERE_WEGO
    override val changelog = persistentListOf(
        InputChangelogItem.Url(20, "https://share.here.com/l/"),
        InputChangelogItem.Url(20, "https://share.here.com/p/"),
        InputChangelogItem.Url(20, "https://wego.here.com/"),
        InputChangelogItem.Url(20, "https://wego.here.com/p/"),
    )

    override val pattern = Regex("""((?:https?://)?(?:share|wego)\.here\.com/$URI_REST)""")

    override fun parse(match: String, resources: Resources) =
        Uri.parse(match, uriQuote).run {
            parseResult {
                val parts = pathParts.drop(1)
                val firstPart = parts.firstOrNull() ?: return@parseResult
                if (firstPart == "") {
                    Regex("""$LAT,$LON,$Z""").matchEntire(queryParams["map"]?.firstOrNull())
                        ?.toLatLonZPoint(Source.MAP_CENTER)?.let {
                            points = persistentListOf(WGS84Point(it))
                        }
                } else {
                    val secondPart = parts.getOrNull(1)
                    if (secondPart != null) {
                        val z = Regex(""".*,$Z""").matchEntire(queryParams["map"]?.firstOrNull())?.doubleGroupOrNull()
                        if (firstPart == "l") {
                            LAT_LON_PATTERN.matchEntire(secondPart)?.toLatLonPoint(Source.URI)?.let {
                                points = persistentListOf(WGS84Point(it, z))
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
                                                    points =
                                                        persistentListOf(WGS84Point(lat, lon, z, source = Source.HASH))
                                                }
                                        }
                                }
                        }
                    }
                }
            }
        }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://wego.here.com/?map={lat}%2C{lon},{z}")

    override fun toString() = "HereWegoUriInput"

    private companion object {
        private const val SIMPLIFIED_BASE64 = """[A-Za-z0-9+/]+=*"""
    }
}
