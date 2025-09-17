package page.ooooo.geoshare.lib

import kotlin.collections.firstOrNull
import kotlin.collections.map

typealias PartialPoint = Pair<String?, String?>
typealias Point = Pair<String, String>

data class Position(
    val points: List<Point>? = null,
    val q: String? = null,
    val z: String? = null,
) {
    constructor(
        lat: String,
        lon: String,
        q: String? = null,
        z: String? = null,
    ) : this(listOf(lat to lon), q, z)

    fun toCoordsDecString(): String = (points?.firstOrNull() ?: ("0" to "0")).let { (lat, lon) -> "$lat, $lon" }

    fun toParamsString(): String = mutableListOf<String>().apply {
        if (!q.isNullOrEmpty()) {
            val coords = (points?.firstOrNull() ?: ("0" to "0")).let { (lat, lon) -> "$lat,$lon" }
            if (q != coords) {
                add(q)
            }
        }
        if (!z.isNullOrEmpty()) {
            add("z$z")
        }
    }.joinToString(" \u2022 ")

    fun toGeoUriString(uriQuote: UriQuote = DefaultUriQuote()): String {
        val coords = (points?.firstOrNull() ?: ("0" to "0")).let { (lat, lon) -> "$lat,$lon" }
        val queryParams = mutableMapOf("q" to (q ?: coords))
        z?.let { queryParams["z"] = it }
        return Uri(scheme = "geo", path = coords, queryParams = queryParams, uriQuote = uriQuote).toString()
    }

    /**
     * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
     */
    fun toAppleMapsUriString(uriQuote: UriQuote = DefaultUriQuote()): String {
        val queryParams = mutableMapOf<String, String>()
        val firstPoint = points?.firstOrNull()
        if (firstPoint != null) {
            val (lat, lon) = firstPoint
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
        val firstPoint = points?.firstOrNull()
        if (firstPoint != null) {
            val (lat, lon) = firstPoint
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
        points?.firstOrNull()?.let { (lat, lon) ->
            queryParams["lat"] = lat
            queryParams["lon"] = lon
        }
        q?.let { queryParams["q"] = it }
        z?.let { queryParams["zoom"] = it }
        return Uri(scheme = "magicearth", path = "//", queryParams = queryParams, uriQuote = uriQuote).toString()
    }

    fun toNorthSouthWestEastDecCoordsString(): String {
        val (lat, lon) = points?.firstOrNull() ?: ("0" to "0")
        return listOf(
            coordToDeg(lat, "S", "N"),
            coordToDeg(lon, "W", "E"),
        ).joinToString(", ")
    }

    fun toGpx(writer: Appendable, uriQuote: UriQuote = DefaultUriQuote()) = writer.apply {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n")
        append("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\"\n")
        append("     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n")
        append("     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n")
        points?.map { (lat, lon) ->
            append("<wpt lat=\"${uriQuote.encode(lat)}\" lon=\"${uriQuote.encode(lon)}\" />\n")
        }
        append("</gpx>\n")
    }

    private fun coordToDeg(s: String, directionNegative: String, directionPositive: String): String {
        var abs: String
        var direction: String
        if (s.startsWith("-")) {
            abs = s.substring(1)
            direction = directionNegative
        } else {
            abs = s
            direction = directionPositive
        }
        return "$abs\u00B0\u00A0$direction"
    }
}
