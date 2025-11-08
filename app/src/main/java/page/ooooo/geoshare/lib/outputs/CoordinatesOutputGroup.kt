package page.ooooo.geoshare.lib.outputs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

object CoordinatesOutputGroup : OutputGroup<Position> {

    object TextOutput : Output.Text<Position> {
        override fun getText(value: Position, uriQuote: UriQuote) = formatDegMinSecString(value)
    }

    object LabelTextOutput : Output.PointLabel<Position> {
        @Composable
        override fun getText(value: Position, i: Int, pointCount: Int, uriQuote: UriQuote) = label(value, i, pointCount)
    }

    object SupportingTextOutput : Output.Text<Position> {
        override fun getText(value: Position, uriQuote: UriQuote) = formatParamsString(value)
    }

    object CopyDegMinSecOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatDegMinSecString(value))

        @Composable
        override fun label() = stringResource(R.string.conversion_succeeded_copy_coordinates)

        override fun isEnabled(value: Position) = true
    }

    object CopyDecOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatDecString(value))

        @Composable
        override fun label() = stringResource(R.string.conversion_succeeded_copy_coordinates)

        override fun isEnabled(value: Position) = true
    }

    object CopyDecAutomation : Automation.HasSuccessMessage {
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
                    formatDecString(Position.example),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_succeeded)
    }

    object CopyDegMinSecAutomation : Automation.HasSuccessMessage {
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

    override fun getTextOutput() = TextOutput

    override fun getLabelTextOutput() = LabelTextOutput

    override fun getSupportingTextOutput() = SupportingTextOutput

    override fun getActionOutputs() = listOf(
        CopyDegMinSecOutput,
        CopyDecOutput,
    )

    override fun getAppOutputs(packageNames: List<String>) = emptyList<Output.App<Position>>()

    override fun getChipOutputs() = emptyList<Output.Action<Position, Action>>()

    override fun getChooserOutput() = null

    override fun getAutomations(packageNames: List<String>): List<Automation> = listOf(
        CopyDecAutomation,
        CopyDegMinSecAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.COPY_COORDS_DEC -> CopyDecAutomation
        Automation.Type.COPY_COORDS_NSWE_DEC -> CopyDegMinSecAutomation
        else -> null
    }

    private fun formatDecString(value: Position): String = value.run {
        CoordinatesPointOutputGroup.formatDecString(mainPoint ?: Point(Srs.WGS84))
    }

    fun formatDegMinSecString(value: Position): String = value.run {
        CoordinatesPointOutputGroup.formatDegMinSecString(mainPoint ?: Point(Srs.WGS84))
    }

    private fun formatParamsString(value: Position): String = value.run {
        buildList {
            q.takeUnless { it.isNullOrEmpty() }?.let { q ->
                (mainPoint ?: Point(Srs.WGS84)).toStringPair(Srs.WGS84).let { (latStr, lonStr) ->
                    if (q != "$latStr,$lonStr") {
                        add(q.replace('+', ' '))
                    }
                }
            }
            zStr?.let { zStr ->
                add("z$zStr")
            }
        }.joinToString("\t\t")
    }

    @Composable
    private fun label(value: Position, i: Int, pointCount: Int): String? = value.run {
        CoordinatesPointOutputGroup.label(mainPoint ?: Point(Srs.WGS84), i, pointCount)
    }
}
