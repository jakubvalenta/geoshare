package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonNamePoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.GCJ02GreaterChinaAndTaiwanPoint
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmapUriInput @Inject constructor(
    override val uriQuote: UriQuote,
) : UriInput, Input.HasRandomUri {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.AMAP
    override val changelog = persistentListOf(
        InputChangelogItem.Url(27, "https://wb.amap.com/"),
    )

    override val pattern = Regex("""((?:https?://)?wb\.amap\.com/$URI_REST)""")

    override suspend fun parse(data: Uri, match: String, resources: Resources) = parseResult {
        data.run {
            // Coordinates and name in param 'p' (mobile)
            // https://wb.amap.com/?p=<id>,<lat>,<lon>,<name>
            Regex("""\w+,$LAT,$LON,?(?:$NAME_PARAM)?.*""").matchEntire(queryParams["p"]?.firstOrNull())
                ?.toLatLonNamePoint(Source.URI)
                ?.let {
                    points = persistentListOf(GCJ02GreaterChinaAndTaiwanPoint(it))
                    return@parseResult
                }

            // Coordinates and name in param 'q' (mobile)
            // https://wb.amap.com/?q=<lat>,<lon>,<name>
            Regex("""$LAT,$LON,?(?:$NAME_PARAM)?.*""").matchEntire(queryParams["q"]?.firstOrNull())
                ?.toLatLonNamePoint(Source.URI)
                ?.let {
                    points = persistentListOf(GCJ02GreaterChinaAndTaiwanPoint(it))
                    return@parseResult
                }

            // Coordinates and name in params 'lat', 'lng' and 'name' (desktop)
            LAT_PATTERN.matchEntire(queryParams["lat"]?.firstOrNull())?.doubleGroupOrNull()?.let { lat ->
                LON_PATTERN.matchEntire(queryParams["lng"]?.firstOrNull())?.doubleGroupOrNull()?.let { lon ->
                    val name = Q_PARAM_PATTERN.matchEntire(queryParams["name"]?.firstOrNull())?.groupOrNull()
                    points = persistentListOf(
                        GCJ02GreaterChinaAndTaiwanPoint(lat, lon, name = name, source = Source.URI)
                    )
                    return@parseResult
                }
            }
        }
    }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://wb.amap.com/?q={lat}%2C{lon}")

    override fun toString() = "AmapUriInput"
}
