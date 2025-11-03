package page.ooooo.geoshare.lib.outputs

import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.converters.AppleMapsUrlConverter

/**
 * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
 */
object AppleMapsOutput : Output {
    override fun getText(position: Position, uriQuote: UriQuote) = null

    override fun getText(point: Point, uriQuote: UriQuote) = null

    override fun getActions(position: Position, uriQuote: UriQuote) = listOf<Output.LabeledAction<Output.Action>>(
        Output.LabeledAction(Output.Action.Copy(formatUriString(position, uriQuote))) {
            stringResource(R.string.conversion_succeeded_copy_link, AppleMapsUrlConverter.NAME)
        },
    )

    override fun getActions(point: Point, uriQuote: UriQuote) = listOf<Output.LabeledAction<Output.Action>>(
        Output.LabeledAction(Output.Action.Copy(formatUriString(point, uriQuote))) {
            stringResource(R.string.conversion_succeeded_copy_link, AppleMapsUrlConverter.NAME)
        },
        Output.LabeledAction(Output.Action.OpenChooser(formatUriString(point, uriQuote))) {
            stringResource(R.string.conversion_succeeded_open_app, AppleMapsUrlConverter.NAME)
        },
    )

    override fun getChips(position: Position, uriQuote: UriQuote) = emptyList<Output.LabeledAction<Output.Action>>()

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
