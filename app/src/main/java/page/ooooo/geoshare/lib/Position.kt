package page.ooooo.geoshare.lib

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import kotlin.random.Random

@Immutable
data class Point(val lat: String = "0", val lon: String = "0")

@Immutable
data class Position(
    val points: ImmutableList<Point>? = null,
    val q: String? = null,
    val z: String? = null,
) {
    companion object {
        val example: Position = genRandomPosition(minLat = 0.0, maxLon = -100.0)

        fun genRandomPosition(
            minLat: Double = -50.0,
            maxLat: Double = 80.0,
            minLon: Double = -180.0,
            maxLon: Double = 180.0,
        ): Position = Position(
            Random.nextDouble(minLat, maxLat).toScale(6).toString(),
            Random.nextDouble(minLon, maxLon).toScale(6).toString(),
            z = "8",
        )
    }

    constructor(
        lat: String,
        lon: String,
        q: String? = null,
        z: String? = null,
    ) : this(persistentListOf(Point(lat, lon)), q, z)

    val mainPoint: Point? get() = points?.lastOrNull()

    fun toCoordsDecString(): String = (mainPoint ?: Point()).let { (lat, lon) -> "$lat, $lon" }

    fun toParamsString(separator: String): String = buildList {
        q.takeUnless { it.isNullOrEmpty() }?.let { q ->
            (mainPoint ?: Point("0", "0")).let { (lat, lon) ->
                val coords = "$lat,$lon"
                if (q != coords) {
                    add(q.replace('+', ' '))
                }
            }
        }
        z.takeUnless { it.isNullOrEmpty() }?.let { z ->
            add("z$z")
        }
    }.joinToString(separator)

    fun toGeoUriString(uriQuote: UriQuote = DefaultUriQuote()): String =
        (mainPoint ?: Point()).let { (lat, lon) -> "$lat,$lon" }.let { coords ->
            Uri(
                scheme = "geo",
                path = coords,
                queryParams = buildMap {
                    set("q", q ?: coords)
                    z?.let { z ->
                        set("z", z)
                    }
                }.toImmutableMap(),
                uriQuote = uriQuote,
            ).toString()
        }

    fun toNorthSouthWestEastDecCoordsString(): String = (mainPoint ?: Point()).let { (lat, lon) ->
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
