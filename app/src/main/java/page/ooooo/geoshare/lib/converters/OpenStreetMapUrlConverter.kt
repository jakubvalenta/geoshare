package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import java.net.URL
import kotlin.math.max

class OpenStreetMapUrlConverter : UrlConverter.WithUriPattern, UrlConverter.WithHtmlPattern {
    companion object {
        const val ELEMENT_PATH = """/(?P<type>node|relation|way)/(?P<id>\d+)([/?#].*|$)"""
        const val HASH = """(?P<hash>[A-Za-z0-9_~]+-+)"""

        @Suppress("SpellCheckingInspection")
        private val HASH_CHAR_MAP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_~"
            .mapIndexed { i, char -> char to i }.toMap()

        /**
         * See https://wiki.openstreetmap.org/wiki/Shortlink#How_the_encoding_works
         */
        fun decodeGeoHash(hash: String) =
            decodeGeoHash(hash, HASH_CHAR_MAP, 6).let { (lat, lon, z) ->
                // Add relative zoom, which works like this:
                // - If the hash doesn't end with "-", add 0.
                // - If the hash ends with "-", add -2.
                // - If the hash ends with "--", add -1.
                // - If the hash ends with "---", add 0.
                // - If the hash ends with "----", add -2.
                // - etc.
                val relativeZoom = hash.takeLastWhile { it == '-' }.length.takeIf { it > 0 }
                    ?.let { zoomCharCount -> (zoomCharCount + 2).mod(3) - 2 }
                    ?: 0
                Triple(lat, lon, max(z + relativeZoom, 0))
            }
    }

    class GeoHashPositionRegex(regex: String) : page.ooooo.geoshare.lib.GeoHashPositionRegex(regex) {
        override fun decode(hash: String) = decodeGeoHash(hash)
    }

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?(openstreetmap|osm)\.org/\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_open_street_map_name,
        inputs = listOf(
            DocumentationInput.Url(20, "https://www.openstreetmap.org/"),
            DocumentationInput.Url(23, "https://www.openstreetmap.org/node"),
            DocumentationInput.Url(23, "https://www.openstreetmap.org/relation"),
            DocumentationInput.Url(23, "https://www.openstreetmap.org/way"),
            DocumentationInput.Url(23, "https://osm.org/"),
            DocumentationInput.Url(23, "https://osm.org/go/"),
        ),
    )

    override val conversionUriPattern = uriPattern {
        path(GeoHashPositionRegex("""/go/$HASH"""))
        path(PositionRegex(ELEMENT_PATH))
        fragment(PositionRegex("""map=$Z/$LAT/$LON.*"""))
    }

    override val conversionHtmlPattern = htmlPattern<PositionRegex> {
        content(PointsPositionRegex(""""lat":$LAT,"lon":$LON"""))
    }

    override val conversionHtmlRedirectPattern = null

    override fun getHtmlUrl(uri: Uri): URL? {
        val m = Pattern.compile(ELEMENT_PATH).matcher(uri.path)
        if (!m.matches()) {
            return null
        }
        val type = try {
            m.group("type")
        } catch (_: IllegalArgumentException) {
            return null
        }
        val id = try {
            m.group("id")
        } catch (_: IllegalArgumentException) {
            return null
        }
        return URL("https://www.openstreetmap.org/api/0.6/$type/$id${if (type != "node") "/full" else ""}.json")
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_open_street_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_open_street_map_loading_indicator_title
}
