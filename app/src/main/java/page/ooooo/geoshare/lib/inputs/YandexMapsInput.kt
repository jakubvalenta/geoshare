package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import io.ktor.utils.io.*
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.findLatLonZ
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.extensions.matchLatLonZ
import page.ooooo.geoshare.lib.extensions.matchZ
import page.ooooo.geoshare.lib.position.LatLonZ
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.position.buildPosition

object YandexMapsInput : Input.HasShortUri, Input.HasHtml {
    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?yandex(\.[a-z]{2,3})?\.[a-z]{2,3}/\S+""")
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
    override val shortUriPattern: Pattern =
        Pattern.compile("""(https?://)?yandex(\.[a-z]{2,3})?\.[a-z]{2,3}/maps/-/\S+""")
    override val shortUriMethod = Input.ShortUriMethod.HEAD

    override suspend fun parseUri(uri: Uri): ParseUriResult? {
        var htmlUriString: String? = null
        val position = buildPosition(srs) {
            uri.run {
                @Suppress("SpellCheckingInspection")
                setPointIfNull { LON_LAT_PATTERN matchLatLonZ queryParams["whatshere[point]"] }
                setPointIfNull { LON_LAT_PATTERN matchLatLonZ queryParams["ll"] }
                @Suppress("SpellCheckingInspection")
                setZIfNull { Z_PATTERN matchZ queryParams["whatshere[zoom]"] }
                setZIfNull { Z_PATTERN matchZ queryParams["z"] }
                if (!hasPoint() && ("""/maps/org/\d+([/?#].*|$)""" match path) != null) {
                    htmlUriString = uri.toString()
                }
            }
        }
        return ParseUriResult.from(position, htmlUriString)
    }

    override suspend fun parseHtml(channel: ByteReadChannel, positionFromUri: Position, log: ILog): ParseHtmlResult? {
        val positionFromHtml = buildPosition(srs) {
            val pattern = Pattern.compile("""ll=$LON%2C$LAT""")
            while (true) {
                val line = channel.readUTF8Line() ?: break
                (pattern findLatLonZ line)?.let { (lat, lon, z) ->
                    setPointIfNull { LatLonZ(lat, lon, z) }
                    break
                }
            }
        }
        return ParseHtmlResult.from(positionFromUri, positionFromHtml)
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title
}
