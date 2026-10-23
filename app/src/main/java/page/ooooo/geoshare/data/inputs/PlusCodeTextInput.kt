package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.extensions.toScale
import page.ooooo.geoshare.data.formatters.PlusCodeFormatter
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.WGS84Point

/**
 * Plus Code as text.
 *
 * Plus Codes are also supported in [PlusCodeUriInput] and [GoogleMapsUriInput].
 *
 * See https://plus.codes/
 */
object PlusCodeTextInput : BasicOfflineInput, Input.HasPattern, Input.HasRandomSource {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = PlusCodeInputGroup
    override val changelog = persistentListOf(
        InputChangelogItem.Text(39) {
            stringResource(
                R.string.example,
                PlusCodeFormatter.formatPlusCode(WGS84Point(NaivePoint.example)).orEmpty()
            )
        },
    )

    // Require the Plus Code to be either the only text in the source, or to be surrounded by whitespace, because it
    // feels like the Plus Code regex could easily match a part of an unrelated URI, for example.
    override val pattern = Regex(
        """(?:^|\s)(${PlusCodeParser.GLOBAL_CODE})(?:\s|$)""",
        RegexOption.IGNORE_CASE,
    )

    override fun parse(match: String, resources: Resources) =
        parseResult {
            // Global Plus Code
            // e.g. `796RWF8Q+WF`
            PlusCodeParser.decodePlusCode(match)?.let {
                points = persistentListOf(WGS84Point(it.copy(lat = it.lat?.toScale(6), lon = it.lon?.toScale(6))))
                return@parseResult
            }

            // Local Plus Code (not implemented yet)
            // e.g. `28WR+CW` or `28WR+CW Comstock Park, Michigan`
        }

    override fun genRandomSource(point: Point) =
        PlusCodeFormatter.formatPlusCode(point)

    override fun toString() = "PlusCodeTextInput"
}
