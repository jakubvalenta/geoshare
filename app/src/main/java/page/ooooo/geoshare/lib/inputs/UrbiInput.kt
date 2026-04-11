package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.decodeBasicHtmlEntities
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.extensions.toLonLatZPoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.Source
import page.ooooo.geoshare.lib.point.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UrbiInput @Inject constructor(
    private val uriFormatter: UriFormatter,
) : HtmlInput, Input.HasRandomUri {
    override val uriPattern =
        Regex("""(?:https?://)?(?:www\.)?(?:(?:go|maps)\.)?(?:2gis|urbi|urbi-[a-z]{2})(?:\.[a-z]{2,3})?\.[a-z]{2,3}/$URI_REST""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.URBI,
        nameResId = R.string.converter_urbi_name,
        items = listOf(
            InputDocumentationItem.Url(27, "https://2gis.ae/"),
            InputDocumentationItem.Url(27, "https://2gis.am/"),
            InputDocumentationItem.Url(27, "https://2gis.az/"),
            InputDocumentationItem.Url(27, "https://2gis.cl/"),
            InputDocumentationItem.Url(27, "https://2gis.com.cy/"),
            InputDocumentationItem.Url(27, "https://2gis.com/"),
            InputDocumentationItem.Url(27, "https://2gis.cz/"),
            InputDocumentationItem.Url(27, "https://2gis.it/"),
            InputDocumentationItem.Url(27, "https://2gis.kg/"),
            InputDocumentationItem.Url(27, "https://2gis.kz/"),
            InputDocumentationItem.Url(27, "https://2gis.ru/"),
            InputDocumentationItem.Url(27, "https://2gis.uz/"),
            InputDocumentationItem.Url(27, "https://go.2gis.com/"),
            InputDocumentationItem.Url(27, "https://go.urbi.ae/"),
            InputDocumentationItem.Url(27, "https://maps.urbi.ae/"),
            InputDocumentationItem.Url(27, "https://urbi-bh.com/"),
            InputDocumentationItem.Url(27, "https://urbi-eg.com/"),
            InputDocumentationItem.Url(27, "https://urbi-kw.com/"),
            InputDocumentationItem.Url(27, "https://urbi-om.com/"),
            InputDocumentationItem.Url(27, "https://urbi-qa.com/"),
            InputDocumentationItem.Url(27, "https://urbi-sa.com/"),
            InputDocumentationItem.Url(27, "https://urbi.bh/"),
            InputDocumentationItem.Url(27, "https://urbi.qa/"),
        ),
    )

    override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = buildParseUriResult {
        uri.run {
            // Marker
            // https://maps.urbi.ae/dubai/geo/{lon}%2C{lat}?m={lon},{lat}/{z}
            Regex("""$LON,$LAT/$Z""").matchEntire(queryParams["m"])?.toLonLatZPoint(Source.URI)?.let {
                points = persistentListOf(WGS84Point(it))
                return@run
            }

            val z = Z_PATTERN.matchEntire(queryParams["zoom"])?.doubleGroupOrNull()

            // Point
            // https://maps.urbi.ae/dubai/geo/{lon}%2C{lat}
            Regex(""".*/$LON,$LAT/?$""").matchEntire(path)?.toLonLatPoint(Source.URI)?.let {
                points = persistentListOf(WGS84Point(it).copy(z = z))
                return@run
            }

            // API map center
            // https://share.api.2gis.ru/getimage?...&zoom={z}&center={lon},{lat}&title={name}...
            LON_LAT_PATTERN.matchEntire(queryParams["center"])?.toLonLatPoint(Source.MAP_CENTER)?.let {
                points = persistentListOf(
                    WGS84Point(it).copy(
                        z = z,
                        name = Q_PARAM_PATTERN.matchEntire(queryParams["title"])?.groupOrNull(),
                    )
                )
                return@run
            }

            htmlUriString = toString()
        }
    }

    override suspend fun parseHtml(
        htmlUrlString: String,
        channel: ByteReadChannel,
        pointsFromUri: ImmutableList<Point>,
        uriQuote: UriQuote,
        log: ILog,
    ) = buildParseHtmlResult {
        val pattern = Regex("""property="twitter:image" content="([^"]+)""")

        // Notice that unlike in other Inputs, we don't copy any point names from pointsFromUri here

        while (true) {
            val line = channel.readLine() ?: break
            pattern.find(line)?.groupOrNull()?.let { attr ->
                val uri = Uri.parse(attr.decodeBasicHtmlEntities(), uriQuote)
                val res = parseUri(uri)
                if (res.points.isNotEmpty()) {
                    points = res.points
                    return@buildParseHtmlResult
                }
            }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_urbi_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_urbi_loading_indicator_title

    override fun genRandomUri(point: Point) =
        uriFormatter.formatUriString(point, "https://maps.urbi.ae/dubai/geo/{lon}%2C{lat}?m={lon}%2C{lat}%2F{z}")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is UrbiInput
    }

    override fun hashCode() = javaClass.hashCode()
}
