package page.ooooo.geoshare.lib.outputs

import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.IntentTools.Companion.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter

/**
 * See https://developers.google.com/maps/documentation/urls/get-started
 */
object GoogleMapsOutput : Output {
    override val packageNames = listOf(
        GOOGLE_MAPS_PACKAGE_NAME,
        @Suppress("SpellCheckingInspection")
        "us.spotco.maps",
    )

    override fun getPositionText(position: Position, uriQuote: UriQuote) =
        Output.Item(formatPositionUriString(position, uriQuote)) {
            stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsUrlConverter.NAME)
        }

    override fun getPositionExtraTexts(position: Position, uriQuote: UriQuote) = emptyList<Output.Item>()

    override fun getPositionChipTexts(position: Position, uriQuote: UriQuote) = listOf(
        Output.Item(formatPositionUriString(position, uriQuote)) {
            stringResource(R.string.conversion_succeeded_copy_google_maps)
        },
    )

    override fun getPositionUriString(position: Position, uriQuote: UriQuote) =
        Output.Item(formatPositionUriString(position, uriQuote)) {
            stringResource(R.string.conversion_succeeded_open_app, GoogleMapsUrlConverter.NAME)
        }

    override fun getPositionExtraUriStrings(position: Position, uriQuote: UriQuote) = emptyList<Output.Item>()

    override fun getPointText(point: Point, uriQuote: UriQuote) = null

    override fun getPointExtraTexts(point: Point, uriQuote: UriQuote) = listOf(
        Output.Item(formatPointUriString(point, uriQuote)) {
            stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsUrlConverter.NAME)
        },
    )

    override fun getPointUriStrings(point: Point, uriQuote: UriQuote) = listOf(
        Output.Item(formatPointUriString(point, uriQuote)) {
            stringResource(R.string.conversion_succeeded_open_app, GoogleMapsUrlConverter.NAME)
        },
    )

    private fun formatPositionUriString(position: Position, uriQuote: UriQuote) = Uri(
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

    private fun formatPointUriString(point: Point, uriQuote: UriQuote) = Uri(
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
