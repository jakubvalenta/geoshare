package page.ooooo.geoshare.lib

data class Position(
    val lat: String? = null,
    val lon: String? = null,
    val q: String? = null,
    val z: String? = null,
) {
    companion object {
        fun fromGeoUriString(uriString: String, uriQuote: UriQuote = DefaultUriQuote()): Position? {
            if (!uriString.startsWith("geo:")) {
                return null
            }
            val schemeAndRest = uriString.split(":", limit = 2)
            val hostAndQuery = schemeAndRest.getOrNull(1)?.split("?", limit = 2)
            val host = hostAndQuery?.get(0)
            val query = hostAndQuery?.getOrNull(1)
            var lat: String? = null
            var lon: String? = null
            var q: String? = null
            var z: String? = null
            host?.split(",")?.let {
                lat = it.getOrNull(0)
                lon = it.getOrNull(1)
            }
            getUrlQueryParams(query, uriQuote).let {
                q = it["q"]
                z = it["z"]
            }
            return Position(lat, lon, q, z)
        }
    }

    fun toCoordsDecString(): String = "${lat ?: 0}, ${lon ?: 0}"

    fun toParamsString(): String = listOfNotNull(
        q?.takeIf { it.isNotEmpty() && q != "${lat ?: 0},${lon ?: 0}" },
        z?.takeIf { it.isNotEmpty() }?.let { "z$it" },
    ).joinToString(" \u2022 ")

    fun toGeoUriString(uriQuote: UriQuote = DefaultUriQuote()): String = "${lat ?: 0},${lon ?: 0}".let { coords ->
        formatUrl(
            "geo",
            coords,
            mapOf(
                "q" to (q ?: coords),
                "z" to z,
            ),
            uriQuote,
        )
    }

    fun toMagicEarthUriString(uriQuote: UriQuote = DefaultUriQuote()): String = formatUrl(
        "magicearth",
        "//",
        mapOf("lat" to lat, "lon" to lon, "q" to q, "zoom" to z),
        uriQuote,
    )

    fun toNorthSouthWestEastDecCoordsString(): String = listOf(
        coordToDeg(lat, "S", "N"),
        coordToDeg(lon, "W", "E"),
    ).joinToString(", ")

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
