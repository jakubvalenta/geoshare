package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import page.ooooo.geoshare.lib.uriPattern

class OsmAndUrlConverter : UrlConverter.WithUriPattern {
    @StringRes
    override val nameResId = R.string.converter_osm_and_name

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?osmand\.net/\S+""")
    override val supportedInputs = listOf(
        SupportedInput.Url("https://osmand.net/map", 20),
    )

    override val conversionUriPattern = uriPattern {
        all {
            fragment(PositionRegex("""$Z/$LAT/$LON.*"""))
            query("pin", PositionRegex("""$LAT,$LON"""))
        }
        fragment(PositionRegex("""$Z/$LAT/$LON.*"""))
        query("pin", PositionRegex("""$LAT,$LON"""))
    }
}
