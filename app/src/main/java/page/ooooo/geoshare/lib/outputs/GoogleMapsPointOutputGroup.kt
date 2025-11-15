package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.inputs.GoogleMapsInput
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Srs

object GoogleMapsPointOutputGroup : OutputGroup<Point> {

    object CopyDisplayOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsInput.NAME)

        override fun isEnabled(value: Point) = true
    }

    object CopyNavigateToOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatNavigateToUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_drive_to, GoogleMapsInput.NAME)

        override fun isEnabled(value: Point) = true
    }

    object CopyStreetViewOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.Copy(formatStreetViewUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_street_view, GoogleMapsInput.NAME)

        override fun isEnabled(value: Point) = true
    }

    object ChooserNavigateToOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.OpenChooser(formatNavigateToUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_navigate_to, GoogleMapsInput.NAME)

        override fun isEnabled(value: Point) = true
    }

    object ChooserStreetViewOutput : Output.Action<Point, Action> {
        override fun getAction(value: Point, uriQuote: UriQuote) =
            Action.OpenChooser(formatStreetViewUriString(value, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_street_view, GoogleMapsInput.NAME)

        override fun isEnabled(value: Point) = true
    }

    override fun getTextOutput() = null

    override fun getLabelTextOutput() = null

    override fun getSupportingTextOutput() = null

    override fun getActionOutputs() = listOf(
        CopyDisplayOutput,
        CopyNavigateToOutput,
        CopyStreetViewOutput,
        ChooserNavigateToOutput,
        ChooserStreetViewOutput,
    )

    override fun getAppOutputs(packageNames: List<String>) = emptyList<Output.App<Point>>()

    override fun getChipOutputs() = emptyList<Output.Action<Point, Action>>()

    override fun getChooserOutput() = null

    override fun getRandomOutput() = null

    override fun getAutomations(packageNames: List<String>) = emptyList<Automation>()

    override fun findAutomation(type: Automation.Type, packageName: String?) = null

    fun formatDisplayUriString(value: Point, uriQuote: UriQuote): String = Uri(
        scheme = "https",
        host = "www.google.com",
        path = "/maps",
        queryParams = value.toStringPair(Srs.GCJ02).let { (latStr, lonStr) ->
            mapOf("q" to "$latStr,$lonStr").toImmutableMap()
        },
        uriQuote = uriQuote,
    ).toString()

    private fun formatNavigateToUriString(value: Point, uriQuote: UriQuote): String = Uri(
        scheme = "google.navigation",
        path = value.toStringPair(Srs.GCJ02).let { (latStr, lonStr) -> "$latStr,$lonStr" }.let { q ->
            "q=$q"
        },
        uriQuote = uriQuote,
    ).toString()

    private fun formatStreetViewUriString(value: Point, uriQuote: UriQuote): String = Uri(
        scheme = "google.streetview",
        path = value.toStringPair(Srs.GCJ02).let { (latStr, lonStr) -> "$latStr,$lonStr" }.let { coords ->
            @Suppress("SpellCheckingInspection")
            "cbll=$coords"
        },
        uriQuote = uriQuote,
    ).toString()
}
