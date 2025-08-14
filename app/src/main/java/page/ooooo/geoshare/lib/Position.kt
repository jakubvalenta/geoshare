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
    var lat: String? = null,
    var lon: String? = null,
    var q: String? = null,
    var z: String? = null,
) {
    companion object {
        fun fromGeoUri(geoUri: Uri, uriQuote: UriQuote = DefaultUriQuote()): Position? {
            if (geoUri.scheme != "geo") {
                return null
            }
            var lat: String? = null
            var lon: String? = null
            var q: String? = null
            var z: String? = null
            geoUri.authority?.split(",")?.let {
                lat = it.getOrNull(0)
                lon = it.getOrNull(1)
            }
            getUrlQueryParams(geoUri.query, uriQuote).let {
                q = it["q"]
                z = it["z"]
            }
            return Position(lat, lon, q, z)
        }
    }

    fun addMatcher(m: Matcher) {
        matchGroupOrNull(m, "lat")?.let { lat = it }
        matchGroupOrNull(m, "lon")?.let { lon = it }
        matchGroupOrNull(m, "q")?.let { q = it }
        matchGroupOrNull(m, "z")?.let { z = max(1, min(21, it.toDouble().roundToInt())).toString() }
    }

    fun toGeoUriString(uriQuote: UriQuote = DefaultUriQuote()): String {
        val coords = "${lat ?: 0},${lon ?: 0}"
        val params = mapOf(
            "q" to (q ?: coords),
            "z" to z,
        )
            .filter { it.value != null }
            .map { "${it.key}=${uriQuote.encode(it.value!!.replace('+', ' '))}" }
            .fastJoinToString("&")
        return "geo:$coords?$params".trimEnd('?')
    }

    fun toDegString(): String = "${lat ?: 0}, ${lon ?: 0}"

    fun toGeoUri(uriQuote: UriQuote = DefaultUriQuote()): Uri = toGeoUriString(uriQuote).toUri()
}
