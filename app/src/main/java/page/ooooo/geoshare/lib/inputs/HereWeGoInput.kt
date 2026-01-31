package page.ooooo.geoshare.lib.inputs

import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toLatLonZPoint
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints
import page.ooooo.geoshare.lib.point.toParseUriResult
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object HereWeGoInput : Input {
    private const val SIMPLIFIED_BASE64 = """[A-Za-z0-9+/]+=*"""

    override val uriPattern = Regex("""(?:https?://)?(?:share|wego)\.here\.com/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.HERE_WEGO,
        nameResId = R.string.converter_here_wego_name,
        items = listOf(
            InputDocumentationItem.Url(20, "https://share.here.com/l/"),
            InputDocumentationItem.Url(20, "https://share.here.com/p/"),
            InputDocumentationItem.Url(20, "https://wego.here.com/"),
            InputDocumentationItem.Url(20, "https://wego.here.com/p/"),
        ),
    )

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun parseUri(uri: Uri): ParseUriResult? =
        buildPoints {
            uri.run {
                val parts = uri.pathParts.drop(1)
                val firstPart = parts.firstOrNull() ?: return@run
                if (firstPart == "") {
                    (Regex("""$LAT,$LON,$Z""") match queryParams["map"])?.toLatLonZPoint()?.also { points.add(it) }
                } else {
                    val secondPart = parts.getOrNull(1)
                    if (secondPart != null) {
                        if (firstPart == "l") {
                            (LAT_LON_PATTERN match secondPart)?.toLatLonPoint()?.also { points.add(it) }
                        } else if (firstPart == "p") {
                            (Regex("""[a-z]-($SIMPLIFIED_BASE64)""") match secondPart)
                                ?.groupOrNull()
                                ?.let { encoded -> Base64.decode(encoded).decodeToString() }
                                ?.let { decoded ->
                                    (Regex("""(?:lat=|"latitude":)$LAT""") find decoded)
                                        ?.doubleGroupOrNull()
                                        ?.let { lat ->
                                            (Regex("""(?:lon=|"longitude":)$LON""") find decoded)
                                                ?.doubleGroupOrNull()
                                                ?.let { lon ->
                                                    NaivePoint(lat, lon)
                                                }
                                        }
                                }
                                ?.also { points.add(it) }
                        }
                    }
                }

                (Regex(""".*,$Z""") match queryParams["map"])?.doubleGroupOrNull()?.also { defaultZ = it }
            }
        }
            .asWGS84()
            .toParseUriResult()
}
