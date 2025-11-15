package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.inputs.MagicEarthInput

object MagicEarthPointOutputGroup : OutputGroup<Point> {

    object CopyDisplayOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_display, MagicEarthInput.NAME)

        override fun isEnabled(value: Point) = true
    }

    object CopyNavigateToOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatNavigateToUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_drive_to, MagicEarthInput.NAME)

        override fun isEnabled(value: Point) = true
    }

    object CopyNavigateViaOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatNavigateViaUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_drive_via, MagicEarthInput.NAME)

        override fun isEnabled(value: Point) = true
    }

    object ChooserDisplayOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.OpenChooser(formatDisplayUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_display, MagicEarthInput.NAME)

        override fun isEnabled(value: Point) = true
    }

    object ChooserNavigateToOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.OpenChooser(formatNavigateToUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_navigate_to, MagicEarthInput.NAME)

        override fun isEnabled(value: Point) = true
    }

    object ChooserNavigateViaOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.OpenChooser(formatNavigateViaUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_navigate_via, MagicEarthInput.NAME)

        override fun isEnabled(value: Point) = true
    }

    override fun getTextOutput() = null

    override fun getNameOutput() = null

    override fun getDescriptionOutput() = null

    override fun getActionOutputs() = listOf(
        CopyDisplayOutput,
        CopyNavigateToOutput,
        CopyNavigateViaOutput,
        ChooserDisplayOutput,
        ChooserNavigateToOutput,
        ChooserNavigateViaOutput,
    )

    override fun getAppOutputs(packageNames: List<String>) = emptyList<Output.App<Point>>()

    override fun getChipOutputs() = emptyList<Output.Action<Point, Action>>()

    override fun getChooserOutput() = null

    override fun getRandomOutput() = null

    override fun getAutomations(packageNames: List<String>) = emptyList<Automation>()

    override fun findAutomation(type: Automation.Type, packageName: String?) = null

    fun formatDisplayUriString(value: Point, uriQuote: UriQuote, q: String? = null): String =
        Uri(
            scheme = "magicearth",
            path = "//",
            queryParams = buildMap {
                value.toStringPair(Srs.WGS84).let { (latStr, lonStr) ->
                    if (q == null) {
                        set("show_on_map", "")
                        set("lat", latStr)
                        set("lon", lonStr)
                        value.name?.let { name ->
                            set("name", name)
                        }
                    } else {
                        if (value.lat == 0.0 && value.lon == 0.0) {
                            set("open_search", "")
                        } else {
                            set("search_around", "")
                            set("lat", latStr)
                            set("lon", lonStr)
                        }
                        set("q", q)
                    }
                }
            }.toImmutableMap(),
            uriQuote = uriQuote,
        ).toString()

    fun formatNavigateToUriString(value: Point, uriQuote: UriQuote): String =
        Uri(
            scheme = "magicearth",
            path = "//",
            queryParams = buildMap {
                set("navigate_to", "")
                value.toStringPair(Srs.WGS84).let { (latStr, lonStr) ->
                    set("lat", latStr)
                    set("lon", lonStr)
                }
            }.toImmutableMap(),
            uriQuote = uriQuote,
        ).toString()

    fun formatNavigateViaUriString(value: Point, uriQuote: UriQuote): String =
        Uri(
            scheme = "magicearth",
            path = "//",
            queryParams = buildMap {
                set("navigate_via", "")
                value.toStringPair(Srs.WGS84).let { (latStr, lonStr) ->
                    set("lat", latStr)
                    set("lon", lonStr)
                }
            }.toImmutableMap(),
            uriQuote = uriQuote,
        ).toString()
}
