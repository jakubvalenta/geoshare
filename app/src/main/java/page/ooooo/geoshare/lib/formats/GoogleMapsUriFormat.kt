package page.ooooo.geoshare.lib.formats

import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.point.Point

object GoogleMapsUriFormat {
    fun formatNavigationUriString(point: Point, uriQuote: UriQuote) = Uri(
        scheme = "google.navigation",
        path = (point.toGCJ02().run {
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
        path = (point.toGCJ02().run {
            latStr?.let { latStr ->
                lonStr?.let { lonStr ->
                    "$latStr,$lonStr"
                }
            }
        } ?: "0,0").let { @Suppress("SpellCheckingInspection") "cbll=$it" },
        uriQuote = uriQuote,
    ).toString()
}
