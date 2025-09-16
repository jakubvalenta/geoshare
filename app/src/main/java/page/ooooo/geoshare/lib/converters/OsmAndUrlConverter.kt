package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.uriPattern

class OsmAndUrlConverter : UrlConverter.WithUriPattern {
    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""https?://(www\.)?osmand\.net/\S+""")

    override val conversionUriPattern = uriPattern {
        all {
            fragment("""$z/$lat/$lon.*""")
            query("pin", """$lat,$lon""")
        }
        fragment("""$z/$lat/$lon.*""")
        query("pin", """$lat,$lon""")
    }
}
