package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote

/**
 * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
 */
object AppleMapsOutput : Output {
    override val packageNames = emptyList<String>()

    override fun getPositionText(position: Position, uriQuote: UriQuote) = getPositionUriString(position, uriQuote)

    override fun getPositionExtraTexts(position: Position, uriQuote: UriQuote) = emptyList<String>()

    override fun getPositionUriString(position: Position, uriQuote: UriQuote) =
        formatPositionUriString(position, uriQuote)

    override fun getPositionExtraUriStrings(position: Position, uriQuote: UriQuote) = emptyList<String>()

    override fun getPointText(point: Point, uriQuote: UriQuote) = null

    override fun getPointExtraTexts(point: Point, uriQuote: UriQuote) = getPointUriStrings(point, uriQuote)

    override fun getPointUriStrings(point: Point, uriQuote: UriQuote) = listOf(formatPointUriString(point, uriQuote))

    private fun formatPositionUriString(position: Position, uriQuote: UriQuote) = Uri(
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

    private fun formatPointUriString(point: Point, uriQuote: UriQuote) = Uri(
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
