package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.AppleMapsUrlConverter

/**
 * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
 */
object AppleMapsOutputManager : OutputManager {

    object CopyLinkAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_APPLE_MAPS_URI
        override val packageName = ""
        override val testTag = null

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatUriString(position, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link, AppleMapsUrlConverter.NAME))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    object CopyLinkOutput : Output.Action {
        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatUriString(position, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link, AppleMapsUrlConverter.NAME)
    }

    object PointCopyLinkOutput : Output.PointAction {
        override fun getAction(point: Point, uriQuote: UriQuote) =
            Action.Copy(formatUriString(point, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link, AppleMapsUrlConverter.NAME)
    }

    override fun getOutputs(packageNames: List<String>) = listOf(
        CopyLinkOutput,
        PointCopyLinkOutput,
    )

    override fun getAutomations(packageNames: List<String>) = listOf(
        CopyLinkAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.COPY_APPLE_MAPS_URI -> CopyLinkAutomation
        else -> null
    }

    private fun formatUriString(position: Position, uriQuote: UriQuote) = Uri(
        scheme = "https",
        host = "maps.apple.com",
        path = "/",
        queryParams = buildMap {
            position.apply {
                mainPoint?.apply {
                    set("ll", "$lat,$lon")
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
        host = "maps.apple.com",
        path = "/",
        queryParams = buildMap {
            point.apply {
                set("ll", "$lat,$lon")
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()
}
