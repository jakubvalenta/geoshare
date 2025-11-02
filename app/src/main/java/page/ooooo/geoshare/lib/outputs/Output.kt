package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

interface Output {
    val packageNames: List<String>
    fun getMainText(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String?
    fun getExtraTexts(position: Position, uriQuote: UriQuote = DefaultUriQuote()): List<String>
    fun getMainUriString(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String?
    fun getExtraUriStrings(point: Point, uriQuote: UriQuote = DefaultUriQuote()): List<String>
}
