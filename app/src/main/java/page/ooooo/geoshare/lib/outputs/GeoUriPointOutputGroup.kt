package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Srs

object GeoUriPointOutputGroup : OutputGroup<Point> {

    object CopyOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatUriString(value, Srs.WGS84, uriQuote))

        @Composable
        override fun label() = stringResource(R.string.conversion_succeeded_copy_geo)

        override fun isEnabled(value: Point) = true
    }

    object ChooserOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.OpenChooser(formatUriString(value, Srs.WGS84, uriQuote))

        @Composable
        override fun label() = stringResource(R.string.conversion_succeeded_share)

        override fun isEnabled(value: Point) = true
    }

    override fun getTextOutput() = null

    override fun getLabelTextOutput() = null

    override fun getSupportingTextOutput() = null

    override fun getActionOutputs() = listOf(
        CopyOutput,
        ChooserOutput,
    )

    override fun getAppOutputs(packageNames: List<String>) =
        emptyList<Output.App<Point>>()

    override fun getChipOutputs() = emptyList<Output.Action<Point, Action>>()

    override fun getChooserOutput() = ChooserOutput

    override fun getRandomOutput() = null

    override fun getAutomations(packageNames: List<String>) = emptyList<Automation>()

    override fun findAutomation(type: Automation.Type, packageName: String?) = null

    fun formatUriString(value: Point, srs: Srs, uriQuote: UriQuote, q: String? = null, zStr: String? = null): String =
        value.toStringPair(srs).let { (latStr, lonStr) ->
            Uri(
                scheme = "geo",
                path = "$latStr,$lonStr",
                queryParams = buildMap {
                    set("q", q ?: "$latStr,$lonStr")
                    zStr?.let { zStr ->
                        set("z", zStr)
                    }
                }.toImmutableMap(),
                uriQuote = uriQuote,
            ).toString()
        }
}
