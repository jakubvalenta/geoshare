package page.ooooo.geoshare.lib.formatters

import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Point

object GoogleMapsUriFormatter {
    fun formatNavigationUriString(point: Point, uriQuote: UriQuote) = point.run {
        Uri(
            scheme = "google.navigation",
            path = (latStr?.let { latStr ->
                lonStr?.let { lonStr ->
                    "$latStr,$lonStr"
                }
            } ?: name ?: "0,0").let { "q=$it" },
            uriQuote = uriQuote,
        ).toString()
    }

    fun formatStreetViewUriString(point: Point, uriQuote: UriQuote) = point.run {
        Uri(
            scheme = "google.streetview",
            path = (latStr?.let { latStr ->
                lonStr?.let { lonStr ->
                    "$latStr,$lonStr"
                }
            } ?: "0,0").let { @Suppress("SpellCheckingInspection") "cbll=$it" },
            uriQuote = uriQuote,
        ).toString()
    }
}
