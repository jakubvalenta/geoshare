package page.ooooo.geoshare.lib.outputs

import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.R

object CoordinatesOutput : Output {
    override val packageNames = emptyList<String>()

    override fun getPositionText(position: Position, uriQuote: UriQuote) =
        Output.Item(position.toDegMinSecCoordsString()) {
            stringResource(R.string.conversion_succeeded_copy_coordinates)
        }

    override fun getPositionExtraTexts(position: Position, uriQuote: UriQuote) = listOf(
        Output.Item(position.toCoordsDecString()) {
            stringResource(R.string.conversion_succeeded_copy_coordinates)
        },
        Output.Item(position.toGeoUriString(uriQuote)) {
            stringResource(R.string.conversion_succeeded_copy_geo)
        },
    )

    override fun getPositionChipTexts(position: Position, uriQuote: UriQuote) = listOf(
        Output.Item(position.toGeoUriString(uriQuote)) {
            stringResource(R.string.conversion_succeeded_copy_geo)
        },
    )

    override fun getPositionUriString(position: Position, uriQuote: UriQuote) =
        Output.Item(position.toGeoUriString(uriQuote)) {
            stringResource(R.string.conversion_succeeded_share)
        }

    override fun getPositionExtraUriStrings(position: Position, uriQuote: UriQuote) = emptyList<Output.Item>()

    override fun getPointText(point: Point, uriQuote: UriQuote) =
        Output.Item(point.toDegMinSecCoordsString()) {
            stringResource(R.string.conversion_succeeded_copy_coordinates)
        }

    override fun getPointExtraTexts(point: Point, uriQuote: UriQuote) = listOf(
        Output.Item(point.toCoordsDecString()) {
            stringResource(R.string.conversion_succeeded_copy_coordinates)
        },
        Output.Item(point.toGeoUriString(uriQuote = uriQuote)) { stringResource(R.string.conversion_succeeded_copy_geo) },
    )

    override fun getPointUriStrings(point: Point, uriQuote: UriQuote) = listOf(
        Output.Item(point.toGeoUriString(uriQuote = uriQuote)) { stringResource(R.string.conversion_succeeded_share) },
    )
}
