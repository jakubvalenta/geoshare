package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.lib.IntentTools.Companion.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote

object GoogleMapsOutput : Output {
    override val packageNames = listOf(
        GOOGLE_MAPS_PACKAGE_NAME,
        @Suppress("SpellCheckingInspection")
        "us.spotco.maps",
    )

    override fun getMainText(position: Position, uriQuote: UriQuote) = getMainUriString(position, uriQuote)

    override fun getExtraTexts(position: Position, uriQuote: UriQuote) = emptyList<String>()

    /**
     * See https://developers.google.com/maps/documentation/urls/get-started
     */
    override fun getMainUriString(position: Position, uriQuote: UriQuote) = Uri(
        scheme = "https",
        host = "www.google.com",
        path = "/maps",
        queryParams = buildMap {
            position.apply {
                mainPoint?.apply {
                    set("q", "$lat,$lon")
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
