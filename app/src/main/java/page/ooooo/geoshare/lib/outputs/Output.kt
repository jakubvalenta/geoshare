package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

interface Output {
    data class Item(val value: String, val label: @Composable () -> String)

    val packageNames: List<String>

    fun getPositionText(position: Position, uriQuote: UriQuote = DefaultUriQuote()): Item?
    fun getPositionExtraTexts(position: Position, uriQuote: UriQuote = DefaultUriQuote()): List<Item>
    fun getPositionUriString(position: Position, uriQuote: UriQuote = DefaultUriQuote()): Item?
    fun getPositionExtraUriStrings(position: Position, uriQuote: UriQuote = DefaultUriQuote()): List<Item>
    fun getPointText(point: Point, uriQuote: UriQuote = DefaultUriQuote()): Item?
    fun getPointExtraTexts(point: Point, uriQuote: UriQuote = DefaultUriQuote()): List<Item>
    fun getPointUriStrings(point: Point, uriQuote: UriQuote = DefaultUriQuote()): List<Item>
}
