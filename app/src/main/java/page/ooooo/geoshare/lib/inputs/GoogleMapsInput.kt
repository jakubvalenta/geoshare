package page.ooooo.geoshare.lib.inputs

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
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Srs

object GoogleMapsInput : Input.HasUri, Input.HasShortUri, Input.HasHtml {
    const val NAME = "Google Maps"
    private const val SHORT_URL = """((maps\.)?(app\.)?goo\.gl|g\.co)/[/A-Za-z0-9_-]+"""
    private const val DATA = """data=(?P<data>.*(!3d$LAT_NUM!4d$LON_NUM|!1d$LON_NUM!2d$LAT_NUM).*)"""

    private val srs = Srs.GCJ02

    override val uriPattern: Pattern =
        Pattern.compile("""(https?://)?((www|maps)\.)?(google(\.[a-z]{2,3})?\.[a-z]{2,3}[/?#]\S+|$SHORT_URL)""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_google_maps_name,
        inputs = listOf(
            Input.DocumentationInput.Url(5, "https://maps.app.goo.gl"),
            Input.DocumentationInput.Url(5, "https://app.goo.gl/maps"),
            Input.DocumentationInput.Url(5, "https://maps.google.com"),
            Input.DocumentationInput.Url(5, "https://goo.gl/maps"),
            Input.DocumentationInput.Url(5, "https://google.com/maps"),
            Input.DocumentationInput.Url(5, "https://www.google.com/maps"),
            Input.DocumentationInput.Url(10, "https://g.co/kgs"),
        ),
    )
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?$SHORT_URL""")
    override val shortUriMethod = Input.ShortUriMethod.HEAD

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        all {
            optional {
                on { scheme matches Z } doReturn { PositionMatch(it, srs) }
                on { queryParams["zoom"]?.let { it matches Z } } doReturn { PositionMatch(it, srs) }
                first {
                    on { queryParams["destination"]?.let { it matches "$LAT,$LON" } } doReturn {
                        PositionMatch(
                            it,
                            srs
                        )
                    }
                    on { queryParams["destination"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it, srs) }
                    on { queryParams["q"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it, srs) }
                    on { queryParams["q"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it, srs) }
                    on { queryParams["query"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it, srs) }
                    on { queryParams["query"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it, srs) }
                    on { queryParams["viewpoint"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it, srs) }
                    on { queryParams["center"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it, srs) }
                }
            }
            first {
                on { path matches """/maps/.*/@[\d.,+-]+,${Z}z/$DATA""" } doReturn { DataPointsPositionMatch(it, srs) }
                on { path matches """/maps/.*/$DATA""" } doReturn { DataPointsPositionMatch(it, srs) }
                on { path matches """/maps/@$LAT,$LON,${Z}z.*""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/@$LAT,$LON.*""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/@""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/place/$LAT,$LON/@[\d.,+-]+,${Z}z.*""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/place/.*/@$LAT,$LON,${Z}z.*""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/place/.*/@$LAT,$LON.*""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/place/$LAT,$LON.*""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/place/$Q_PATH.*""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/place//.*""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/placelists/list/.*""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/@/data=!3m1!4b1!4m3!11m2!2s.+!3e3""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/search/$LAT,$LON.*""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/search/$Q_PATH.*""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/search/""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/dir/.*/$LAT,$LON/@[\d.,+-]+,${Z}z/?[^/]*""" } doReturn
                    { PositionMatch(it, srs) }
                on { path matches """/maps/dir/.*/$LAT,$LON/data[^/]*""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/dir/.*/$LAT,$LON/?""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/dir/.*/@$LAT,$LON,${Z}z/?[^/]*""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/dir/.*/$Q_PATH/data[^/]*""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/dir/.*/$Q_PATH/?""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/dir/""" } doReturn { PositionMatch(it, srs) }
                on { if ((path matches """/maps/d/(edit|viewer)""") != null) queryParams["mid"]?.let { it matches ".+" } else null } doReturn
                    { PositionMatch(it, srs) }
                on { path matches """/maps/?""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/search/?""" } doReturn { PositionMatch(it, srs) }
                on { path matches """/?""" } doReturn { PositionMatch(it, srs) }
            }
        }
    }

    override val conversionHtmlPattern = conversionPattern<String, PositionMatch> {
        on { this find """/@$LAT,$LON""" } doReturn { PositionMatch(it, srs) }
        on { this find """\[(null,null,|null,\[)$LAT,$LON\]""" } doReturn { PointsPositionMatch(it, srs) }
        on { this find """APP_INITIALIZATION_STATE=\[\[\[[\d.-]+,$LON,$LAT""" } doReturn { PositionMatch(it, srs) }
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
    private class DataPointsPositionMatch(matcher: Matcher, srs: Srs) : PositionMatch(matcher, srs) {
        var dataPatternsCache: List<Pattern>? = null
        val dataPatterns: List<Pattern>
            get() = dataPatternsCache ?: listOf<Pattern>(
                Pattern.compile("""!3d$LAT!4d$LON"""),
                Pattern.compile("""!1d$LON!2d$LAT"""),
            ).also { dataPatternsCache = it }

        override val points
            get() = matcher.groupOrNull("data")?.let { data ->
                buildList {
                    dataPatterns.forEach { dataPattern ->
                        dataPattern.matcher(data).let { m ->
                            while (m.find()) {
                                val lat = m.groupOrNull("lat")?.toDoubleOrNull() ?: continue
                                val lon = m.groupOrNull("lon")?.toDoubleOrNull() ?: continue
                                add(Point(srs, lat, lon))
                            }
                        }
                    }
                }
            }
    }
}
