package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT_NUM
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON_NUM
import page.ooooo.geoshare.lib.PositionRegex.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionRegex.Companion.Q_PATH
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z

class GoogleMapsUrlConverter() :
    UrlConverter.WithUriPattern,
    UrlConverter.WithShortUriPattern,
    UrlConverter.WithHtmlPattern {

    companion object {
        const val SHORT_URL = """((maps\.)?(app\.)?goo\.gl|g\.co)/[/A-Za-z0-9_-]+"""
        const val DATA = """data=(?P<data>.*(!3d$LAT_NUM!4d$LON_NUM|!1d$LON_NUM!2d$LAT_NUM).*)"""
        val DATA_PATTERNS = listOf<Pattern>(
            Pattern.compile("""!3d$LAT!4d$LON"""),
            Pattern.compile("""!1d$LON!2d$LAT"""),
        )
    }

    /**
     * Repeatedly searches for LAT and LON in DATA to get points
     */
    class DataPointsPositionRegex(regex: String) : PositionRegex(regex) {
        override val points: List<Point>?
            get() = groupOrNull("data")?.let { data ->
                mutableListOf<Point>().apply {
                    DATA_PATTERNS.forEach { dataPattern ->
                        dataPattern.matcher(data).let { m ->
                            while (m.find()) {
                                try {
                                    add(m.group("lat") to m.group("lon"))
                                } catch (_: IllegalArgumentException) {
                                    // Do nothing
                                }
                            }
                        }
                    }
                }
            }
    }

    override val uriPattern: Pattern =
        Pattern.compile("""(https?://)?((www|maps)\.)?(google(\.[a-z]{2,3})?\.[a-z]{2,3}[/?#]\S+|$SHORT_URL)""")
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?$SHORT_URL""")

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
                path(DataPointsPositionRegex("""/maps/.*/@[\d.,+-]+,${Z}z/$DATA"""))
                path(DataPointsPositionRegex("""/maps/.*/$DATA"""))
                path(PositionRegex("""/maps/@$LAT,$LON,${Z}z.*"""))
                path(PositionRegex("""/maps/@$LAT,$LON.*"""))
                path(PositionRegex("""/maps/@"""))
                path(PositionRegex("""/maps/place/$LAT,$LON/@[\d.,+-]+,${Z}z.*"""))
                path(PositionRegex("""/maps/place/.*/@$LAT,$LON,${Z}z.*"""))
                path(PositionRegex("""/maps/place/.*/@$LAT,$LON.*"""))
                path(PositionRegex("""/maps/place/$LAT,$LON.*"""))
                path(PositionRegex("""/maps/place/$Q_PATH.*"""))
                path(PositionRegex("""/maps/place//.*"""))
                path(PositionRegex("""/maps/placelists/list/.*"""))
                path(PositionRegex("""/maps/search/$LAT,$LON.*"""))
                path(PositionRegex("""/maps/search/$Q_PATH.*"""))
                path(PositionRegex("""/maps/search/"""))
                path(PositionRegex("""/maps/dir/.*/$LAT,$LON/@[\d.,+-]+,${Z}z/?[^/]*"""))
                path(PositionRegex("""/maps/dir/.*/$LAT,$LON/data[^/]*"""))
                path(PositionRegex("""/maps/dir/.*/$LAT,$LON/?"""))
                path(PositionRegex("""/maps/dir/.*/@$LAT,$LON,${Z}z/?[^/]*"""))
                path(PositionRegex("""/maps/dir/.*/$Q_PATH/data[^/]*"""))
                path(PositionRegex("""/maps/dir/.*/$Q_PATH/?"""))
                path(PositionRegex("""/maps/dir/"""))
                all {
                    path(PositionRegex("""/maps/d/(edit|viewer)"""))
                    query("mid", PositionRegex(".+"))
                }
                path(PositionRegex("""/maps/?"""))
                path(PositionRegex("""/search/?"""))
                path(PositionRegex("""/?"""))
            }
        }
    }

    override val conversionHtmlPattern = htmlPattern {
        content(PositionRegex("""/@$LAT,$LON"""))
        content(PointsPositionRegex("""\[(null,null,|null,\[)$LAT,$LON\]"""))
    }

    override val conversionHtmlRedirectPattern = htmlPattern {
        content(RedirectRegex("""data-url="(?P<url>[^"]+)""""))
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
}
