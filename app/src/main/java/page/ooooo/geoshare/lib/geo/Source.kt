package page.ooooo.geoshare.lib.geo

enum class Source {
    /**
     * Example: JSON
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
     * Example: META tag
     */
    HTML,

    /**
     * Example: SCRIPT tag
     */
    JAVASCRIPT,

    /**
     * Example: URI path '/@{lat},{lon}', URI query param 'center'
     */
    MAP_CENTER,

    /**
     * Example: string '{deg} {min} {sec} N, {deg} {min} {sec} E'
     */
    TEXT,

    /**
     * Example: URI query param 'll', URI query param 'q'
     */
    URI,
}
