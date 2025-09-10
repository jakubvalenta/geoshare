package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.allUriPattern

class OpenStreetMapUrlConverter : UrlConverter.WithUriPattern {
    override val uriPattern: Pattern = Pattern.compile("""https?://(www\.)?openstreetmap\.org/.+""")

    override val conversionUriPattern = allUriPattern {
        fragment("""map=$z/$lat/$lon.*""")
    }
}
