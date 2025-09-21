package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.htmlPattern
import page.ooooo.geoshare.lib.uriPattern

class YandexMapsUrlConverter() :
    UrlConverter.WithUriPattern,
    UrlConverter.WithShortUriPattern,
    UrlConverter.WithHtmlPattern {

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?yandex\.com/\S+""")
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?yandex\.com/maps/-/\S+""")
    override val shortUriReplacement: String? = null

    @Suppress("SpellCheckingInspection")
    override val conversionUriPattern = uriPattern {
        all {
            optional {
                first {
                    query("whatshere%5Bzoom%5D", PositionRegex(Z))
                    query("z", PositionRegex(Z))
                }
            }
            first {
                query("whatshere%5Bpoint%5D", PositionRegex("$LON,$LAT"))
                query("ll", PositionRegex("$LON,$LAT"))
                path(PositionRegex("""/maps/org/\d+/.*"""))
            }
        }
    }

    override fun getHtmlUri(uri: Uri, position: Position?, uriQuote: UriQuote) = uri

    override val conversionHtmlPattern = htmlPattern {
        content(PositionRegex("""data-coordinates="$LON,$LAT""""))
    }
    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title
}
