package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.AppleMapsUrlConverter

/**
 * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
 */
object AppleMapsOutputGroup : OutputGroup<Position> {

    object CopyOutput : Output.Action<Position, Action> {
        override fun getAction(value: Position, uriQuote: UriQuote) =
            Action.Copy(formatUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link, AppleMapsUrlConverter.NAME)
    }

    object CopyAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_APPLE_MAPS_URI
        override val packageName = ""
        override val testTag = null

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatUriString(position, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link, AppleMapsUrlConverter.NAME))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    override fun getTextOutput(): Output.Text<Position>? = null

    override fun getSupportingTextOutput(): Output.Text<Position>? = null

    override fun getActionOutputs() = listOf(
        CopyOutput,
    )

    override fun getAppOutputs(packageNames: List<String>) = emptyList<Output.App<Position>>()

    override fun getChipOutputs() = emptyList<Output.Action<Position, Action>>()

    override fun getChooserOutput() = null

    override fun getAutomations(packageNames: List<String>) = listOf(
        CopyAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.COPY_APPLE_MAPS_URI -> CopyAutomation
        else -> null
    }

    private fun formatUriString(value: Position, uriQuote: UriQuote) = Uri(
        scheme = "https",
        host = "maps.apple.com",
        path = "/",
        queryParams = buildMap {
            value.apply {
                mainPoint?.apply {
                    set("ll", "$lat,$lon")
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
