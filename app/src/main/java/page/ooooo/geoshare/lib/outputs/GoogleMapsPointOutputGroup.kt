package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Srs
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter

object GoogleMapsPointOutputGroup : OutputGroup<Point> {

    object CopyOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatUriString(value, uriQuote))

        @Composable
        override fun label(value: Point) = copyLabel(value)
    }

    override fun getTextOutput() = null

    override fun getLabelTextOutput() = null

    override fun getSupportingTextOutput() = null

    override fun getAppOutputs(packageNames: List<String>) = emptyList<Output.App<Point>>()

    override fun getChipOutputs() = emptyList<Output.Action<Point, Action>>()

    override fun getChooserOutput() = null

    override fun getActionOutputs() = listOf(
        CopyOutput,
    )

    override fun getAutomations(packageNames: List<String>) = emptyList<Automation>()

    override fun findAutomation(type: Automation.Type, packageName: String?) = null

    fun formatUriString(value: Point, uriQuote: UriQuote) = Uri(
        scheme = "https",
        host = "www.google.com",
        path = "/maps",
        queryParams = value.toSrs(Srs.GCJ02).run {
            mapOf("q" to "$latStr,$lonStr").toImmutableMap()
        },
        uriQuote = uriQuote,
    ).toString()

    @Composable
    fun copyLabel(value: Point): String = if (value.isInChina()) {
        stringResource(R.string.conversion_succeeded_copy_link_srs, GoogleMapsUrlConverter.NAME, Srs.GCJ02.name)
    } else {
        stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsUrlConverter.NAME)
    }
}
