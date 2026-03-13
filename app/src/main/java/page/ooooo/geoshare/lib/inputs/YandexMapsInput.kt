package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.point.Point

object YandexMapsInput : ShortUriInput, HtmlInput, Input.HasRandomUri {
    override val uriPattern = Regex("""(?:https?://)?yandex(?:\.[a-z]{2,3})?\.[a-z]{2,3}/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.YANDEX_MAPS,
        nameResId = R.string.converter_yandex_maps_name,
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
    override val shortUriPattern =
        Regex("""(?:https?://)?yandex(?:\.[a-z]{2,3})?\.[a-z]{2,3}/maps/-/\S+""")
    override val shortUriMethod = ShortUriInput.Method.HEAD

    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        uri.run {
            val z = listOf(@Suppress("SpellCheckingInspection") "whatshere[zoom]", "z")
                .firstNotNullOfOrNull { key -> Z_PATTERN.matchEntire(queryParams[key])?.doubleGroupOrNull() }

            // Coordinates
            // https://yandex.com/maps?ll={lon},{lat}
            // https://yandex.com/maps?whatshere%5Bpoint%5D={lon}%2C{lat}
            listOf(@Suppress("SpellCheckingInspection") "whatshere[point]", "ll")
                .firstNotNullOfOrNull { key -> LON_LAT_PATTERN.matchEntire(queryParams[key])?.toLonLatPoint() }?.let {
                    points = persistentListOf(it.asWGS84().copy(z = z))
                    return@buildParseUriResult
                }

            // Organization -- these links seem to return 404 now; we still keep the code in case they start working again
            // https://yandex.com/maps/org/94933420809?spam
            if (Regex("""/maps/org/\d+(?:[/?#].*|$)""").matches(path)) {
                htmlUriString = uri.toString()
            }
        }
    }

    override suspend fun parseHtml(
        htmlUrlString: String,
        channel: ByteReadChannel,
        pointsFromUri: ImmutableList<Point>,
        log: ILog,
    ) = buildParseHtmlResult {
        val name = pointsFromUri.lastOrNull()?.name

        val pattern = Regex("""ll=$LON%2C$LAT""")
        while (true) {
            val line = channel.readLine() ?: break
            pattern.find(line)?.toLonLatPoint()?.let {
                points = persistentListOf(it.asWGS84().copy(name = name))
                return@buildParseHtmlResult
            }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title

    override fun genRandomUri(point: Point) =
        point.formatUriString("https://yandex.com/maps?ll={lon}%2C{lat}&z={z}")
}
