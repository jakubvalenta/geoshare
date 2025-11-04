package page.ooooo.geoshare.lib.outputs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Automation
import page.ooooo.geoshare.lib.Action
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.toDegMinSec
import page.ooooo.geoshare.lib.toScale
import kotlin.math.abs

object CoordinatesOutputManager : OutputManager {

    object CopyCoordsDecAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_COORDS_DEC
        override val packageName = ""
        override val testTag = "geoShareUserPreferenceAutomationCopyCoordsDec"

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatDecString(position))

        @Composable
        override fun Label() {
            Column {
                Text(
                    stringResource(R.string.conversion_succeeded_copy_coordinates)
                )
                Text(
                    formatDegMinSecString(Position.example),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_succeeded)
    }

    object CopyCoordsDegMinSecAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_COORDS_NSWE_DEC
        override val packageName = ""
        override val testTag = null

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatDegMinSecString(position))

        @Composable
        override fun Label() {
            Column {
                Text(
                    stringResource(R.string.conversion_succeeded_copy_coordinates)
                )
                Text(
                    formatDegMinSecString(Position.example),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_succeeded)
    }

    override fun getOutputs(position: Position, packageNames: List<String>, uriQuote: UriQuote) = buildList {
        val degMinSec = formatDegMinSecString(position)
        add(Output.Text(degMinSec))
        add(Output.SupportingText(formatParamsString(position)))
        add(Output.Action(Action.Copy(degMinSec)) {
            stringResource(R.string.conversion_succeeded_copy_coordinates)
        })
        add(Output.Action(Action.Copy(formatDecString(position))) {
            stringResource(R.string.conversion_succeeded_copy_coordinates)
        })
        position.points?.forEachIndexed { i, point ->
            add(Output.PointAction(i, Action.Copy(formatDegMinSecString(point))) {
                stringResource(R.string.conversion_succeeded_copy_coordinates)
            })
            add(Output.PointAction(i, Action.Copy(formatDecString(point))) {
                stringResource(R.string.conversion_succeeded_copy_coordinates)
            })
        }
    }

    override fun getAutomations(packageNames: List<String>): List<Automation> = listOf(
        CopyCoordsDecAutomation,
        CopyCoordsDegMinSecAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.COPY_COORDS_DEC -> CopyCoordsDecAutomation
        Automation.Type.COPY_COORDS_NSWE_DEC -> CopyCoordsDegMinSecAutomation
        else -> null
    }

    private fun formatDecString(position: Position): String = position.run {
        formatDecString(mainPoint ?: Point())
    }

    private fun formatDecString(point: Point): String = point.run {
        "$lat, $lon"
    }

    fun formatDegMinSecString(position: Position): String = position.run {
        formatDegMinSecString(mainPoint ?: Point())
    }

    private fun formatDegMinSecString(point: Point): String = point.run {
        (lat.toDoubleOrNull() ?: 0.0).toDegMinSec().let { (deg, min, sec) ->
            "${abs(deg)}°\u00a0$min′\u00a0${sec.toScale(5)}″\u00a0${if (deg < 0) "S" else "N"}, "
        } +
                (lon.toDoubleOrNull() ?: 0.0).toDegMinSec().let { (deg, min, sec) ->
                    "${abs(deg)}°\u00a0$min′\u00a0${sec.toScale(5)}″\u00a0${if (deg < 0) "W" else "E"}"
                }
    }

    private fun formatParamsString(position: Position): String = position.run {
        buildList {
            q.takeUnless { it.isNullOrEmpty() }?.let { q ->
                (mainPoint ?: Point("0", "0")).let { (lat, lon) ->
                    val coords = "$lat,$lon"
                    if (q != coords) {
                        add(q.replace('+', ' '))
                    }
                }
            }
            z.takeUnless { it.isNullOrEmpty() }?.let { z ->
                add("z$z")
            }
        }.joinToString("\t\t")
    }
}
