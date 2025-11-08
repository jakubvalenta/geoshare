package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.inputs.MagicEarthInput

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/, although it's outdated, for
 * example the drive_via parameter doesn't work anymore but trial and error showed that navigate_via works.
 */
object MagicEarthOutputGroup : OutputGroup<Position> {

    @Suppress("SpellCheckingInspection")
    const val PACKAGE_NAME = "com.generalmagic.magicearth"

    object CopyDisplayOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_display, MagicEarthInput.NAME)

        override fun isEnabled(value: Position) = true
    }

    object CopyNavigateToOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatNavigateToUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_drive_to, MagicEarthInput.NAME)

        override fun isEnabled(value: Position) = true
    }

    object CopyNavigateViaOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatNavigateViaUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_drive_via, MagicEarthInput.NAME)

        override fun isEnabled(value: Position) = true
    }

    @Immutable
    data class AppDisplayOutput(override val packageName: String) : Output.App<Position> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.OpenApp(packageName, formatDisplayUriString(value, uriQuote))

        @Composable
        override fun label(app: IntentTools.App) =
            stringResource(R.string.conversion_succeeded_open_app_display, app.label)

        override fun isEnabled(value: Position) = true
    }

    @Immutable
    data class AppNavigateToOutput(override val packageName: String) : Output.App<Position> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.OpenApp(packageName, formatNavigateToUriString(value, uriQuote))

        @Composable
        override fun label(app: IntentTools.App) =
            stringResource(R.string.conversion_succeeded_open_app_navigate_to, app.label)

        override fun isEnabled(value: Position) = true
    }

    @Immutable
    data class AppNavigateViaOutput(override val packageName: String) : Output.App<Position> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.OpenApp(packageName, formatNavigateViaUriString(value, uriQuote))

        @Composable
        override fun label(app: IntentTools.App) =
            stringResource(R.string.conversion_succeeded_open_app_navigate_via, app.label)

        override fun isEnabled(value: Position) = true
    }

    object CopyAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_MAGIC_EARTH_URI
        override val packageName = ""
        override val testTag = null

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(position, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link, MagicEarthInput.NAME))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    override fun getTextOutput() = null

    override fun getLabelTextOutput() = null

    override fun getSupportingTextOutput() = null

    override fun getActionOutputs() = listOf(
        CopyDisplayOutput,
        CopyNavigateToOutput,
        CopyNavigateViaOutput,
    )

    override fun getAppOutputs(packageNames: List<String>) = buildList {
        PACKAGE_NAME.takeIf { it in packageNames }?.let { packageName ->
            add(AppDisplayOutput(packageName))
            add(AppNavigateToOutput(packageName))
            add(AppNavigateViaOutput(packageName))
        }
    }

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
        MagicEarthPointOutputGroup.formatDisplayUriString(mainPoint ?: Point(Srs.WGS84), uriQuote, q = q)
    }

    private fun formatNavigateToUriString(value: Position, uriQuote: UriQuote): String = value.run {
        MagicEarthPointOutputGroup.formatNavigateToUriString(mainPoint ?: Point(Srs.WGS84), uriQuote)
    }

    private fun formatNavigateViaUriString(value: Position, uriQuote: UriQuote): String = value.run {
        MagicEarthPointOutputGroup.formatNavigateViaUriString(mainPoint ?: Point(Srs.WGS84), uriQuote)
    }
}
