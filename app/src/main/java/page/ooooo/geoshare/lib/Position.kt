package page.ooooo.geoshare.lib

import android.net.Uri
import androidx.compose.ui.util.fastJoinToString
import androidx.core.net.toUri
import com.google.re2j.Matcher
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private fun matchGroupOrNull(m: Matcher, name: String): String? = try {
    m.group(name)
} catch (_: IllegalArgumentException) {
    null
}

data class Position(
    var coords: Coords = Coords(),
    var params: Params = Params(),
) {
    data class Coords(var lat: String? = null, var lon: String? = null) {
        companion object {
            fun fromGeoUri(geoUri: Uri): Coords = geoUri.authority.takeIf { it != null }?.let { authority ->
                authority.split(",").takeIf { it.size >= 2 }?.let { parts ->
                    Coords(parts[0], parts[1])
                }
            } ?: Coords()
        }

        fun addMatcher(m: Matcher) {
            val newLat = matchGroupOrNull(m, "lat")
            if (newLat != null) {
                lat = newLat
            }
            val newLon = matchGroupOrNull(m, "lon")
            if (newLon != null) {
                lon = newLon
            }
        }

        fun toGeoUriAuthority(): String = "geo:${lat ?: 0},${lon ?: 0}"

        fun toDegString(): String = "${lat ?: 0}, ${lon ?: 0}"
    }

    data class Params(var q: String? = null, var z: String? = null) {
        companion object {
            fun fromGeoUri(geoUri: Uri, uriQuote: UriQuote = DefaultUriQuote()): Params =
                getUrlQueryParams(geoUri.query, uriQuote).let {
                    Params(q = it["q"], z = it["z"])
                }
        }

        fun addMatcher(m: Matcher) {
            matchGroupOrNull(m, "q")?.let { q = it }
            matchGroupOrNull(m, "z")?.let { z = max(1, min(21, it.toDouble().roundToInt())).toString() }
        }

        fun toGeoUriQueryParams(uriQuote: UriQuote = DefaultUriQuote()): String =
            mapOf("q" to q, "z" to z).filter { it.value != null }
                .map { "${it.key}=${uriQuote.encode(it.value!!.replace('+', ' '))}" }.fastJoinToString("&")
    }

    companion object {
        fun fromGeoUri(geoUri: Uri, uriQuote: UriQuote = DefaultUriQuote()): Position? =
            geoUri.takeIf { it.scheme == "geo" }?.let {
                Position(
                    Coords.fromGeoUri(it),
                    Params.fromGeoUri(it, uriQuote),
                )
            }
    }

    fun addMatcher(m: Matcher) {
        coords.addMatcher(m)
        params.addMatcher(m)
    }

    fun toGeoUriString(uriQuote: UriQuote = DefaultUriQuote()): String =
        "${coords.toGeoUriAuthority()}?${params.toGeoUriQueryParams(uriQuote)}".trimEnd('?')

    fun toGeoUri(uriQuote: UriQuote = DefaultUriQuote()): Uri = toGeoUriString(uriQuote).toUri()

    fun toDegString(): String = coords.toDegString()
}
