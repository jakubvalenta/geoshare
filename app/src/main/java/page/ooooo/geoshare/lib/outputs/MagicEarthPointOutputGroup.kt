package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.MagicEarthUrlConverter
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object MagicEarthPointOutputGroup : OutputGroup<Point> {

    object CopyDisplayOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_display, MagicEarthUrlConverter.NAME)
    }

    object CopyDriveToOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatDriveToUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_drive_to, MagicEarthUrlConverter.NAME)
    }

    object CopyDriveViaOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatDriveViaUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_drive_via, MagicEarthUrlConverter.NAME)
    }

    object ChooserDisplayOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.OpenChooser(formatDisplayUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_display, MagicEarthUrlConverter.NAME)
    }

    object ChooserDriveToOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.OpenChooser(formatDriveToUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_drive_to, MagicEarthUrlConverter.NAME)
    }

    object ChooserDriveViaOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.OpenChooser(formatDriveViaUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_drive_via, MagicEarthUrlConverter.NAME)
    }

    override fun getTextOutput(): Output.Text<Point>? = null

    override fun getSupportingTextOutput(): Output.Text<Point>? = null

    override fun getActionOutputs() = listOf(
        CopyDisplayOutput,
        CopyDriveToOutput,
        CopyDriveViaOutput,
        ChooserDisplayOutput,
        ChooserDriveToOutput,
        ChooserDriveViaOutput,
    )

    override fun getAppOutputs(packageNames: List<String>) =
        emptyList<Output.App<Point>>()

    override fun getChipOutputs() = emptyList<Output.Action<Point, Action>>()

    override fun getChooserOutput() = null

    override fun getAutomations(packageNames: List<String>) = emptyList<Automation>()

    override fun findAutomation(type: Automation.Type, packageName: String?) = null

    fun formatDisplayUriString(value: Point, uriQuote: UriQuote, q: String? = null): String =
        Uri(
            scheme = "magicearth",
            path = "//",
            queryParams = buildMap {
                value.apply {
                    if (q == null) {
                        set("show_on_map", "")
                        set("lat", lat)
                        set("lon", lon)
                    } else {
                        if (lat == "0" && lon == "0") {
                            set("open_search", "")
                        } else {
                            set("search_around", "")
                            set("lat", lat)
                            set("lon", lon)
                        }
                        set("q", q)
                    }
                }
            }.toImmutableMap(),
            uriQuote = uriQuote,
        ).toString()

    fun formatDriveToUriString(value: Point, uriQuote: UriQuote): String =
        Uri(
            scheme = "magicearth",
            path = "//",
            queryParams = buildMap {
                set("drive_to", "")
                value.run {
                    set("lat", lat)
                    set("lon", lon)
                }
            }.toImmutableMap(),
            uriQuote = uriQuote,
        ).toString()

    fun formatDriveViaUriString(value: Point, uriQuote: UriQuote): String =
        Uri(
            scheme = "magicearth",
            path = "//",
            queryParams = buildMap {
                set("drive_via", "")
                value.run {
                    set("lat", lat)
                    set("lon", lon)
                }
            }.toImmutableMap(),
            uriQuote = uriQuote,
        ).toString()
}
