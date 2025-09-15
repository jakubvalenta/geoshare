package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.allUriPattern

/**
 * See https://developers.google.com/waze/deeplinks/
 */
class WazeUrlConverter : UrlConverter.WithUriPattern, UrlConverter.WithShortUriPattern {
    override val uriPattern: Pattern = Pattern.compile("""https?://((www|ul)\.)?waze\.com/\S+""")
    override val shortUriPattern: Pattern =
        Pattern.compile("""https?://(www\.)?waze\.com/(ul/h|live-map\?h=)(?P<id>[A-Za-z0-9_-]+)""")
    override val shortUriReplacement = "https://www.waze.com/live-map?h=\${id}"

    override val conversionUriPattern = allUriPattern {
        optional {
            query("z", z, sanitizeZoom)
        }
        first {
            query("to", """ll\.$lat,$lon""")
            query("ll", "$lat,$lon")
            query("q", q)
            query("venue_id", ".+")
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title
}
