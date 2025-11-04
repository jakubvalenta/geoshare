package page.ooooo.geoshare.lib.outputs

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Automation
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.AppleMapsUrlConverter

/**
 * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
 */
object AppleMapsOutput : Output {

    object CopyLinkAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_APPLE_MAPS_URI
        override val packageName = null
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

    override fun getText(position: Position, uriQuote: UriQuote) = null

    override fun getText(point: Point, uriQuote: UriQuote) = null

    override fun getActions(position: Position, uriQuote: UriQuote) = listOf<Output.Item<Action>>(
        Output.Item(Action.Copy(formatUriString(position, uriQuote))) {
            stringResource(R.string.conversion_succeeded_copy_link, AppleMapsUrlConverter.NAME)
        },
    )

    override fun getActions(point: Point, uriQuote: UriQuote) = listOf<Output.Item<Action>>(
        Output.Item(Action.Copy(formatUriString(point, uriQuote))) {
            stringResource(R.string.conversion_succeeded_copy_link, AppleMapsUrlConverter.NAME)
        },
        Output.Item(Action.OpenChooser(formatUriString(point, uriQuote))) {
            stringResource(R.string.conversion_succeeded_open_app, AppleMapsUrlConverter.NAME)
        },
    )

    override fun getAutomations(context: Context): List<Automation> = listOf(
        CopyLinkAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?) =
        if (type == Automation.Type.COPY_APPLE_MAPS_URI) CopyLinkAutomation else null

    override fun getChips(position: Position, uriQuote: UriQuote) = emptyList<Output.Item<Action>>()

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
