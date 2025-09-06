package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ConversionUrlPattern
import page.ooooo.geoshare.lib.allHtmlPattern
import page.ooooo.geoshare.lib.allUrlPattern

class GoogleMapsUrlConverter() : UrlConverter {
    override val name = "Google Maps"

    override val host: Pattern = Pattern.compile("""((www|maps)\.)?google(\.[a-z]{2,3})?\.[a-z]{2,3}""")
    override val shortUrlHost: Pattern = Pattern.compile("""(maps\.)?(app\.)?goo\.gl""")

    @Suppress("SpellCheckingInspection")
    override val conversionUrlPattern: ConversionUrlPattern = allUrlPattern {
        val data = """!3d(?P$lat-?\d{1,2}(\.\d{1,16})?)!4d(?P$lon-?\d{1,3}(\.\d{1,16})?)"""

        query("zoom", z)
        first {
            query("destination", "$lat,$lon")
            query("destination", q)
            query("q", "$lat,$lon")
            query("q", q)
            query("query", "$lat,$lon")
            query("query", q)
            query("viewpoint", "$lat,$lon")
            query("center", "$lat,$lon")
        }
        first {
            path("""^/maps/.*/@[\d.,+-]+,${z}z/data=.*$data.*$""")
            path("""^/maps/.*/data=.*$data.*$""")
            path("""^/maps/@$lat,$lon,${z}z.*$""")
            path("""^/maps/@$lat,$lon.*$""")
            path("""^/maps/@$""")
            path("""^/maps/place/$lat,$lon/@[\d.,+-]+,${z}z.*$""")
            path("""^/maps/place/$q/@$lat,$lon,${z}z.*$""")
            path("""^/maps/place/$q/@$lat,$lon.*$""")
            path("""^/maps/place/$lat,$lon.*$""")
            path("""^/maps/place/$q.*$""")
            path("""^/maps/place//.*$""")
            path("""^/maps/placelists/list/.*$""")
            path("""^/maps/search/$lat,$lon.*$""")
            path("""^/maps/search/$q.*$""")
            path("""^/maps/search/$""")
            path("""^/maps/dir/.*/$lat,$lon/data[^/]*$""")
            path("""^/maps/dir/.*/$q/data[^/]*$""")
            path("""^/maps/dir/.*/$lat,$lon$""")
            path("""^/maps/dir/.*/@$lat,$lon,${z}z.*$""")
            path("""^/maps/dir/.*/$q$""")
            path("""^/maps/dir/$""")
            path("""^/maps/?$""")
            path("""^/search/?$""")
            path("""^/?$""")
        }
    }

    override val htmlPattern = allHtmlPattern {
        html("""/@$lat,$lon""")
        html("""\[null,null,$lat,$lon\]""")
    }

    override val htmlRedirectPattern = allHtmlPattern {
        html("""data-url="(?P<url>[^"]+""") // FIXME Return matcher instead of Position and read matcher["url"] in ConversionState.
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
}
