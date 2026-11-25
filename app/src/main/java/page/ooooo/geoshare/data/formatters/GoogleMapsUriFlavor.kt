package page.ooooo.geoshare.data.formatters

/**
 * Format of a Google Maps display URI that a receiving app understands.
 */
enum class GoogleMapsUriFlavor {
    /**
     * `https://www.google.com/maps/place/{lat},{lon}`, for apps that don't decode the URI and don't support search
     * queries. Points without coordinates can't be formatted.
     */
    PLACE_PATH,

    /**
     * `https://maps.google.com/?q={lat}%2C{lon}` or `https://maps.google.com/?q={name}`.
     */
    Q_PARAM,
}
