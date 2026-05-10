package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

object YandexMapsUriInput : UriInput, Input.HasRandomUri {
    override val pattern = Regex("""((?:https?://)?yandex(?:\.[a-z]{2,3})?\.[a-z]{2,3}/$URI_REST)""")
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.YANDEX_MAPS,
        items = listOf(
            InputDocumentationItem.Url(20, "https://ya.ru/maps"),
            InputDocumentationItem.Url(22, "https://yandex.az/maps"),
            InputDocumentationItem.Url(22, "https://yandex.by/maps"),
            InputDocumentationItem.Url(22, "https://yandex.co.il/maps"),
            InputDocumentationItem.Url(20, "https://yandex.com/maps"),
            InputDocumentationItem.Url(22, "https://yandex.com.am/maps"),
            InputDocumentationItem.Url(22, "https://yandex.com.ge/maps"),
            InputDocumentationItem.Url(22, "https://yandex.com.tr/maps"),
            InputDocumentationItem.Url(22, "https://yandex.ee/maps"),
            InputDocumentationItem.Url(22, "https://yandex.eu/maps"),
            InputDocumentationItem.Url(22, "https://yandex.fr/maps"),
            InputDocumentationItem.Url(22, "https://yandex.kg/maps"),
            InputDocumentationItem.Url(22, "https://yandex.kz/maps"),
            InputDocumentationItem.Url(22, "https://yandex.lt/maps"),
            InputDocumentationItem.Url(22, "https://yandex.lv/maps"),
            InputDocumentationItem.Url(22, "https://yandex.md/maps"),
            InputDocumentationItem.Url(22, "https://yandex.ru/maps"),
            InputDocumentationItem.Url(22, "https://yandex.tj/maps"),
            InputDocumentationItem.Url(22, "https://yandex.tm/maps"),
            InputDocumentationItem.Url(22, "https://yandex.ua/maps"),
            InputDocumentationItem.Url(22, "https://yandex.uz/maps"),
        ),
    )

    override suspend fun parse(data: Uri, match: String, prevPoints: Points?, uriQuote: UriQuote, log: Log) =
        buildParseResult {
            data.run {
                val z = listOf(@Suppress("SpellCheckingInspection") "whatshere[zoom]", "z")
                    .firstNotNullOfOrNull { key -> Z_PATTERN.matchEntire(queryParams[key])?.doubleGroupOrNull() }

                // Coordinates
                // https://yandex.com/maps?ll={lon},{lat}
                // https://yandex.com/maps?whatshere%5Bpoint%5D={lon}%2C{lat}
                listOf(@Suppress("SpellCheckingInspection") "whatshere[point]", "ll")
                    .firstNotNullOfOrNull { key ->
                        LON_LAT_PATTERN.matchEntire(queryParams[key])?.toLonLatPoint(Source.URI)
                    }?.let {
                        points = persistentListOf(WGS84Point(it).copy(z = z))
                        return@buildParseResult
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
                            nextInput = YandexMapsHtmlInput
                            return@buildParseResult
                        }

                        "org" -> {
                            // Old POI -- these links seem to return 404 now; we still keep the code in case they start working again
                            // https://yandex.com/maps/org/{id}?...
                            nextInput = YandexMapsHtmlInput
                            return@buildParseResult
                        }
                    }
                }
            }
        }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://yandex.com/maps?ll={lon}%2C{lat}&z={z}")

    override fun toString() = "YandexMapUriInput"
}
