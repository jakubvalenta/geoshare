package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.formatters.PlusCodeFormatter
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.decodePlusCode

/**
 * Plus Codes input.
 *
 * To make sure Plus Codes pasted from Google Maps, which use the GCJ02 Mainland China coordinate system, are accurate,
 * this input produces [GCJ02MainlandChinaPoint] points. This means Plus Codes within Mainland China pasted from an app
 * other than Google Maps will probably be inaccurate, but we assume there are few apps other than Google Maps that use
 * Plus Codes.
 *
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
                    R.string.example,
                    PlusCodeFormatter.formatPlusCode(GCJ02MainlandChinaPoint(NaivePoint.example)) ?: ""
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
                        GCJ02MainlandChinaPoint(it).copy(lat = it.lat?.toScale(6), lon = it.lon?.toScale(6))
                    )
                    return@run
                }
            }

            // TODO Local code (with or without locality)
            // e.g. `28WR+CW` or `28WR+CW Comstock Park, Michigan`
        }
    }
}
