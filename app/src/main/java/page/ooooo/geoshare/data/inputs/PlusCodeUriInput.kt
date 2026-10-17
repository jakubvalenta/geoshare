package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.data.extensions.toScale
import page.ooooo.geoshare.data.formatters.PlusCodeFormatter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.uri.Uri
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plus Code as URI.
 *
 * Plus Codes are also supported in [PlusCodeTextInput] and [GoogleMapsUriInput].
 *
 * See https://plus.codes/
 */
@Singleton
class PlusCodeUriInput @Inject constructor(
    private val uriQuote: UriQuote,
) : BasicOfflineInput, Input.HasPattern, Input.HasRandomUri {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.PLUS_CODE
    override val changelog = persistentListOf(
        InputChangelogItem.Url(39, "https://plus.codes"),
    )

    override val pattern = Regex(
        """((?:https?://)?plus\.codes/${PlusCodeParser.GLOBAL_CODE})""",
        RegexOption.IGNORE_CASE,
    )

    override fun parse(match: String, resources: Resources) =
        Uri.parse(match, uriQuote).run {
            parseResult {
                // Replace " " with "+", because URI parsing replaced "+" with " ", so we need to revert it.
                val codeString = pathParts.getOrNull(1)?.replace(' ', '+') ?: return@parseResult

                // Global Plus Code
                // e.g. `796RWF8Q+WF`
                PlusCodeParser.decodePlusCode(codeString)?.let {
                    points = persistentListOf(WGS84Point(it.copy(lat = it.lat?.toScale(6), lon = it.lon?.toScale(6))))
                    return@parseResult
                }

                // Local Plus Code (not implemented yet)
                // e.g. `28WR+CW` or `28WR+CW Comstock Park, Michigan`
            }
        }

    override fun genRandomUri(point: Point) =
        PlusCodeFormatter.formatPlusCode(point)?.let {
            "https://plus.codes/${uriQuote.encode(it)}/"
        }

    override fun toString() = "PlusCodeUriInput"
}
