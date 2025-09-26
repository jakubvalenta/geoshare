package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import page.ooooo.geoshare.lib.htmlPattern
import page.ooooo.geoshare.lib.uriPattern

class AppleMapsUrlConverter() : UrlConverter.WithUriPattern, UrlConverter.WithHtmlPattern {

    /**
     * Sets points to zero, so that we avoid parsing HTML for this URI. Because parsing HTML for this URI doesn't work.
     */
    class DoNotParseHtmlPositionRegex(regex: String) : PositionRegex(regex) {
        override val points = listOf("0" to "0")
    }

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?maps\.apple(\.com)?[/?#]\S+""")

    override val conversionUriPattern = uriPattern {
        all {
            optional {
                query("z", PositionRegex(Z))
            }
            first {
                all {
                    host(PositionRegex("maps.apple"))
                    path(PositionRegex("/p/.+"))
                }
                query("ll", PositionRegex("$LAT,$LON"))
                query("coordinate", PositionRegex("$LAT,$LON"))
                query("q", PositionRegex("$LAT,$LON"))
                query("address", PositionRegex(Q_PARAM))
                query("name", PositionRegex(Q_PARAM))
                query("auid", PositionRegex(".+"))
                query("place-id", PositionRegex(".+"))
                all {
                    query("q", PositionRegex(Q_PARAM))
                    query("sll", PositionRegex("$LAT,$LON"))
                }
                query("sll", PositionRegex("$LAT,$LON"))
                query("center", PositionRegex("$LAT,$LON"))
                query("q", DoNotParseHtmlPositionRegex(Q_PARAM))
            }
        }
    }

    override val conversionHtmlPattern = htmlPattern {
        all {
            content(PositionRegex("""<meta property="place:location:latitude" content="$LAT""""))
            content(PositionRegex("""<meta property="place:location:longitude" content="$LON""""))
        }
    }

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title
}
