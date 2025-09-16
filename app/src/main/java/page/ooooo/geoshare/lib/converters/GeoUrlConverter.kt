package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.uriPattern

class GeoUrlConverter : UrlConverter.WithUriPattern {
    override val uriPattern: Pattern = Pattern.compile("""geo:\S+""")
    override val conversionUriPattern = uriPattern {
        all {
            path("""$lat,$lon""")
            optional {
                query("q", q)
            }
            optional {
                query("z", z)
            }
        }
    }
}
