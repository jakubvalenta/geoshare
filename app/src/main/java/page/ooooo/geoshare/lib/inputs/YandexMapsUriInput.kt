package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.findAll
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YandexMapsUriInput @Inject constructor(
    private val yandexMapsHtmlInput: dagger.Lazy<YandexMapsHtmlInput>,
    override val uriQuote: UriQuote,
) : UriInput, Input.HasRandomUri {
    override val group = InputGroup.YANDEX_MAPS
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

    override suspend fun parse(data: Uri, match: String, resources: Resources) = parseResult {
        data.run {
            val z = listOf(@Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "whatshere[zoom]", "z")
                .firstNotNullOfOrNull { key -> Z_PATTERN.matchEntire(queryParams[key])?.doubleGroupOrNull() }

            // Directions
            // https://yandex.com/maps?rtext={lat}%2C{lon}~{lat}%2C{lon}~{lat}%2C{lon}
            LAT_LON_PATTERN.findAll(queryParams[@Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "rtext"])
                .mapNotNull { m -> m.toLatLonPoint(Source.URI)?.copy(z = z)?.let { WGS84Point(it) } }
                .toImmutableList()
                .takeIf { it.isNotEmpty() }
                ?.let {
                    points = it
                    return@parseResult
                }

            // Coordinates
            // https://yandex.com/maps?ll={lon},{lat}
            // https://yandex.com/maps?whatshere%5Bpoint%5D={lon}%2C{lat}
            listOf(@Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "whatshere[point]", "ll")
                .firstNotNullOfOrNull { key ->
                    LON_LAT_PATTERN.matchEntire(queryParams[key])?.toLonLatPoint(Source.URI)
                }?.let {
                    points = persistentListOf(WGS84Point(it, z))
                    return@parseResult
                }

            pathParts.forEachIndexed { i, part ->
                when (part) {
                    "geo" -> {
                        // POI
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
                        // Old POI -- these links seem to return 404 now; we still keep the code in case they start working again
                        // https://yandex.com/maps/org/{id}?...
                        next = MatchedInput(yandexMapsHtmlInput.get(), match)
                        return@parseResult
                    }
                }
            }
        }
    }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://yandex.com/maps?ll={lon}%2C{lat}&z={z}")

    override fun toString() = "YandexMapUriInput"
}
