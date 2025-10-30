package page.ooooo.geoshare.lib.outputs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*

object CoordinatesOutputGroup : OutputGroup<Position> {

    object TextOutput : Output.Text<Position> {
        override fun getText(value: Position, uriQuote: UriQuote) =
            formatDegMinSecString(value)
    }

    object LabelTextOutput : Output.ComposableText<Position> {
        @Composable
        override fun getText(value: Position, num: Int, uriQuote: UriQuote) =
            num.takeIf { it > 1 }?.let { num ->
                stringResource(R.string.conversion_succeeded_point_number, num)
            }
    }

    object SupportingTextOutput : Output.Text<Position> {
        override fun getText(value: Position, uriQuote: UriQuote) =
            formatParamsString(value)
    }

    object CopyDegMinSecOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatDegMinSecString(value))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_coordinates)
    }

    object CopyDecOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatDecString(value))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_coordinates)
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
        CoordinatesPointOutputGroup.formatDecString(mainPoint ?: Point())
    }

    fun formatDegMinSecString(value: Position): String = value.run {
        CoordinatesPointOutputGroup.formatDegMinSecString(mainPoint ?: Point())
    }

    private fun formatParamsString(value: Position): String = value.run {
        buildList {
            mainPoint?.desc.takeUnless { it.isNullOrEmpty() }?.let { description ->
                add(description)
            }
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
