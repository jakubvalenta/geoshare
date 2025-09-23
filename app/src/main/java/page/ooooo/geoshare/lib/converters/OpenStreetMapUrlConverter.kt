package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import page.ooooo.geoshare.lib.uriPattern

class OpenStreetMapUrlConverter : UrlConverter.WithUriPattern {
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?openstreetmap\.org/\S+""")

    override val conversionUriPattern = uriPattern {
        fragment(PositionRegex("""map(=|%3D)$Z(/|%2F)$LAT(/|%2F)$LON.*"""))
    }
}
