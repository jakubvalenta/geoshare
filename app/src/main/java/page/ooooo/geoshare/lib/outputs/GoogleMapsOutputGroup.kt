package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.IntentTools.Companion.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.inputs.GoogleMapsInput
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

/**
 * See https://developers.google.com/maps/documentation/urls/get-started
 */
object GoogleMapsOutputGroup : OutputGroup<Position> {

    val PACKAGE_NAMES = listOf(
        GOOGLE_MAPS_PACKAGE_NAME,
        @Suppress("SpellCheckingInspection")
        "us.spotco.maps",
    )

    object CopyDisplayOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsInput.NAME)

        override fun isEnabled(value: Position) = true
    }

    object CopyNavigateToOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatNavigateToUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_drive_to, GoogleMapsInput.NAME)

        override fun isEnabled(value: Position) = true
    }

    object CopyDisplayStreetViewOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatDisplayStreetViewUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_street_view, GoogleMapsInput.NAME)

        override fun isEnabled(value: Position) = value.mainPoint != null
    }

    object ChipOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(value, uriQuote))

        @Composable
        override fun label() = stringResource(R.string.conversion_succeeded_copy_google_maps)

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
    data class AppDisplayStreetViewOutput(override val packageName: String) : Output.App<Position> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.OpenApp(packageName, formatDisplayStreetViewUriString(value, uriQuote))

        @Composable
        override fun label(app: IntentTools.App) =
            stringResource(R.string.conversion_succeeded_open_app_display, app.label)

        override fun isEnabled(value: Position) = value.mainPoint != null
    }

    object CopyAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_GOOGLE_MAPS_URI
        override val packageName = ""
        override val testTag = null

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(position, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsInput.NAME))
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
        CopyDisplayStreetViewOutput,
    )

    override fun getAppOutputs(packageNames: List<String>) = buildList {
        PACKAGE_NAMES.filter { it in packageNames }.forEach { packageName ->
            add(AppNavigateToOutput(packageName))
            add(AppDisplayStreetViewOutput(packageName))
        }
    }

    override fun getChipOutputs() = listOf(
        ChipOutput,
    )

    override fun getChooserOutput() = null

    override fun getAutomations(packageNames: List<String>) = listOf(
        CopyAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.COPY_GOOGLE_MAPS_URI -> CopyAutomation
        else -> null
    }

    private fun formatDisplayUriString(value: Position, uriQuote: UriQuote): String = Uri(
        scheme = "https",
        host = "www.google.com",
        path = "/maps",
        queryParams = buildMap {
            value.apply {
                mainPoint?.toStringPair(Srs.GCJ02)?.let { (latStr, lonStr) ->
                    set("q", "$latStr,$lonStr")
                } ?: q?.let { q ->
                    set("q", q)
                }
                zStr?.let { zStr ->
                    set("z", zStr)
                }
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()

    private fun formatNavigateToUriString(value: Position, uriQuote: UriQuote): String = Uri(
        scheme = "google.navigation",
        path = value.run {
            mainPoint?.toStringPair(Srs.GCJ02)?.let { (latStr, lonStr) -> "$latStr,$lonStr" }
                ?: q
                ?: "0,0"
        }.let { q ->
            "q=$q"
        },
        uriQuote = uriQuote,
    ).toString()

    private fun formatDisplayStreetViewUriString(value: Position, uriQuote: UriQuote): String = Uri(
        scheme = "google.streetview",
        path = value.run {
            mainPoint?.toStringPair(Srs.GCJ02)?.let { (latStr, lonStr) -> "$latStr,$lonStr" }
                ?: "0,0"
        }.let { coords ->
            @Suppress("SpellCheckingInspection")
            "cbll=$coords&cbp=0,30,0,0,-15"
        },
        uriQuote = uriQuote,
    ).toString()
}
