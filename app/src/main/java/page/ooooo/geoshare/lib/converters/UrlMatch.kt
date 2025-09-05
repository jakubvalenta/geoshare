package page.ooooo.geoshare.lib.converters

import page.ooooo.geoshare.lib.Position

data class UrlMatch(val position: Position?, val supportsHtmlParsing: Boolean) {
    fun union(other: UrlMatch): UrlMatch = UrlMatch(
        position = if (position == null) {
            other.position
        } else if (other.position == null) {
            position
        } else {
            position.union(position)
        },
        supportsHtmlParsing = supportsHtmlParsing or other.supportsHtmlParsing,
    )
}
