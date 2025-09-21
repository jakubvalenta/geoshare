package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.htmlPattern
import page.ooooo.geoshare.lib.uriPattern

/**
 * See https://developers.google.com/waze/deeplinks/
 */
class WazeUrlConverter : UrlConverter.WithUriPattern, UrlConverter.WithShortUriPattern, UrlConverter.WithHtmlPattern {
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?((www|ul)\.)?waze\.com/\S+""")
    override val shortUriPattern: Pattern =
        Pattern.compile("""(https?://)?(www\.)?waze\.com/(ul/h|live-map\?h=)(?P<id>[A-Za-z0-9_-]+)""")
    override val shortUriReplacement = "https://www.waze.com/live-map?h=\${id}"

    override val conversionUriPattern = uriPattern {
        all {
            optional {
                query("z", PositionRegex(Z))
            }
            first {
                query("to", PositionRegex("""ll\.$LAT,$LON"""))
                query("ll", PositionRegex("$LAT,$LON"))
                @Suppress("SpellCheckingInspection") query("latlng", PositionRegex("$LAT,$LON"))
                query("q", PositionRegex(Q_PARAM))
                query("venue_id", PositionRegex(".+"))
                query("place", PositionRegex(".+"))
                query("to", PositionRegex("""place\..+"""))
            }
        }
    }

    override fun getHtmlUri(uri: Uri, position: Position?, uriQuote: UriQuote) = uri

    override val conversionHtmlPattern = htmlPattern {
        content(PositionRegex(""""latLng":{"lat":$LAT,"lng":$LON}"""))
    }

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title
}
