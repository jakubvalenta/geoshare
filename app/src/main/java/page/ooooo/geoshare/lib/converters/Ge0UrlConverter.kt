package page.ooooo.geoshare.lib.converters

import com.google.re2j.Matcher
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*

class Ge0UrlConverter : UrlConverter.WithUriPattern {
    companion object {
        const val HASH = """(?P<hash>[A-Za-z0-9\-_]{2,})"""
    }

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""((https?://)?(comaps\.at|ge0\.me|omaps\.app)|ge0:/)/\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_ge0_name,
        inputs = listOf(
            DocumentationInput.Url(25, "http://ge0.me/"),
            DocumentationInput.Url(25, "https://omaps.app/"),
            DocumentationInput.Url(25, "https://comaps.at/"),
        ),
    )

    override val conversionUriPattern = conversionPattern<PositionMatch> {
        all {
            scheme("ge0") { Ge0HashPositionMatch(it) }
            host(HASH) { Ge0HashPositionMatch(it) }
        }
        path("""/$HASH\S*""") { Ge0HashPositionMatch(it) }
    }

    private class Ge0HashPositionMatch(matcher: Matcher) : GeoHashPositionMatch(matcher) {
        override fun decode(hash: String) = decodeGe0Hash(hash).let { (lat, lon, z) ->
            Triple(lat.toScale(7), lon.toScale(7), z)
        }
    }
}
