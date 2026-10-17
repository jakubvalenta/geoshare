package page.ooooo.geoshare.data.geo

enum class Source {
    /**
     * Example: JSON response from a third-party API
     */
    API,

    /**
     * Example: random point, empty point
     */
    GENERATED,

    /**
     * Example: current device location
     */
    GPS_SENSOR,

    /**
     * Example: Geohash, QuadTiles, base64-encoded coordinates
     */
    HASH,

    /**
     * Example: DIV tag, SCRIPT tag
     */
    HTML_NOT_SEMANTIC,

    /**
     * Example: META tag, HTML5 data attribute
     */
    HTML_SEMANTIC,

    /**
     * Example: a property of the Window object
     */
    JAVASCRIPT_STATE,

    /**
     * Example: URI path "/@{lat},{lon}", URI query param "center"
     */
    MAP_CENTER,

    /**
     * Example: string "{deg} {min} {sec} N, {deg} {min} {sec} E"
     */
    TEXT,

    /**
     * Example: URI query parameters "lat", "lon", and "q"
     */
    URI,
}
