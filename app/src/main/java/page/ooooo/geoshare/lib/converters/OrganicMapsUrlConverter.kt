package page.ooooo.geoshare.lib.converters

import com.google.re2j.Matcher
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*

class OrganicMapsUrlConverter : UrlConverter.WithUriPattern {
    companion object {
        const val HASH = """(?P<hash>[A-Za-z0-9\-_]{2,})"""
    }

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(omaps\.app|comaps\.at)/\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_organic_maps_name,
        inputs = listOf(
            DocumentationInput.Url(25, "https://omaps.app/"),
            DocumentationInput.Url(25, "https://comaps.at/"),
        ),
    )

    override val conversionUriPattern = conversionPattern<PositionMatch> {
        path("""/$HASH\S*""") { OrganicMapsGeoHashPositionMatch(it) }
    }

    private class OrganicMapsGeoHashPositionMatch(matcher: Matcher) : GeoHashPositionMatch(matcher) {
        override fun decode(hash: String) = decodeOrganicMapsGeoHash(hash).let { (lat, lon, z) ->
            Triple(lat.toScale(7), lon.toScale(7), z)
        }
    }
}
