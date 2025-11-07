package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT_NUM
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON_NUM
import page.ooooo.geoshare.lib.PositionMatch.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionMatch.Companion.Q_PATH
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z

class GoogleMapsUrlConverter() : UrlConverter.WithUriPattern, UrlConverter.WithShortUriPattern,
    UrlConverter.WithHtmlPattern {

    companion object {
        const val NAME = "Google Maps"
        const val SHORT_URL = """((maps\.)?(app\.)?goo\.gl|g\.co)/[/A-Za-z0-9_-]+"""
        const val DATA = """data=(?P<data>.*(!3d$LAT_NUM!4d$LON_NUM|!1d$LON_NUM!2d$LAT_NUM).*)"""
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
    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        all {
            optional {
                on { scheme matches Z } doReturn { PositionMatch(it) }
                on { queryParams["zoom"]?.let { it matches Z } } doReturn { PositionMatch(it) }
                first {
                    on { queryParams["destination"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                    on { queryParams["destination"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it) }
                    on { queryParams["q"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                    on { queryParams["q"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it) }
                    on { queryParams["query"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                    on { queryParams["query"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it) }
                    on { queryParams["viewpoint"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                    on { queryParams["center"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                }
            }
            first {
                on { path matches """/maps/.*/@[\d.,+-]+,${Z}z/$DATA""" } doReturn { DataPointsPositionMatch(it) }
                on { path matches """/maps/.*/$DATA""" } doReturn { DataPointsPositionMatch(it) }
                on { path matches """/maps/@$LAT,$LON,${Z}z.*""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/@$LAT,$LON.*""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/@""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/place/$LAT,$LON/@[\d.,+-]+,${Z}z.*""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/place/.*/@$LAT,$LON,${Z}z.*""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/place/.*/@$LAT,$LON.*""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/place/$LAT,$LON.*""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/place/$Q_PATH.*""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/place//.*""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/placelists/list/.*""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/@/data=!3m1!4b1!4m3!11m2!2s.+!3e3""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/search/$LAT,$LON.*""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/search/$Q_PATH.*""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/search/""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/dir/.*/$LAT,$LON/@[\d.,+-]+,${Z}z/?[^/]*""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/dir/.*/$LAT,$LON/data[^/]*""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/dir/.*/$LAT,$LON/?""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/dir/.*/@$LAT,$LON,${Z}z/?[^/]*""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/dir/.*/$Q_PATH/data[^/]*""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/dir/.*/$Q_PATH/?""" } doReturn { PositionMatch(it) }
                on { path matches """/maps/dir/""" } doReturn { PositionMatch(it) }
                on { if ((path matches """/maps/d/(edit|viewer)""") != null) queryParams["mid"]?.let { it matches ".+" } else null } doReturn
                        { PositionMatch(it) }
                on { path matches """/maps/?""" } doReturn { PositionMatch(it) }
                on { path matches """/search/?""" } doReturn { PositionMatch(it) }
                on { path matches """/?""" } doReturn { PositionMatch(it) }
            }
        }
    }

    override val conversionHtmlPattern = conversionPattern<String, PositionMatch> {
        on { this find """/@$LAT,$LON""" } doReturn { PositionMatch(it) }
        on { this find """\[(null,null,|null,\[)$LAT,$LON\]""" } doReturn { PointsPositionMatch(it) }
        on { this find """APP_INITIALIZATION_STATE=\[\[\[[\d.-]+,$LON,$LAT""" } doReturn { PositionMatch(it) }
    }

    override val conversionHtmlRedirectPattern = conversionPattern<String, RedirectMatch> {
        on { this find """data-url="(?P<url>[^"]+)"""" } doReturn { RedirectMatch(it) }
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
