package page.ooooo.geoshare.lib.inputs

import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.geo.decodeOpenLocationCode

/**
 * Also known as Plus Codes.
 *
 * See https://en.wikipedia.org/wiki/Open_Location_Code
 */
object OpenLocationCodeInput : Input {
    private const val CHAR = @Suppress("SpellCheckingInspection") """[2-9CFGHJMPQRVWXcfghjmpqrvwx]"""
    private const val CODE = """$CHAR{2,8}\+$CHAR{2,7}"""

    override val uriPattern = Regex("""($CODE)(?: $URI_REST)?""")

    override val documentation = InputDocumentation(
        id = InputDocumentationId.OPEN_LOCATION_CODE,
        nameResId = R.string.converter_open_location_code_name,
        items = listOf(
            InputDocumentationItem.Text(39, @Composable { "" }), // TODO Example
        ),
    )

    override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = buildParseUriResult {
        uri.run {
            // Plus Code (full or short, with or without locality)
            // e.g. `28WR+CW Comstock Park, Michigan`
            uriPattern.matchEntire(path)?.let { m ->
                m.groupOrNull(1)?.let { code ->
                    val locality = m.groupOrNull(2)
                    decodeOpenLocationCode(code, locality).let {
                        points = persistentListOf(
                            WGS84Point(it).copy(lat = it.lat?.toScale(6), lon = it.lon?.toScale(6))
                        )
                        return@run
                    }
                }
            }
        }
    }
}
