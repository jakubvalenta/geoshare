package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.allUriPattern

class GeoUrlConverter : UrlConverter {
    override val name = "geo: URI"
    override val uriPattern: Pattern = Pattern.compile("""geo:.+""")
    override val conversionUriPattern = allUriPattern {
        path("""$lat,$lon""")
        optional {
            query("q", q)
        }
        optional {
            query("z", z)
        }
    }
}
