package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

object GeneralOutput : Output {
    override val packageNames = emptyList<String>()

    override fun getMainText(position: Position, uriQuote: UriQuote) = position.toDegMinSecCoordsString()

    override fun getExtraTexts(position: Position, uriQuote: UriQuote) = listOf(
        position.toCoordsDecString(),
        position.toGeoUriString(uriQuote),
    )

    override fun getMainUriString(position: Position, uriQuote: UriQuote) = position.toGeoUriString(uriQuote)

    override fun getExtraUriStrings(point: Point, uriQuote: UriQuote) = emptyList<String>()
}
