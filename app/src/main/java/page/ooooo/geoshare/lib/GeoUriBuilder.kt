package page.ooooo.geoshare.lib

import androidx.compose.ui.util.fastJoinToString
import com.google.re2j.Matcher
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
    private val uriQuote: UriQuote = DefaultUriQuote(),
) {
    fun fromMatcher(m: Matcher) {
        setQFromString(matchGroupOrNull(m, "q"))
        setZFromString(matchGroupOrNull(m, "z"))
    }

    fun setQFromString(newQ: String?) {
        if (newQ != null) {
            q = newQ
        }
    }

    fun setZFromString(newZ: String?) {
        if (newZ != null) {
            z = max(1, min(21, newZ.toDouble().roundToInt())).toString()
        }
    }

    override fun toString(): String = mapOf("q" to q, "z" to z)
        .filter { it.value != null }
        .map { "${it.key}=${uriQuote.encode(it.value!!.replace('+', ' '))}" }
        .fastJoinToString("&")
}

data class GeoUriBuilder(private val uriQuote: UriQuote = DefaultUriQuote()) {
    var coords: GeoUriCoords = GeoUriCoords()
    var params: GeoUriParams = GeoUriParams(uriQuote = uriQuote)

    fun fromMatcher(m: Matcher) {
        coords.fromMatcher(m)
        params.fromMatcher(m)
    }

    override fun toString(): String = "geo:$coords?$params".trimEnd('?')
}
