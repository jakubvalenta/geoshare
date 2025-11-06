package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.IntentTools.Companion.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter

/**
 * See https://developers.google.com/maps/documentation/urls/get-started
 */
object GoogleMapsOutputGroup : OutputGroup<Position> {

    val PACKAGE_NAMES = listOf(
        GOOGLE_MAPS_PACKAGE_NAME,
        @Suppress("SpellCheckingInspection")
        "us.spotco.maps",
    )

    object CopyOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsUrlConverter.NAME)
    }

    object ChipOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_google_maps)
    }

    @Immutable
    data class AppWGSOutput(override val packageName: String) : Output.App<Position> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.OpenApp(packageName, formatUriString(value, uriQuote))

        @Composable
        override fun label(app: IntentTools.App) =
            stringResource(R.string.conversion_succeeded_open_app_srs, app.label, "WGS 84")
    }

    object CopyAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_GOOGLE_MAPS_URI
        override val packageName = ""
        override val testTag = null

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatUriString(position, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsUrlConverter.NAME))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    override fun getTextOutput() = null

    override fun getLabelTextOutput() = null

    override fun getSupportingTextOutput() = null

    override fun getActionOutputs() = listOf(
        CopyOutput,
    )

    override fun getAppOutputs(packageNames: List<String>) = buildList {
        PACKAGE_NAMES.filter { it in packageNames }.forEach { packageName ->
            add(AppWGSOutput(packageName))
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

    private fun formatUriString(value: Position, uriQuote: UriQuote) = Uri(
        scheme = "https",
        host = "www.google.com",
        path = "/maps",
        queryParams = buildMap {
            value.apply {
                mainPoint?.apply {
                    set("q", "$lat,$lon")
                } ?: q?.let { q ->
                    set("q", q)
                }
                z?.let { z ->
                    set("z", z)
                }
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()
}
