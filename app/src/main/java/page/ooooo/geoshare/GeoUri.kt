package page.ooooo.geoshare

import androidx.compose.ui.util.fastJoinToString
import java.util.regex.Matcher
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private fun matchGroupOrNull(m: Matcher, name: String): String? =
    try {
        m.group(name)
    } catch (_: IllegalArgumentException) {
        null
    }

data class GeoUriCoords(var lat: String = "0", var lon: String = "0") {
    fun fromMatcher(m: Matcher) {
        val newLat = matchGroupOrNull(m, "lat")
        if (newLat != null) {
            lat = newLat
        }
        val newLon = matchGroupOrNull(m, "lon")
        if (newLon != null) {
            lon = newLon
        }
    }

    override fun toString(): String = "$lat,$lon"
}

data class GeoUriParams(
    var q: String? = null,
    var z: String? = null,
    private val uriQuote: UriQuote = DefaultUriQuote()
) {
    fun fromMatcher(m: Matcher) {
        val newQ = matchGroupOrNull(m, "q")
        if (newQ != null) {
            q = newQ
        }
        val newZ = matchGroupOrNull(m, "z")
        if (newZ != null) {
            z = max(1, min(21, newZ.toDouble().roundToInt())).toString()
        }
    }

    override fun toString(): String = hashMapOf("q" to q, "z" to z)
        .filter { it.value != null }
        .map { "${it.key}=${uriQuote.encode(it.value!!.replace('+', ' '))}" }
        .fastJoinToString("&")
}

data class GeoUri(private val uriQuote: UriQuote = DefaultUriQuote()) {
    val coords: GeoUriCoords = GeoUriCoords()
    val params: GeoUriParams = GeoUriParams(uriQuote = uriQuote)

    fun fromMatcher(m: Matcher) {
        coords.fromMatcher(m)
        params.fromMatcher(m)
    }

    override fun toString(): String = "geo:$coords?$params".trimEnd('?')
}
