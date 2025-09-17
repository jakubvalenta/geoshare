package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionRegex.Companion.Q_PATH
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import page.ooooo.geoshare.lib.RedirectRegex
import page.ooooo.geoshare.lib.htmlPattern
import page.ooooo.geoshare.lib.uriPattern

class GoogleMapsUrlConverter() : UrlConverter.WithUriPattern, UrlConverter.WithShortUriPattern,
    UrlConverter.WithHtmlPattern {
    private val shortUriRegex = """((maps\.)?(app\.)?goo\.gl|g\.co)/[/A-Za-z0-9_-]+"""

    override val uriPattern: Pattern =
        Pattern.compile("""https?://((www|maps)\.)?(google(\.[a-z]{2,3})?\.[a-z]{2,3}/\S+|$shortUriRegex)""")
    override val shortUriPattern: Pattern = Pattern.compile("""https?://$shortUriRegex""")
    override val shortUriReplacement: String? = null

    @Suppress("SpellCheckingInspection")
    override val conversionUriPattern = uriPattern {
        all {
            optional {
                query("zoom", PositionRegex(Z))
                first {
                    query("destination", PositionRegex("$LAT,$LON"))
                    query("destination", PositionRegex(Q_PARAM))
                    query("q", PositionRegex("$LAT,$LON"))
                    query("q", PositionRegex(Q_PARAM))
                    query("query", PositionRegex("$LAT,$LON"))
                    query("query", PositionRegex(Q_PARAM))
                    query("viewpoint", PositionRegex("$LAT,$LON"))
                    query("center", PositionRegex("$LAT,$LON"))
                }
            }
            first {
                val data = """!3d$LAT!4d$LON"""

                path(PositionRegex("""/maps/.*/@[\d.,+-]+,${Z}z/data=.*$data.*"""))
                path(PositionRegex("""/maps/.*/data=.*$data.*"""))
                path(PositionRegex("""/maps/@$LAT,$LON,${Z}z.*"""))
                path(PositionRegex("""/maps/@$LAT,$LON.*"""))
                path(PositionRegex("""/maps/@"""))
                path(PositionRegex("""/maps/place/$LAT,$LON/@[\d.,+-]+,${Z}z.*"""))
                path(PositionRegex("""/maps/place/$Q_PATH/@$LAT,$LON,${Z}z.*"""))
                path(PositionRegex("""/maps/place/$Q_PATH/@$LAT,$LON.*"""))
                path(PositionRegex("""/maps/place/$LAT,$LON.*"""))
                path(PositionRegex("""/maps/place/$Q_PATH.*"""))
                path(PositionRegex("""/maps/place//.*"""))
                path(PositionRegex("""/maps/placelists/list/.*"""))
                path(PositionRegex("""/maps/search/$LAT,$LON.*"""))
                path(PositionRegex("""/maps/search/$Q_PATH.*"""))
                path(PositionRegex("""/maps/search/"""))
                path(PositionRegex("""/maps/dir/.*/$LAT,$LON/data[^/]*"""))
                path(PositionRegex("""/maps/dir/.*/$Q_PATH/data[^/]*"""))
                path(PositionRegex("""/maps/dir/.*/$LAT,$LON"""))
                path(PositionRegex("""/maps/dir/.*/@$LAT,$LON,${Z}z.*"""))
                path(PositionRegex("""/maps/dir/.*/$Q_PATH"""))
                path(PositionRegex("""/maps/dir/"""))
                path(PositionRegex("""/maps/?"""))
                path(PositionRegex("""/search/?"""))
                path(PositionRegex("""/?"""))
            }
        }
    }

    override val conversionHtmlPattern = htmlPattern {
        content(PositionRegex("""/@$LAT,$LON"""))
        content(object : PositionRegex("""\[null,null,$LAT,$LON\]""") {
            override val points: List<Pair<String, String>>
                get() = pattern.matcher(input).let { pointsMatcher ->
                    mutableListOf<Pair<String, String>>().apply {
                        while (pointsMatcher.find()) {
                            try {
                                add(pointsMatcher.group("lat") to pointsMatcher.group("lon"))
                            } catch (_: IllegalArgumentException) {
                                // Do nothing
                            }
                        }
                    }
                }
        })
    }

    override val conversionHtmlRedirectPattern = htmlPattern {
        content(RedirectRegex("""data-url="(?P<url>[^"]+)""""))
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
}
