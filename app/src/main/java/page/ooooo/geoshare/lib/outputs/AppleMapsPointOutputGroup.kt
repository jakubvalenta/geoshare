package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.AppleMapsUrlConverter

/**
 * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
 */
object AppleMapsPointOutputGroup : OutputGroup<Point> {

    object CopyOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link, AppleMapsUrlConverter.NAME)
    }

    override fun getTextOutput() = null

    override fun getLabelTextOutput() = null

    override fun getSupportingTextOutput() = null

    override fun getActionOutputs() = listOf(
        CopyOutput,
    )

    override fun getAppOutputs(packageNames: List<String>) = emptyList<Output.App<Point>>()

    override fun getChipOutputs() = emptyList<Output.Action<Point, Action>>()

    override fun getChooserOutput() = null

    override fun getAutomations(packageNames: List<String>) = emptyList<Automation>()

    override fun findAutomation(type: Automation.Type, packageName: String?) = null

    private fun formatUriString(value: Point, uriQuote: UriQuote) = Uri(
        scheme = "https",
        host = "maps.apple.com",
        path = "/",
        queryParams = value.toStringPair(Srs.WGS84).let { (latStr, lonStr) ->
            mapOf("ll" to "$latStr,$lonStr").toImmutableMap()
        },
        uriQuote = uriQuote,
    ).toString()
}
