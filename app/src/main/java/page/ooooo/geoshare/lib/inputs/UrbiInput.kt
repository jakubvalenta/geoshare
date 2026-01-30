package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.findPoint
import page.ooooo.geoshare.lib.extensions.matchPoint
import page.ooooo.geoshare.lib.extensions.matchZ
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints
import page.ooooo.geoshare.lib.point.toParseHtmlResult
import page.ooooo.geoshare.lib.point.toParseUriResult

object UrbiInput : Input.HasHtml {
    override val uriPattern: Pattern =
        Pattern.compile("""(https?://)?(www\.)?((go|maps)\.)?(2gis|urbi|urbi-[a-z]{2})(\.[a-z]{2,3})?\.[a-z]{2,3}/\S+""")
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

    override suspend fun parseUri(uri: Uri): ParseUriResult? =
        buildPoints {
            uri.run {
                ("""$LON,$LAT/$Z""" matchPoint queryParams["m"])?.also { points.add(it) }
                    ?: (""".*/$LON,$LAT/?$""" matchPoint path)?.also { points.add(it) }
                    ?: (LON_LAT_PATTERN matchPoint queryParams["center"])?.also { points.add(it) }

                (Z_PATTERN matchZ queryParams["zoom"])?.also { defaultZ = it }
            }
        }
            .asWGS84()
            .toParseUriResult(uri.toString())

    override suspend fun parseHtml(
        channel: ByteReadChannel,
        pointsFromUri: ImmutableList<Point>,
        log: ILog,
    ): ParseHtmlResult? =
        buildPoints {
            defaultName = pointsFromUri.lastOrNull()?.name

            val pattern = Pattern.compile("""zoom=$Z&amp;center=$LON%2C$LAT""")
            while (true) {
                val line = channel.readUTF8Line() ?: break
                (pattern findPoint line)?.also {
                    points.add(it)
                    break
                }
            }
        }
            .asWGS84()
            .toParseHtmlResult()

    @StringRes
    override val permissionTitleResId = R.string.converter_urbi_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_urbi_loading_indicator_title
}
