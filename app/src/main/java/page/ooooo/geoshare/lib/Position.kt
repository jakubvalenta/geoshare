package page.ooooo.geoshare.lib

data class Position(
    val lat: String? = null,
    val lon: String? = null,
    val q: String? = null,
    val z: String? = null,
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
