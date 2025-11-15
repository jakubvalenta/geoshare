package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Srs

object GeoUriPointOutputGroup : OutputGroup<Point> {

    object CopyOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatUriString(value, Srs.WGS84, nameDisabled = false, uriQuote = uriQuote))

        @Composable
        override fun label() = stringResource(R.string.conversion_succeeded_copy_geo)

        override fun isEnabled(value: Point) = true
    }

    object ChooserOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.OpenChooser(formatUriString(value, Srs.WGS84, nameDisabled = false, uriQuote = uriQuote))

        @Composable
        override fun label() = stringResource(R.string.conversion_succeeded_share)

        override fun isEnabled(value: Point) = true
    }

    override fun getTextOutput() = null

    override fun getNameOutput() = null

    override fun getDescriptionOutput() = null

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

    fun formatUriString(
        value: Point,
        srs: Srs,
        nameDisabled: Boolean,
        uriQuote: UriQuote,
        q: String? = null,
        zStr: String? = null,
    ): String = buildString {
        // Use custom string builder instead of Uri.toString(), because we want to allow custom chars in query params
        value.toStringPair(srs).let { (latStr, lonStr) -> "$latStr,$lonStr" }.let { coordsStr ->
            append("geo:")
            append(Uri.formatPath(coordsStr, uriQuote = uriQuote))
            buildMap {
                if (q != null) {
                    set("q", q)
                } else if (value.lat != 0.0 && value.lon != 0.0) {
                    if (!nameDisabled && value.name != null) {
                        set("q", "$coordsStr(${value.name})")
                    } else {
                        set("q", coordsStr)
                    }
                }
                if (zStr != null) {
                    set("z", zStr)
                }
            }
                .takeIf { it.isNotEmpty() }
                ?.let { Uri.formatQueryParams(it.toImmutableMap(), allow = ",()", uriQuote = uriQuote) }
                ?.let { append("?$it") }
        }
    }
}
