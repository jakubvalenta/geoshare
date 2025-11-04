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
object GoogleMapsOutputManager : OutputManager {
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

    override fun getOutputs(position: Position, packageNames: List<String>, uriQuote: UriQuote) = buildList {
        add(Output.Action(Action.Copy(formatUriString(position, uriQuote))) {
            stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsUrlConverter.NAME)
        })
        position.points?.forEachIndexed { i, point ->
            add(Output.PointAction(i, Action.Copy(formatUriString(point, uriQuote))) {
                stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsUrlConverter.NAME)
            })
        }
        add(Output.Chip(Action.Copy(formatUriString(position, uriQuote))) {
            stringResource(R.string.conversion_succeeded_copy_google_maps)
        })
    }

    override fun getAutomations(packageNames: List<String>): List<Automation> = listOf(
        CopyLinkAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.COPY_GOOGLE_MAPS_URI -> CopyLinkAutomation
        else -> null
    }

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
