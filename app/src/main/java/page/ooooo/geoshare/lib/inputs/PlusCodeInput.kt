package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.formatters.PlusCodeFormatter
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.geo.decodePlusCode
import javax.inject.Inject
import javax.inject.Singleton

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
@Singleton
class PlusCodeInput @Inject constructor() : TextInput, Input.HasRandomUri {
    override val pattern = Regex(
        """(?:^|\s|https://www\.google\.com/maps/place/|https://plus\.codes/)($GLOBAL_CODE)(?:\s|/|$)""",
        RegexOption.IGNORE_CASE,
    )

    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.PLUS_CODE,
        items = listOf(
            InputDocumentationItem.Url(39, "https://plus.codes"),
            InputDocumentationItem.Text(39) {
                stringResource(
                    R.string.example,
                    PlusCodeFormatter.formatPlusCode(WGS84Point(NaivePoint.example)) ?: ""
                )
            },
        ),
    )

    override suspend fun parse(
        data: String,
        match: String,
        prevResult: ParseResult?,
    ) = buildParseResult {
        // URL-decode code string if it was extracted from a URL
        val codeString = data.replace("%2B", "+")

        // Global code
        // e.g. `796RWF8Q+WF`
        decodePlusCode(codeString)?.let {
            points = persistentListOf(
                GCJ02MainlandChinaPoint(it).copy(lat = it.lat?.toScale(6), lon = it.lon?.toScale(6))
            )
            return@buildParseResult
        }

        // Local code (not implemented yet)
        // e.g. `28WR+CW` or `28WR+CW Comstock Park, Michigan`
    }

    override fun genRandomUri(point: Point): String? =
        PlusCodeFormatter.formatPlusCode(point)

    override fun toString() = "PlusCodeInput"

    private companion object {
        /**
         * See https://github.com/google/open-location-code/blob/main/Documentation/Reference/App_Developers.md#supporting-global-codes
         */
        private const val GLOBAL_CODE =
            @Suppress("SpellCheckingInspection") """[23456789C][23456789CFGHJMPQRV][23456789CFGHJMPQRVWX]{6}(?:\+|%2B)[23456789CFGHJMPQRVWX]{2,7}"""
    }
}
