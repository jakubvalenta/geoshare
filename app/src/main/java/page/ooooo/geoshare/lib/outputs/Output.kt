package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

interface Output {
    val packageNames: List<String>
    fun getPositionText(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String?
    fun getPositionExtraTexts(position: Position, uriQuote: UriQuote = DefaultUriQuote()): List<String>
    fun getPositionUriString(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String?
    fun getPositionExtraUriStrings(position: Position, uriQuote: UriQuote = DefaultUriQuote()): List<String>
    fun getPointText(point: Point, uriQuote: UriQuote = DefaultUriQuote()): String?
    fun getPointExtraTexts(point: Point, uriQuote: UriQuote = DefaultUriQuote()): List<String>
    fun getPointUriStrings(point: Point, uriQuote: UriQuote = DefaultUriQuote()): List<String>
}
