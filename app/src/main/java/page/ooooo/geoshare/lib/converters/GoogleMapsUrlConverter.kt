package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT_NUM
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON_NUM
import page.ooooo.geoshare.lib.PositionMatch.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionMatch.Companion.Q_PATH
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z

class GoogleMapsUrlConverter() :
    UrlConverter.WithUriPattern,
    UrlConverter.WithShortUriPattern,
    UrlConverter.WithHtmlPattern {

    companion object {
        const val NAME = "Google Maps"
        const val SHORT_URL = """((maps\.)?(app\.)?goo\.gl|g\.co)/[/A-Za-z0-9_-]+"""
        const val DATA = """data=(?P<data>.*(!3d$LAT_NUM!4d$LON_NUM|!1d$LON_NUM!2d$LAT_NUM).*)"""

        /**
         * See https://developers.google.com/maps/documentation/urls/get-started
         */
        fun formatUriString(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String = Uri(
            scheme = "https",
            host = "www.google.com",
            path = "/maps",
            queryParams = buildMap {
                position.apply {
                    mainPoint?.let { (lat, lon) ->
                        set("q", "$lat,$lon")
                    } ?: q?.let { q ->
                        set("q", q)
                    }
                    z?.let { z ->
                        set("z", z)
                    }
                }
            }.toImmutableMap(),
            uriQuote = uriQuote,
        ).toString()
    }

    override val uriPattern: Pattern =
        Pattern.compile("""(https?://)?((www|maps)\.)?(google(\.[a-z]{2,3})?\.[a-z]{2,3}[/?#]\S+|$SHORT_URL)""")
    override val documentation = Documentation(
        nameResId = R.string.converter_google_maps_name,
        inputs = listOf(
            DocumentationInput.Url(5, "https://maps.app.goo.gl"),
            DocumentationInput.Url(5, "https://app.goo.gl/maps"),
            DocumentationInput.Url(5, "https://maps.google.com"),
            DocumentationInput.Url(5, "https://goo.gl/maps"),
            DocumentationInput.Url(5, "https://google.com/maps"),
            DocumentationInput.Url(5, "https://www.google.com/maps"),
            DocumentationInput.Url(10, "https://g.co/kgs"),
        ),
    )
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?$SHORT_URL""")
    override val shortUriMethod = ShortUriMethod.HEAD

    @Suppress("SpellCheckingInspection")
    override val conversionUriPattern = conversionPattern {
        all {
            optional {
                onUri { scheme matcherIfMatches Z } doReturn { PositionMatch(it) }
                onUri { queryParams["zoom"]?.let { it matcherIfMatches Z } } doReturn { PositionMatch(it) }
                first {
                    onUri { queryParams["destination"]?.let { it matcherIfMatches "$LAT,$LON" } } doReturn
                            { PositionMatch(it) }
                    onUri { queryParams["destination"]?.let { it matcherIfMatches Q_PARAM } } doReturn
                            { PositionMatch(it) }
                    onUri { queryParams["q"]?.let { it matcherIfMatches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                    onUri { queryParams["q"]?.let { it matcherIfMatches Q_PARAM } } doReturn { PositionMatch(it) }
                    onUri { queryParams["query"]?.let { it matcherIfMatches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                    onUri { queryParams["query"]?.let { it matcherIfMatches Q_PARAM } } doReturn { PositionMatch(it) }
                    onUri { queryParams["viewpoint"]?.let { it matcherIfMatches "$LAT,$LON" } } doReturn
                            { PositionMatch(it) }
                    onUri { queryParams["center"]?.let { it matcherIfMatches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                }
            }
            first {
                onUri { path matcherIfMatches """/maps/.*/@[\d.,+-]+,${Z}z/$DATA""" } doReturn
                        { DataPointsPositionMatch(it) }
                onUri { path matcherIfMatches """/maps/.*/$DATA""" } doReturn { DataPointsPositionMatch(it) }
                onUri { path matcherIfMatches """/maps/@$LAT,$LON,${Z}z.*""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/@$LAT,$LON.*""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/@""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/place/$LAT,$LON/@[\d.,+-]+,${Z}z.*""" } doReturn
                        { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/place/.*/@$LAT,$LON,${Z}z.*""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/place/.*/@$LAT,$LON.*""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/place/$LAT,$LON.*""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/place/$Q_PATH.*""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/place//.*""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/placelists/list/.*""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/@/data=!3m1!4b1!4m3!11m2!2s.+!3e3""" } doReturn
                        { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/search/$LAT,$LON.*""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/search/$Q_PATH.*""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/search/""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/dir/.*/$LAT,$LON/@[\d.,+-]+,${Z}z/?[^/]*""" } doReturn
                        { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/dir/.*/$LAT,$LON/data[^/]*""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/dir/.*/$LAT,$LON/?""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/dir/.*/@$LAT,$LON,${Z}z/?[^/]*""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/dir/.*/$Q_PATH/data[^/]*""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/dir/.*/$Q_PATH/?""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/dir/""" } doReturn { PositionMatch(it) }
                all {
                    onUri { path matcherIfMatches """/maps/d/(edit|viewer)""" } doReturn { PositionMatch(it) }
                    onUri { queryParams["mid"]?.let { it matcherIfMatches ".+" } } doReturn { PositionMatch(it) }
                }
                onUri { path matcherIfMatches """/maps/?""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/search/?""" } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/?""" } doReturn { PositionMatch(it) }
            }
        }
    }

    override val conversionHtmlPattern = conversionPattern {
        onHtml { this matcherIfFind """/@$LAT,$LON""" } doReturn { PositionMatch(it) }
        onHtml { this matcherIfFind """\[(null,null,|null,\[)$LAT,$LON\]""" } doReturn { PointsPositionMatch(it) }
    }

    override val conversionHtmlRedirectPattern = conversionPattern {
        onHtml { this matcherIfFind """data-url="(?P<url>[^"]+)"""" } doReturn { RedirectMatch(it) }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title

    /**
     * Repeatedly searches for LAT and LON in DATA to get points
     */
    private class DataPointsPositionMatch(matcher: Matcher) : PositionMatch(matcher) {
        var dataPatternsCache: List<Pattern>? = null
        val dataPatterns: List<Pattern>
            get() = dataPatternsCache ?: listOf<Pattern>(
                Pattern.compile("""!3d$LAT!4d$LON"""),
                Pattern.compile("""!1d$LON!2d$LAT"""),
            ).also { dataPatternsCache = it }

        override val points: List<Point>?
            get() = matcher.groupOrNull("data")?.let { data ->
                buildList {
                    dataPatterns.forEach { dataPattern ->
                        dataPattern.matcher(data).let { m ->
                            while (m.find()) {
                                try {
                                    add(Point(m.group("lat"), m.group("lon")))
                                } catch (_: IllegalArgumentException) {
                                    // Do nothing
                                }
                            }
                        }
                    }
                }
            }
    }
}
