package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.formatters.PlusCodeFormatter
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.geo.decodePlusCode

/**
 * See https://plus.codes/
 */
object PlusCodeInput : Input {
    /**
     * See https://github.com/google/open-location-code/blob/main/Documentation/Reference/App_Developers.md#supporting-global-codes
     */
    private const val GLOBAL_CODE =
        @Suppress("SpellCheckingInspection") """[23456789C][23456789CFGHJMPQRV][23456789CFGHJMPQRVWX]{6}\+[23456789CFGHJMPQRVWX]{2,7}"""

    override val uriPattern = Regex("""($GLOBAL_CODE)(?:\s|$)""", RegexOption.IGNORE_CASE)

    override val documentation = InputDocumentation(
        id = InputDocumentationId.PLUS_CODE,
        nameResId = R.string.converter_plus_code_name,
        items = listOf(
            InputDocumentationItem.Text(39) {
                stringResource(
                    R.string.example, PlusCodeFormatter.formatPlusCode(WGS84Point.example) ?: ""
                )
            },
        ),
    )

    override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = buildParseUriResult {
        uri.run {
            // Use Uri.toString() instead of Uri.path, so that the '+' character is not replaced with space
            val input = toString()

            // Global code
            // e.g. `796RWF8Q+WF`
            uriPattern.matchEntire(input)?.groupOrNull()?.let { codeString ->
                decodePlusCode(codeString)?.let {
                    points = persistentListOf(
                        WGS84Point(it).copy(lat = it.lat?.toScale(6), lon = it.lon?.toScale(6))
                    )
                    return@run
                }
            }

            // TODO Local code (with or without locality)
            // e.g. `28WR+CW` or `28WR+CW Comstock Park, Michigan`
        }
    }
}
