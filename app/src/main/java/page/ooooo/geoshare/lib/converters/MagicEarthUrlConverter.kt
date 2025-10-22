package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.uriPattern

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/
 */
class MagicEarthUrlConverter : UrlConverter.WithUriPattern {
    companion object {
        const val NAME = "Magic Earth"

        /**
         * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/
         */
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
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?magicearth.com/\?\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_magic_earth_name,
        inputs = listOf(
            DocumentationInput.Url(20, "https://magicearth.com/"),
        ),
    )

    override val conversionUriPattern = uriPattern {
        all {
            optional {
                query("z", PositionRegex(Z))
            }
            first {
                all {
                    query("lat", PositionRegex(LAT))
                    query("lon", PositionRegex(LON))
                }
                query("name", PositionRegex(Q_PARAM))
                @Suppress("SpellCheckingInspection")
                query("daddr", PositionRegex(Q_PARAM))
                query("q", PositionRegex(Q_PARAM))
            }
        }
    }
}
