package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.extensions.toLonLatZPoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 2GIS / Urbi input.
 *
 * We call it Urbi, because 2GIS starts with a number, so it couldn't be used as a class name.
 */
@Singleton
class UrbiUriInput @Inject constructor(
    private val urbiHtmlInput: UrbiHtmlInput,
) : UriInput, Input.HasRandomUri {
    override val pattern =
        Regex("""((?:https?://)?(?:www\.)?(?:(?:go|maps)\.)?(?:2gis|urbi|urbi-[a-z]{2})(?:\.[a-z]{2,3})?\.[a-z]{2,3}/$URI_REST)""")
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.URBI,
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

    override suspend fun parse(
        data: Uri,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        data.run {
            // Marker
            // https://maps.urbi.ae/dubai/geo/{lon}%2C{lat}?m={lon},{lat}/{z}
            Regex("""$LON,$LAT/$Z""").matchEntire(queryParams["m"])?.toLonLatZPoint(Source.URI)?.let {
                points = persistentListOf(WGS84Point(it))
                return@run
            }

            val z = Z_PATTERN.matchEntire(queryParams["zoom"])?.doubleGroupOrNull()

            // Point
            // https://maps.urbi.ae/dubai/geo/{lon}%2C{lat}
            pathParts.firstNotNullOfOrNull { LON_LAT_PATTERN.matchEntire(it)?.toLonLatPoint(Source.URI) }?.let {
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

            nextStep = NextStep.NextInput(urbiHtmlInput, match)
        }
    }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://maps.urbi.ae/dubai/geo/{lon}%2C{lat}?m={lon}%2C{lat}%2F{z}")

    override fun toString() = "UrbiUriInput"
}
