package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.allUriPattern

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/
 */
class MagicEarthUrlConverter : UrlConverter.WithUriPattern {
    override val uriPattern: Pattern = Pattern.compile("""https?://magicearth.com/\?\S+""")

    override val conversionUriPattern = allUriPattern {
        optional {
            query("z", z, sanitizeZoom)
        }
        first {
            all {
                query("lat", lat)
                query("lon", lon)
            }
            query("name", q)
            @Suppress("SpellCheckingInspection")
            query("daddr", q)
            query("q", q)
        }
    }
}
