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

    override val conversionUriPattern = conversionPattern {
        all {
            optional {
                onUri { queryParams["z"]?.let { it matcherIfMatches Z } } doReturn { PositionMatch(it) }
            }
            optional {
                onUri { queryParams["zoom"]?.let { it matcherIfMatches Z } } doReturn { PositionMatch(it) }
            }
            first {
                all {
                    onUri { queryParams["lat"]?.let { it matcherIfMatches LAT } } doReturn { PositionMatch(it) }
                    onUri { queryParams["lon"]?.let { it matcherIfMatches LON } } doReturn { PositionMatch(it) }
                }
                onUri { queryParams["name"]?.let { it matcherIfMatches Q_PARAM } } doReturn { PositionMatch(it) }
                @Suppress("SpellCheckingInspection")
                onUri { queryParams["daddr"]?.let { it matcherIfMatches Q_PARAM } } doReturn { PositionMatch(it) }
                onUri { queryParams["q"]?.let { it matcherIfMatches Q_PARAM } } doReturn { PositionMatch(it) }
            }
        }
    }
}
