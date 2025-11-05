package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.converters.MagicEarthUrlConverter

object MagicEarthOutputGroup : OutputGroup<Position> {
    @Suppress("SpellCheckingInspection")
    const val PACKAGE_NAME = "com.generalmagic.magicearth"

    object CopyDisplayOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_display, MagicEarthUrlConverter.NAME)
    }

    object CopyDriveToOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatDriveToUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_drive_to, MagicEarthUrlConverter.NAME)
    }

    object CopyDriveViaOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatDriveViaUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_drive_via, MagicEarthUrlConverter.NAME)
    }

    object AppDisplayOutput : Output.App<Position> {
        override val packageName = PACKAGE_NAME

        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.OpenApp(PACKAGE_NAME, formatDisplayUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_display, MagicEarthUrlConverter.NAME)
    }

    object AppDriveToOutput : Output.App<Position> {
        override val packageName = PACKAGE_NAME

        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.OpenApp(PACKAGE_NAME, formatDriveToUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_drive_to, MagicEarthUrlConverter.NAME)
    }

    object AppDriveViaOutput : Output.App<Position> {
        override val packageName = PACKAGE_NAME

        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.OpenApp(PACKAGE_NAME, formatDriveToUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_drive_via, MagicEarthUrlConverter.NAME)
    }

    object CopyAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_MAGIC_EARTH_URI
        override val packageName = ""
        override val testTag = null

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(position, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link, MagicEarthUrlConverter.NAME))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    override fun getTextOutput(): Output.Text<Position>? = null

    override fun getSupportingTextOutput(): Output.Text<Position>? = null

    override fun getActionOutputs() = listOf(
        CopyDisplayOutput,
        CopyDriveToOutput,
        CopyDriveViaOutput,
    )

    override fun getAppOutputs(packageNames: List<String>) = listOf(
        AppDisplayOutput,
        AppDriveToOutput,
        AppDriveViaOutput,
    )

    override fun getChipOutputs() = emptyList<Output.Action<Position, Action>>()

    override fun getChooserOutput() = null

    override fun getAutomations(packageNames: List<String>): List<Automation> = listOf(
        CopyAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.COPY_MAGIC_EARTH_URI -> CopyAutomation
        else -> null
    }

    private fun formatDisplayUriString(value: Position, uriQuote: UriQuote): String = value.run {
        MagicEarthPointOutputGroup.formatDisplayUriString(mainPoint ?: Point(), uriQuote, q = q)
    }

    private fun formatDriveToUriString(value: Position, uriQuote: UriQuote): String = value.run {
        MagicEarthPointOutputGroup.formatDriveToUriString(mainPoint ?: Point(), uriQuote)
    }

    private fun formatDriveViaUriString(value: Position, uriQuote: UriQuote): String = value.run {
        MagicEarthPointOutputGroup.formatDriveViaUriString(mainPoint ?: Point(), uriQuote)
    }
}
