package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YahooMapsUriInput @Inject constructor(
    val yahooMapsWebViewInput: dagger.Lazy<YahooMapsWebViewInput>,
    val uriQuote: UriQuote,
) : BasicOfflineInput, Input.HasPattern, Input.HasRandomUri {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.YAHOO_MAPS
    override val changelog = persistentListOf(
        InputChangelogItem.Url(51, "https://map.yahoo.co.jp"),
    )

    override val pattern = Regex("""((?:https?://)?map\.yahoo(?:\.[a-z]{2,3})?\.[a-z]{2,3}[/?#]$URI_REST)""")

    override fun parse(match: String, resources: Resources) =
        Uri.parse(match, uriQuote).run {
            parseResult {
                // Zoom
                // https://map.yahoo.co.jp/?zoom={z}
                val z = Z_PATTERN.matchEntire(queryParams["zoom"]?.firstOrNull())?.doubleGroupOrNull()

                // Query
                // https://map.yahoo.co.jp/search?q={q}
                val name = Q_PARAM_PATTERN.matchEntire(queryParams["q"]?.firstOrNull())?.groupOrNull()?.let { q ->
                    q.trim().takeIf { it.isNotEmpty() }
                }

                when (pathParts.getOrNull(1)) {
                    "place" ->
                        if (queryParams["gid"]?.firstOrNull().isNullOrEmpty()) {
                            // Point
                            // https://map.yahoo.co.jp/place?lat={lat}&lon={lot}
                            LAT_PATTERN.matchEntire(queryParams["lat"]?.firstOrNull())?.doubleGroupOrNull()
                                ?.let { lat ->
                                    LON_PATTERN.matchEntire(queryParams["lon"]?.firstOrNull())?.doubleGroupOrNull()
                                        ?.let { lon ->
                                            points =
                                                persistentListOf(WGS84Point(lat, lon, z, name, source = Source.URI))
                                            return@parseResult
                                        }
                                }
                        } else {
                            // Place (don't use 'lat' and 'lon', because that's map center and user expects the point location)
                            // https://map.yahoo.co.jp/place?gid={id}
                            next = MatchedInput(yahooMapsWebViewInput.get(), match)
                            return@parseResult
                        }

                    // Directions
                    // https://map.yahoo.co.jp/route/{type}?from={point1Name}&to={lastPointName}&fromLat={point1Lat}&fromLon={point1Lon}&toLat={lastPointLat}&toLon={lastPointLon}&waypoints=name:{point2Name},lat:{point2Lat},lon:{point2Lon};...
                    "route" ->
                        buildList {
                            val fromLat =
                                LAT_PATTERN.matchEntire(queryParams["fromLat"]?.firstOrNull())?.doubleGroupOrNull()
                            val fromLon =
                                LON_PATTERN.matchEntire(queryParams["fromLon"]?.firstOrNull())?.doubleGroupOrNull()
                            val fromName =
                                Q_PARAM_PATTERN.matchEntire(queryParams["from"]?.firstOrNull())?.groupOrNull()
                            if (fromLat != null && fromLon != null) {
                                add(WGS84Point(fromLat, fromLon, z, name = fromName, source = Source.URI))
                            } else if (fromName != null) {
                                add(WGS84Point(z = z, name = fromName, source = Source.URI))
                            }
                            queryParams["waypoints"]?.firstOrNull()?.let { pointsStr ->
                                pointsStr
                                    .split(';')
                                    .filter { it.isNotBlank() }
                                    .forEach { pointStr ->
                                        var pointLat: Double? = null
                                        var pointLon: Double? = null
                                        var pointName: String? = null
                                        pointStr
                                            .split(',')
                                            .forEach { pointKeyValStr ->
                                                val pointKeyVal = pointKeyValStr.split(':', limit = 2)
                                                val pointKey = pointKeyVal.getOrNull(0)
                                                val pointVal = pointKeyVal.getOrNull(1)
                                                if (pointVal != null) {
                                                    when (pointKey) {
                                                        "lat" -> {
                                                            pointLat = pointVal.toDoubleOrNull()
                                                        }

                                                        "lon" -> {
                                                            pointLon = pointVal.toDoubleOrNull()
                                                        }

                                                        "name" -> {
                                                            pointName = pointVal
                                                        }
                                                    }
                                                }
                                            }
                                        if (pointLat != null && pointLon != null) {
                                            add(
                                                WGS84Point(
                                                    pointLat,
                                                    pointLon,
                                                    z,
                                                    name = pointName,
                                                    source = Source.URI
                                                )
                                            )
                                        } else if (pointName != null) {
                                            add(WGS84Point(z = z, name = pointName, source = Source.URI))
                                        }
                                    }
                            }
                            val toLat =
                                LAT_PATTERN.matchEntire(queryParams["toLat"]?.firstOrNull())?.doubleGroupOrNull()
                            val toLon =
                                LON_PATTERN.matchEntire(queryParams["toLon"]?.firstOrNull())?.doubleGroupOrNull()
                            val toName = Q_PARAM_PATTERN.matchEntire(queryParams["to"]?.firstOrNull())?.groupOrNull()
                            if (toLat != null && toLon != null) {
                                add(WGS84Point(toLat, toLon, z, name = toName, source = Source.URI))
                            } else if (toName != null) {
                                add(WGS84Point(z = z, name = toName, source = Source.URI))
                            }
                        }
                            .takeIf { it.isNotEmpty() }
                            ?.let {
                                points = it.toImmutableList()
                                return@parseResult
                            }
                }

                // Map center
                // https://map.yahoo.co.jp/?lat={lat}&lon={lot}
                LAT_PATTERN.matchEntire(queryParams["lat"]?.firstOrNull())?.doubleGroupOrNull()?.let { lat ->
                    LON_PATTERN.matchEntire(queryParams["lon"]?.firstOrNull())?.doubleGroupOrNull()?.let { lon ->
                        points = persistentListOf(WGS84Point(lat, lon, z, name, source = Source.MAP_CENTER))
                        return@parseResult
                    }
                }

                if (name != null) {
                    points = persistentListOf(WGS84Point(name = name, source = Source.URI))
                }
            }
        }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://map.yahoo.co.jp/place?lat={lat}&lon={lon}&zoom={z}")

    override fun toString() = "YahooMapsUriInput"
}
