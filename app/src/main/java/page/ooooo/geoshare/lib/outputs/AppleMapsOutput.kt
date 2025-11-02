package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote

object AppleMapsOutput : Output {
    override val packageNames = emptyList<String>()

    override fun getMainText(position: Position, uriQuote: UriQuote) = getMainUriString(position, uriQuote)

    override fun getExtraTexts(position: Position, uriQuote: UriQuote) = emptyList<String>()

    /**
     * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
     */
    override fun getMainUriString(position: Position, uriQuote: UriQuote) = Uri(
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

    override fun getExtraUriStrings(point: Point, uriQuote: UriQuote) = emptyList<String>()
}
