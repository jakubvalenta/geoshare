package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.data.extensions.doubleGroupOrNull
import page.ooooo.geoshare.data.extensions.matchEntire
import page.ooooo.geoshare.data.extensions.toLonLatPoint
import page.ooooo.geoshare.data.formatters.UriFormatter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.uri.Uri
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YandexMapsUriInput @Inject constructor(
    private val yandexMapsHtmlInput: dagger.Lazy<YandexMapsHtmlInput>,
    private val uriQuote: UriQuote,
) : BasicOfflineInput, Input.HasPattern, Input.HasRandomSource {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = YandexMapsInputGroup
    override val changelog = persistentListOf(
        InputChangelogItem.Url(20, "https://ya.ru/maps"),
        InputChangelogItem.Url(22, "https://yandex.az/maps"),
        InputChangelogItem.Url(22, "https://yandex.by/maps"),
        InputChangelogItem.Url(22, "https://yandex.co.il/maps"),
        InputChangelogItem.Url(20, "https://yandex.com/maps"),
        InputChangelogItem.Url(22, "https://yandex.com.am/maps"),
        InputChangelogItem.Url(22, "https://yandex.com.ge/maps"),
        InputChangelogItem.Url(22, "https://yandex.com.tr/maps"),
        InputChangelogItem.Url(22, "https://yandex.ee/maps"),
        InputChangelogItem.Url(22, "https://yandex.eu/maps"),
        InputChangelogItem.Url(22, "https://yandex.fr/maps"),
        InputChangelogItem.Url(22, "https://yandex.kg/maps"),
        InputChangelogItem.Url(22, "https://yandex.kz/maps"),
        InputChangelogItem.Url(22, "https://yandex.lt/maps"),
        InputChangelogItem.Url(22, "https://yandex.lv/maps"),
        InputChangelogItem.Url(22, "https://yandex.md/maps"),
        InputChangelogItem.Url(22, "https://yandex.ru/maps"),
        InputChangelogItem.Url(22, "https://yandex.tj/maps"),
        InputChangelogItem.Url(22, "https://yandex.tm/maps"),
        InputChangelogItem.Url(22, "https://yandex.ua/maps"),
        InputChangelogItem.Url(22, "https://yandex.uz/maps"),
    )

    override val pattern = Regex("""((?:https?://)?yandex(?:\.[a-z]{2,3})?\.[a-z]{2,3}/$URI_REST)""")

    override fun parse(match: String, resources: Resources) =
        Uri.parse(match, uriQuote).run {
            parseResult {
                // Zoom
                // https://yandex.com/maps?whatshere%5Bzoom%5D={z}
                // https://yandex.com/maps?z={z}
                val z = listOf(
                    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "whatshere[zoom]",
                    "z",
                )
                    .firstNotNullOfOrNull { key ->
                        Z_PATTERN.matchEntire(queryParams[key]?.firstOrNull())?.doubleGroupOrNull()
                    }

                // Point
                // https://yandex.com/maps?whatshere%5Bpoint%5D={lon}%2C{lat}
                LON_LAT_PATTERN.matchEntire(
                    queryParams[@Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "whatshere[point]"]
                        ?.firstOrNull()
                )?.toLonLatPoint(Source.URI)?.let {
                    points = persistentListOf(WGS84Point(it, z))
                    return@parseResult
                }

                // Directions
                // https://yandex.com/maps?rtext={point1Lat}%2C{poin1Lon}~{point2Lat}%2C{point2Lon}~...~{lastPointLat}%2C{lastPointLon}
                queryParams[@Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "rtext"]?.firstOrNull()
                    ?.let { pointsStr ->
                        pointsStr
                            .split('~')
                            .filter { it.isNotBlank() }
                            .mapNotNull { pointStr ->
                                pointStr.split(',', limit = 2).takeIf { it.size == 2 }?.let { latLon ->
                                    latLon.getOrNull(0)?.toDoubleOrNull()?.let { lat ->
                                        latLon.getOrNull(1)?.toDoubleOrNull()?.let { lon ->
                                            WGS84Point(lat, lon, z = z, source = Source.URI)
                                        }
                                    }
                                }
                            }
                            .takeIf { it.isNotEmpty() }
                            ?.let {
                                points = it.toImmutableList()
                                return@parseResult
                            }
                    }

                pathParts.forEachIndexed { i, part ->
                    when (part) {
                        "geo" -> {
                            // POI Geo
                            // https://yandex.com/maps/.../.../geo/{name}/{id}/
                            points = persistentListOf(
                                WGS84Point(
                                    name = pathParts.getOrNull(i + 1)?.replace('_', ' '),
                                    source = Source.URI,
                                ),
                            )
                            next = MatchedInput(yandexMapsHtmlInput.get(), match)
                            return@parseResult
                        }

                        "org" -> {
                            // POI Org -- these links seem to return 404 now; we still keep the code in case they start working again
                            // https://yandex.com/maps/org/{id}?...
                            next = MatchedInput(yandexMapsHtmlInput.get(), match)
                            return@parseResult
                        }
                    }
                }

                // Map center
                // https://yandex.com/maps?ll={lon},{lat}
                LON_LAT_PATTERN.matchEntire(queryParams["ll"]?.firstOrNull())?.toLonLatPoint(Source.MAP_CENTER)?.let {
                    points = persistentListOf(WGS84Point(it, z))
                    return@parseResult
                }
            }
        }

    override fun genRandomSource(point: Point) =
        UriFormatter.formatUriString(point, "https://yandex.com/maps?ll={lon}%2C{lat}&z={z}")

    override fun toString() = "YandexMapUriInput"
}
