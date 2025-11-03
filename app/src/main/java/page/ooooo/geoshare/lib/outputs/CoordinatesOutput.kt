package page.ooooo.geoshare.lib.outputs

import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

object CoordinatesOutput : Output {
    override fun getText(position: Position, uriQuote: UriQuote) = position.toDegMinSecCoordsString()

    override fun getText(point: Point, uriQuote: UriQuote) = point.toDegMinSecCoordsString()

    override fun getActions(position: Position, uriQuote: UriQuote) = listOf<Output.LabeledAction<Output.Action>>(
        Output.LabeledAction(Output.Action.Copy(position.toDegMinSecCoordsString())) {
            stringResource(R.string.conversion_succeeded_copy_coordinates)
        },
        Output.LabeledAction(Output.Action.Copy(position.toCoordsDecString())) {
            stringResource(R.string.conversion_succeeded_copy_coordinates)
        },
        Output.LabeledAction(Output.Action.Copy(position.toGeoUriString(uriQuote))) {
            stringResource(R.string.conversion_succeeded_copy_geo)
        },
    )

    override fun getActions(point: Point, uriQuote: UriQuote) = listOf<Output.LabeledAction<Output.Action>>(
        Output.LabeledAction(Output.Action.Copy(point.toDegMinSecCoordsString())) {
            stringResource(R.string.conversion_succeeded_copy_coordinates)
        },
        Output.LabeledAction(Output.Action.Copy(point.toCoordsDecString())) {
            stringResource(R.string.conversion_succeeded_copy_coordinates)
        },
        Output.LabeledAction(Output.Action.Copy(point.toGeoUriString(uriQuote = uriQuote))) {
            stringResource(R.string.conversion_succeeded_copy_geo)
        },
        Output.LabeledAction(Output.Action.OpenChooser(point.toGeoUriString(uriQuote = uriQuote))) {
            stringResource(R.string.conversion_succeeded_share)
        },
    )

    override fun getChips(position: Position, uriQuote: UriQuote) = listOf<Output.LabeledAction<Output.Action>>(
        Output.LabeledAction(Output.Action.Copy(position.toGeoUriString(uriQuote))) {
            stringResource(R.string.conversion_succeeded_copy_geo)
        },
    )
}
