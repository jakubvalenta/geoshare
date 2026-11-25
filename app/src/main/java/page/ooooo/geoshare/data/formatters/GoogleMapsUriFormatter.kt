package page.ooooo.geoshare.data.formatters

import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.uri.DefaultUriQuote
import page.ooooo.geoshare.data.uri.Uri
import page.ooooo.geoshare.data.uri.UriQuote

object GoogleMapsUriFormatter {
    /**
     * Returns null if the point can't be formatted in the given [flavor], e.g. a point without coordinates in
     * [GoogleMapsUriFlavor.PLACE_PATH].
     */
    fun formatDisplayUriString(
        point: Point,
        flavor: GoogleMapsUriFlavor = GoogleMapsUriFlavor.Q_PARAM,
        uriQuote: UriQuote = DefaultUriQuote,
    ): String? =
        when (flavor) {
            GoogleMapsUriFlavor.PLACE_PATH -> UriFormatter.formatUriString(
                point = point,
                // Keep the comma unencoded, because apps that need this flavor don't decode the URI
                coordsUriTemplate = "https://www.google.com/maps/place/{lat},{lon}",
                uriQuote = uriQuote,
            )

            GoogleMapsUriFlavor.Q_PARAM -> UriFormatter.formatUriString(
                point = point,
                // Use https://maps.google.com/?q= instead of https://www.google.com/maps/search/?api=1&q=, because
                // Telegram doesn't show a map preview when using the API link.
                coordsUriTemplate = "https://maps.google.com/?q={lat}%2C{lon}",
                nameUriTemplate = "https://maps.google.com/?q={q}",
                uriQuote = uriQuote,
            )
        }

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
            } ?: "0,0").let { "cbll=$it" },
            uriQuote = uriQuote,
        ).toString()
    }
}
