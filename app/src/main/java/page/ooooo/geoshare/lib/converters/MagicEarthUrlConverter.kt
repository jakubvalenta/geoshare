package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/
 */
class MagicEarthUrlConverter : UrlConverter.WithUriPattern {
    companion object {
        const val NAME = "Magic Earth"

        @Suppress("SpellCheckingInspection")
        fun formatUriString(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String = Uri(
            scheme = "magicearth",
            path = "//",
            queryParams = buildMap {
                position.apply {
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
                }
            }.toImmutableMap(),
            uriQuote = uriQuote,
        ).toString()
    }

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""((https?://)?magicearth.com|magicearth:/)/\?\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_magic_earth_name,
        inputs = listOf(
            DocumentationInput.Url(20, "https://magicearth.com/"),
        ),
    )

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        all {
            optional {
                on { queryParams["z"]?.let { it matches Z } } doReturn { PositionMatch(it) }
            }
            optional {
                on { queryParams["zoom"]?.let { it matches Z } } doReturn { PositionMatch(it) }
            }
            first {
                all {
                    on { queryParams["lat"]?.let { it matches LAT } } doReturn { PositionMatch(it) }
                    on { queryParams["lon"]?.let { it matches LON } } doReturn { PositionMatch(it) }
                }
                on { queryParams["name"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it) }
                @Suppress("SpellCheckingInspection")
                on { queryParams["daddr"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it) }
                on { queryParams["q"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it) }
            }
        }
    }
}
