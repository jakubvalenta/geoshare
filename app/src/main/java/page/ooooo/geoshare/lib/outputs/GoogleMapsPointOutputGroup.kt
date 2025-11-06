package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Srs
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter

object GoogleMapsPointOutputGroup : OutputGroup<Point> {

    @Immutable
    data class CopyOutput(var srs: Srs) : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatUriString(value, srs, uriQuote))

        @Composable
        override fun label() = when (srs) {
            is Srs.WGS84 ->
                stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsUrlConverter.NAME)

            is Srs.GCJ02 ->
                stringResource(R.string.conversion_succeeded_copy_link_srs, GoogleMapsUrlConverter.NAME, srs.name)
        }
    }

    override fun getTextOutput() = null

    override fun getLabelTextOutput() = null

    override fun getSupportingTextOutput() = null

    override fun getAppOutputs(packageNames: List<String>) = emptyList<Output.App<Point>>()

    override fun getChipOutputs() = emptyList<Output.Action<Point, Action>>()

    override fun getChooserOutput() = null

    override fun getActionOutputs() = listOf(
        CopyOutput(Srs.GCJ02), // TODO Hide if out of China
        CopyOutput(Srs.WGS84),
    )

    override fun getAutomations(packageNames: List<String>) = emptyList<Automation>()

    override fun findAutomation(type: Automation.Type, packageName: String?) = null

    fun formatUriString(value: Point, srs: Srs, uriQuote: UriQuote) = Uri(
        scheme = "https",
        host = "www.google.com",
        path = "/maps",
        queryParams = value.toSrs(srs).run {
            mapOf("q" to "$latStr,$lonStr").toImmutableMap()
        },
        uriQuote = uriQuote,
    ).toString()
}
