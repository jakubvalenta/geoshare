package page.ooooo.geoshare.lib

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

    val mainPoint: Point? get() = points?.lastOrNull()

    fun toCoordsDecString(): String = (mainPoint ?: ("0" to "0")).let { (lat, lon) -> "$lat, $lon" }

    fun toParamsString(): String = mutableListOf<String>().apply {
        q.takeUnless { it.isNullOrEmpty() }?.let { q ->
            (mainPoint ?: ("0" to "0")).let { (lat, lon) ->
                val coords = "$lat,$lon"
                if (q != coords) {
                    add(q.replace('+', ' '))
                }
            }
        }
        z.takeUnless { it.isNullOrEmpty() }?.let { z ->
            add("z$z")
        }
    }.joinToString(" \u2022 ")

    fun toGeoUriString(uriQuote: UriQuote = DefaultUriQuote()): String =
        (mainPoint ?: ("0" to "0")).let { (lat, lon) -> "$lat,$lon" }.let { coords ->
            Uri(
                scheme = "geo",
                path = coords,
                queryParams = mutableMapOf<String, String>().apply {
                    set("q", q ?: coords)
                    z?.let { z ->
                        set("z", z)
                    }
                },
                uriQuote = uriQuote,
            ).toString()
        }

    /**
     * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
     */
    fun toAppleMapsUriString(uriQuote: UriQuote = DefaultUriQuote()): String = Uri(
        scheme = "https",
        host = "maps.apple.com",
        path = "/",
        queryParams = mutableMapOf<String, String>().apply {
            mainPoint?.let { (lat, lon) ->
                set("ll", "$lat,$lon")
            } ?: q?.let { q ->
                set("q", q)
            }
            z?.let { z ->
                set("z", z)
            }
        },
        uriQuote = uriQuote,
    ).toString()

    /**
     * See https://developers.google.com/maps/documentation/urls/get-started
     */
    fun toGoogleMapsUriString(uriQuote: UriQuote = DefaultUriQuote()): String = Uri(
        scheme = "https",
        host = "www.google.com",
        path = "/maps",
        queryParams = mutableMapOf<String, String>().apply {
            mainPoint?.let { (lat, lon) ->
                set("q", "$lat,$lon")
            } ?: q?.let { q ->
                set("q", q)
            }
            z?.let { z ->
                set("z", z)
            }
        },
        uriQuote = uriQuote,
    ).toString()

    /**
     * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/
     */
    @Suppress("SpellCheckingInspection")
    fun toMagicEarthUriString(uriQuote: UriQuote = DefaultUriQuote()): String = Uri(
        scheme = "magicearth",
        path = "//",
        queryParams = mutableMapOf<String, String>().apply {
            mainPoint?.let { (lat, lon) ->
                set("lat", lat)
                set("lon", lon)
            }
            q?.let { q ->
                set("q", q)
            }
            z?.let { z ->
                set("zoom", z)
            }
        },
        uriQuote = uriQuote,
    ).toString()

    fun toNorthSouthWestEastDecCoordsString(): String = (mainPoint ?: ("0" to "0")).let { (lat, lon) ->
        listOf(
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
