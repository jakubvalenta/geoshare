package page.ooooo.geoshare.lib.formatters

import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsUriFormatter @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
) {
    fun formatNavigationUriString(point: Point, uriQuote: UriQuote) = Uri(
        scheme = "google.navigation",
        path = (coordinateConverter.toGCJ02(point).run { // TODO Figure out which coordinate system to use
            latStr?.let { latStr ->
                lonStr?.let { lonStr ->
                    "$latStr,$lonStr"
                }
            } ?: name
        } ?: "0,0").let { "q=$it" },
        uriQuote = uriQuote,
    ).toString()

    fun formatStreetViewUriString(point: Point, uriQuote: UriQuote) = Uri(
        scheme = "google.streetview",
        path = (coordinateConverter.toGCJ02(point).run { // TODO Figure out which coordinate system to use
            latStr?.let { latStr ->
                lonStr?.let { lonStr ->
                    "$latStr,$lonStr"
                }
            }
        } ?: "0,0").let { @Suppress("SpellCheckingInspection") "cbll=$it" },
        uriQuote = uriQuote,
    ).toString()
}
