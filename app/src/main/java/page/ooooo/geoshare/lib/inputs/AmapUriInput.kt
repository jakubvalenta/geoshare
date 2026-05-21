package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonNamePoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.GCJ02GreaterChinaAndTaiwanPoint
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmapUriInput @Inject constructor(
    override val uriQuote: UriQuote,
) : UriInput, Input.HasRandomUri {
    override val pattern = Regex("""((?:https?://)?wb\.amap\.com/$URI_REST)""")
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.AMAP,
        items = listOf(
            InputDocumentationItem.Url(27, "https://wb.amap.com/"),
        ),
    )

    override suspend fun parse(data: Uri, match: String, prevResult: ParseResult?) = parseResult {
        data.run {
            // Query param p
            // https://wb.amap.com/?p=<id>,<lat>,<lon>,<name>
            Regex("""\w+,$LAT,$LON,?(?:$NAME_PARAM)?.*""").matchEntire(queryParams["p"])?.toLatLonNamePoint(Source.URI)
                ?.let {
                    points = persistentListOf(GCJ02GreaterChinaAndTaiwanPoint(it))
                    return@run
                }

            // Query param q
            // https://wb.amap.com/?q=<lat>,<lon>,<name>
            Regex("""$LAT,$LON,?(?:$NAME_PARAM)?.*""").matchEntire(queryParams["q"])?.toLatLonNamePoint(Source.URI)
                ?.let {
                    points = persistentListOf(GCJ02GreaterChinaAndTaiwanPoint(it))
                    return@run
                }
        }
    }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://wb.amap.com/?q={lat}%2C{lon}")

    override fun toString() = "AmapUriInput"
}
