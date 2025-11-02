package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

object GeneralOutput : Output {
    override val packageNames = emptyList<String>()

    override fun getPositionText(position: Position, uriQuote: UriQuote) = position.toDegMinSecCoordsString()

    override fun getPositionExtraTexts(position: Position, uriQuote: UriQuote) = listOf(
        position.toCoordsDecString(),
        position.toGeoUriString(uriQuote),
    )

    override fun getPositionUriString(position: Position, uriQuote: UriQuote) = position.toGeoUriString(uriQuote)

    override fun getPointText(point: Point, uriQuote: UriQuote) = point.toDegMinSecCoordsString()

    override fun getPointExtraTexts(point: Point, uriQuote: UriQuote) = getPointUriStrings(point, uriQuote)

    override fun getPointUriStrings(point: Point, uriQuote: UriQuote) =
        listOf(point.toGeoUriString(uriQuote = uriQuote))
}
