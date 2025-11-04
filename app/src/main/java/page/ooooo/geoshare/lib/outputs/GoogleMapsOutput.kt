package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Automation
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter

/**
 * See https://developers.google.com/maps/documentation/urls/get-started
 */
object GoogleMapsOutput : Output {
    // TODO GOOGLE_MAPS_PACKAGE_NAME
    @Suppress("SpellCheckingInspection")
    // TODO "us.spotco.maps"

    object CopyLinkAutomation : Automation.HasSuccessMessage {
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

    override fun getText(position: Position, uriQuote: UriQuote) = null

    override fun getText(point: Point, uriQuote: UriQuote) = null

    override fun getSupportingText(position: Position, uriQuote: UriQuote) = null

    override fun getActions(position: Position, packageNames: List<String>, uriQuote: UriQuote) =
        listOf<Output.Item<Action>>(
            Output.Item(Action.Copy(formatUriString(position, uriQuote))) {
                stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsUrlConverter.NAME)
            },
        )

    override fun getActions(point: Point, uriQuote: UriQuote) = listOf<Output.Item<Action>>(
        Output.Item(Action.Copy(formatUriString(point, uriQuote))) {
            stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsUrlConverter.NAME)
        },
    )

    override fun getAutomations(packageNames: List<String>): List<Automation> = listOf(
        CopyLinkAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.COPY_GOOGLE_MAPS_URI -> CopyLinkAutomation
        else -> null
    }

    override fun getChips(position: Position, uriQuote: UriQuote) = listOf<Output.Item<Action>>(
        Output.Item(Action.Copy(formatUriString(position, uriQuote))) {
            stringResource(R.string.conversion_succeeded_copy_google_maps)
        }
    )

    private fun formatUriString(position: Position, uriQuote: UriQuote) = Uri(
        scheme = "https",
        host = "www.google.com",
        path = "/maps",
        queryParams = buildMap {
            position.apply {
                mainPoint?.apply {
                    set("q", "$lat,$lon")
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

    private fun formatUriString(point: Point, uriQuote: UriQuote) = Uri(
        scheme = "https",
        host = "www.google.com",
        path = "/maps",
        queryParams = buildMap {
            point.apply {
                set("q", "$lat,$lon")
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()
}
