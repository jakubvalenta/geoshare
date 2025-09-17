package page.ooooo.geoshare.lib

data class Position(
    val lat: String? = null,
    val lon: String? = null,
    val q: String? = null,
    val z: String? = null,
    val points: List<Pair<String, String>>? = null,
) {
    fun toCoordsDecString(): String = "${lat ?: 0}, ${lon ?: 0}"

    fun toParamsString(): String = listOfNotNull(
        q?.takeIf { it.isNotEmpty() && q != "${lat ?: 0},${lon ?: 0}" },
        z?.takeIf { it.isNotEmpty() }?.let { "z$it" },
    ).joinToString(" \u2022 ")

    fun toGeoUriString(uriQuote: UriQuote = DefaultUriQuote()): String {
        val coords = "${lat ?: 0},${lon ?: 0}"
        val queryParams = mutableMapOf("q" to (q ?: coords))
        z?.let { queryParams["z"] = z }
        return Uri(scheme = "geo", path = coords, queryParams = queryParams, uriQuote = uriQuote).toString()
    }

    /**
     * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
     */
    fun toAppleMapsUriString(uriQuote: UriQuote = DefaultUriQuote()): String {
        val queryParams = mutableMapOf<String, String>()
        if (lat != null && lon != null) {
            queryParams["ll"] = "$lat,$lon"
        } else if (q != null) {
            queryParams["q"] = q
        }
        z?.let { queryParams["z"] = z }
        return Uri(
            scheme = "https",
            host = "maps.apple.com",
            "/",
            queryParams = queryParams,
            uriQuote = uriQuote,
        ).toString()
    }

    /**
     * See https://developers.google.com/maps/documentation/urls/get-started
     */
    fun toGoogleMapsUriString(uriQuote: UriQuote = DefaultUriQuote()): String {
        val queryParams = mutableMapOf<String, String>()
        if (lat != null && lon != null) {
            queryParams["q"] = "$lat,$lon"
        } else if (q != null) {
            queryParams["q"] = q
        }
        z?.let { queryParams["z"] = z }
        return Uri(
            scheme = "https",
            host = "www.google.com",
            "/maps",
            queryParams = queryParams,
            uriQuote = uriQuote,
        ).toString()
    }

    /**
     * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/
     */
    fun toMagicEarthUriString(uriQuote: UriQuote = DefaultUriQuote()): String {
        val queryParams = mutableMapOf<String, String>()
        lat?.let { queryParams["lat"] = lat }
        lon?.let { queryParams["lon"] = lon }
        q?.let { queryParams["q"] = q }
        z?.let { queryParams["zoom"] = z }
        return Uri(scheme = "magicearth", path = "//", queryParams = queryParams, uriQuote = uriQuote).toString()
    }

    fun toNorthSouthWestEastDecCoordsString(): String = listOf(
        coordToDeg(lat, "S", "N"),
        coordToDeg(lon, "W", "E"),
    ).joinToString(", ")

    fun toGpx(uriQuote: UriQuote = DefaultUriQuote()): String = StringBuilder().apply {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n")
        append("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\"\n")
        append("     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n")
        append("     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n")
        points?.map { (lat, lon) ->
            append("<wpt lat=\"${uriQuote.encode(lat)}\" lon=\"${uriQuote.encode(lon)}\" />\n")
        }
        append("</gpx>\n")
    }.toString()

    private fun coordToDeg(s: String?, directionNegative: String, directionPositive: String): String {
        var abs: String
        var direction: String
        if (s == null) {
            abs = "0"
            direction = directionPositive
        } else if (s.startsWith("-")) {
            abs = s.substring(1)
            direction = directionNegative
        } else {
            abs = s
            direction = directionPositive
        }
        return "$abs\u00B0\u00A0$direction"
    }
}
