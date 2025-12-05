package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import io.ktor.utils.io.*
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.findLatLonZ
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.extensions.matchLatLonZ
import page.ooooo.geoshare.lib.extensions.matchZ
import page.ooooo.geoshare.lib.position.LatLonZ
import page.ooooo.geoshare.lib.position.PositionBuilder
import page.ooooo.geoshare.lib.position.Srs

object YandexMapsInput : Input.HasShortUri, Input.HasHtml {
    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?yandex(\.[a-z]{2,3})?\.[a-z]{2,3}/\S+""")
    override val documentation = Input.Documentation(
        id = Input.DocumentationId.YANDEX_MAPS,
        nameResId = R.string.converter_yandex_maps_name,
        inputs = listOf(
            Input.DocumentationInput.Url(20, "https://ya.ru/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.az/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.by/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.co.il/maps"),
            Input.DocumentationInput.Url(20, "https://yandex.com/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.com.am/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.com.ge/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.com.tr/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.ee/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.eu/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.fr/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.kg/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.kz/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.lt/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.lv/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.md/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.ru/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.tj/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.tm/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.ua/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.uz/maps"),
        ),
    )
    override val shortUriPattern: Pattern =
        Pattern.compile("""(https?://)?yandex(\.[a-z]{2,3})?\.[a-z]{2,3}/maps/-/\S+""")
    override val shortUriMethod = Input.ShortUriMethod.HEAD

    override fun parseUri(uri: Uri) = uri.run {
        PositionBuilder(srs).apply {
            @Suppress("SpellCheckingInspection")
            setPointIfNull { LON_LAT_PATTERN matchLatLonZ queryParams["whatshere[point]"] }
            setPointIfNull { LON_LAT_PATTERN matchLatLonZ queryParams["ll"] }
            @Suppress("SpellCheckingInspection")
            setZIfNull { Z_PATTERN matchZ queryParams["whatshere[zoom]"] }
            setZIfNull { Z_PATTERN matchZ queryParams["z"] }
            setUriStringIfNull { if (("""/maps/org/\d+([/?#].*|$)""" match path) != null) uri.toString() else null }
        }.toPair()
    }

    override suspend fun parseHtml(channel: ByteReadChannel) =
        PositionBuilder(srs).apply {
            val pattern = Pattern.compile("""ll=$LON%2C$LAT""")
            while (true) {
                val line = channel.readUTF8Line() ?: break
                (pattern findLatLonZ line)?.let { (lat, lon, z) ->
                    setPointIfNull { LatLonZ(lat, lon, z) }
                    break
                }
            }
        }.toPair()

    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title
}
