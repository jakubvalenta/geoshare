package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.allUriPattern

class OsmAndUrlConverter : UrlConverter.WithUriPattern {
    override val uriPattern: Pattern = Pattern.compile("""https?://(www\.)?osmand\.net/.+""")

    override val conversionUriPattern = allUriPattern {
        first {
            all {
                fragment("""$z/$lat/$lon.*""")
                query("pin", """$lat,$lon""")
            }
            fragment("""$z/$lat/$lon.*""")
            query("pin", """$lat,$lon""")
        }
    }
}
