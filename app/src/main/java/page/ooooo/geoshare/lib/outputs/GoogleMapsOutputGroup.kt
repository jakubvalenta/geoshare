package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.IntentTools.Companion.GOOGLE_MAPS_PACKAGE_NAME
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
        override fun label() = stringResource(R.string.conversion_succeeded_copy_google_maps)
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

    override fun getAppOutputs(packageNames: List<String>) = emptyList<Output.App<Position>>()

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
}
