package page.ooooo.geoshare.lib.converters

import com.google.re2j.Matcher
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.GeoHashPositionMatch
import page.ooooo.geoshare.lib.PositionMatch
import page.ooooo.geoshare.lib.conversionPattern
import page.ooooo.geoshare.lib.decodeGeoHash
import page.ooooo.geoshare.lib.toScale
import kotlin.math.roundToInt

class OrganicMapsUrlConverter : UrlConverter.WithUriPattern {
    companion object {
        const val HASH = """(?P<hash>[A-Za-z0-9\-_]{2,})"""

        @Suppress("SpellCheckingInspection")
        private val HASH_CHAR_MAP =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".mapIndexed { i, char -> char to i }
                .toMap()

        fun decodeGeoHash(hash: String): Triple<Double, Double, Int> {
            val zFromHash = hash.getOrNull(0)
                ?.let { HASH_CHAR_MAP[it] }
                ?.let { (it / 4.0 + 4).roundToInt() }
            val hash = try {
                hash.substring(1)
            } catch (_: IndexOutOfBoundsException) {
                ""
            }
            return decodeGeoHash(hash, HASH_CHAR_MAP, 6, isLonOddBits = false, useMeanValue = true)
                .let { (lat, lon, z) -> Triple(lat, lon, zFromHash ?: z) }
        }
    }

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(omaps\.app|comaps\.at)/\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_organic_maps_name,
        inputs = listOf(
            DocumentationInput.Url(26, "https://omaps.app/"),
            DocumentationInput.Url(26, "https://comaps.at/"),
        ),
    )

    override val conversionUriPattern = conversionPattern<PositionMatch> {
        path("""/$HASH\S*""") { OrganicMapsGeoHashPositionMatch(it) }
    }

    private class OrganicMapsGeoHashPositionMatch(matcher: Matcher) : GeoHashPositionMatch(matcher) {
        override fun decode(hash: String) = decodeGeoHash(hash).let { (lat, lon, z) ->
            Triple(lat.toScale(7), lon.toScale(7), z)
        }
    }
}
