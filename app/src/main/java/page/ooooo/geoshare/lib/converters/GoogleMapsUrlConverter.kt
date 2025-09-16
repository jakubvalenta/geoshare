package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ConversionUriPattern
import page.ooooo.geoshare.lib.firstHtmlPattern
import page.ooooo.geoshare.lib.allUriPattern

class GoogleMapsUrlConverter() : UrlConverter.WithUriPattern, UrlConverter.WithShortUriPattern,
    UrlConverter.WithHtmlPattern {
    private val shortUriRegex = """((maps\.)?(app\.)?goo\.gl|g\.co)/[/A-Za-z0-9_-]+"""

    override val uriPattern: Pattern =
        Pattern.compile("""https?://((www|maps)\.)?(google(\.[a-z]{2,3})?\.[a-z]{2,3}/\S+|$shortUriRegex)""")
    override val shortUriPattern: Pattern = Pattern.compile("""https?://$shortUriRegex""")
    override val shortUriReplacement: String? = null

    @Suppress("SpellCheckingInspection")
    override val conversionUriPattern: ConversionUriPattern = allUriPattern {
        optional {
            query("zoom", z, sanitizeZoom)
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
        }
        first {
            val data = """!3d$lat!4d$lon"""
            val q = """(?P<q>[^/]+)"""

            path("""/maps/.*/@[\d.,+-]+,${z}z/data=.*$data.*""", sanitizeZoom)
            path("""/maps/.*/data=.*$data.*""")
            path("""/maps/@$lat,$lon,${z}z.*""", sanitizeZoom)
            path("""/maps/@$lat,$lon.*""")
            path("""/maps/@""")
            path("""/maps/place/$lat,$lon/@[\d.,+-]+,${z}z.*""", sanitizeZoom)
            path("""/maps/place/$q/@$lat,$lon,${z}z.*""", sanitizeZoom)
            path("""/maps/place/$q/@$lat,$lon.*""")
            path("""/maps/place/$lat,$lon.*""")
            path("""/maps/place/$q.*""")
            path("""/maps/place//.*""")
            path("""/maps/placelists/list/.*""")
            path("""/maps/search/$lat,$lon.*""")
            path("""/maps/search/$q.*""")
            path("""/maps/search/""")
            path("""/maps/dir/.*/$lat,$lon/data[^/]*""")
            path("""/maps/dir/.*/$q/data[^/]*""")
            path("""/maps/dir/.*/$lat,$lon""")
            path("""/maps/dir/.*/@$lat,$lon,${z}z.*""", sanitizeZoom)
            path("""/maps/dir/.*/$q""")
            path("""/maps/dir/""")
            path("""/maps/?""")
            path("""/search/?""")
            path("""/?""")
        }
    }

    override val conversionHtmlPattern = firstHtmlPattern {
        html(""".*?/@$lat,$lon.*""")
        html(""".*?\[null,null,$lat,$lon\].*""")
    }

    override val conversionHtmlRedirectPattern = firstHtmlPattern {
        html(""".*?data-url="(?P<url>[^"]+)".*""")
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
}
