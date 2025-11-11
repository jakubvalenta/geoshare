package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import kotlinx.io.Source
import kotlinx.io.readLine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionMatch
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT_NUM
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON_NUM
import page.ooooo.geoshare.lib.PositionMatch.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionMatch.Companion.Q_PATH
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.RedirectMatch
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.conversionPattern
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.findAll
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matches
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
                on { scheme matches Z } doReturn { PositionMatch.Zoom(it, srs) }
                on { queryParams["zoom"]?.let { it matches Z } } doReturn { PositionMatch.Zoom(it, srs) }
                on { path matches """/maps/.*/@[\d.,+-]+,${Z}z.*""" } doReturn { PositionMatch.Zoom(it, srs) }
                first {
                    on { queryParams["destination"]?.let { it matches "$LAT,$LON" } } doReturn
                        { PositionMatch.LatLon(it, srs) }
                    on { queryParams["destination"]?.let { it matches Q_PARAM } } doReturn
                        { PositionMatch.LatLon(it, srs) }
                    on { queryParams["q"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch.LatLon(it, srs) }
                    on { queryParams["q"]?.let { it matches Q_PARAM } } doReturn { PositionMatch.Query(it, srs) }
                    on { queryParams["query"]?.let { it matches "$LAT,$LON" } } doReturn
                        { PositionMatch.LatLon(it, srs) }
                    on { queryParams["query"]?.let { it matches Q_PARAM } } doReturn { PositionMatch.Query(it, srs) }
                    on { queryParams["viewpoint"]?.let { it matches "$LAT,$LON" } } doReturn
                        { PositionMatch.LatLon(it, srs) }
                    on { queryParams["center"]?.let { it matches "$LAT,$LON" } } doReturn
                        { PositionMatch.LatLon(it, srs) }
                }
            }
            first {
                onEach {
                    sequence {
                        (path matches """/maps/.*/$DATA""")?.groupOrNull("data")?.let { data ->
                            (data find """!3d$LAT!4d$LON""")?.let { m ->
                                yield(m)
                                return@sequence
                            }
                            yieldAll(data findAll """!1d$LON!2d$LAT""")
                        }
                    }
                } doReturn { PositionMatch.LatLon(it, srs) }
                on { path matches """/maps/@$LAT,$LON,${Z}z.*""" } doReturn { PositionMatch.LatLonZ(it, srs) }
                on { path matches """/maps/@$LAT,$LON.*""" } doReturn { PositionMatch.LatLon(it, srs) }
                on { path matches """/maps/@""" } doReturn { PositionMatch.Empty(it, srs) }
                on { path matches """/maps/place/$LAT,$LON/@[\d.,+-]+,${Z}z.*""" } doReturn
                    { PositionMatch.LatLonZ(it, srs) }
                on { path matches """/maps/place/.*/@$LAT,$LON,${Z}z.*""" } doReturn { PositionMatch.LatLonZ(it, srs) }
                on { path matches """/maps/place/.*/@$LAT,$LON.*""" } doReturn { PositionMatch.LatLon(it, srs) }
                on { path matches """/maps/place/$LAT,$LON.*""" } doReturn { PositionMatch.LatLon(it, srs) }
                on { path matches """/maps/place/$Q_PATH.*""" } doReturn { PositionMatch.Query(it, srs) }
                on { path matches """/maps/place//.*""" } doReturn { PositionMatch.Empty(it, srs) }
                on { path matches """/maps/placelists/list/.*""" } doReturn { PositionMatch.Empty(it, srs) }
                on { path matches """/maps/@/data=!3m1!4b1!4m3!11m2!2s.+!3e3""" } doReturn
                    { PositionMatch.Empty(it, srs) }
                on { path matches """/maps/search/$LAT,$LON.*""" } doReturn { PositionMatch.LatLon(it, srs) }
                on { path matches """/maps/search/$Q_PATH.*""" } doReturn { PositionMatch.Query(it, srs) }
                on { path matches """/maps/search/""" } doReturn { PositionMatch.Empty(it, srs) }
                on { path matches """/maps/dir/.*/$LAT,$LON/@[\d.,+-]+,${Z}z/?[^/]*""" } doReturn
                    { PositionMatch.LatLonZ(it, srs) }
                on { path matches """/maps/dir/.*/$LAT,$LON/data[^/]*""" } doReturn { PositionMatch.LatLon(it, srs) }
                on { path matches """/maps/dir/.*/$LAT,$LON/?""" } doReturn { PositionMatch.LatLon(it, srs) }
                on { path matches """/maps/dir/.*/@$LAT,$LON,${Z}z/?[^/]*""" } doReturn
                    { PositionMatch.LatLonZ(it, srs) }
                on { path matches """/maps/dir/.*/$Q_PATH/data[^/]*""" } doReturn { PositionMatch.Query(it, srs) }
                on { path matches """/maps/dir/.*/$Q_PATH/?""" } doReturn { PositionMatch.Query(it, srs) }
                on { path matches """/maps/dir/""" } doReturn { PositionMatch.Empty(it, srs) }
                on { if ((path matches """/maps/d/(edit|viewer)""") != null) queryParams["mid"]?.let { it matches ".+" } else null } doReturn
                    { PositionMatch.Empty(it, srs) }
                on { path matches """/maps/?""" } doReturn { PositionMatch.Empty(it, srs) }
                on { path matches """/search/?""" } doReturn { PositionMatch.Empty(it, srs) }
                on { path matches """/?""" } doReturn { PositionMatch.Empty(it, srs) }
            }
        }
    }

    override val conversionHtmlPattern = conversionPattern<Source, PositionMatch> {
        onEach {
            sequence {
                val linkPattern = Pattern.compile("""/@$LAT,$LON""")
                val pointPatterns = listOf(
                    Pattern.compile("""APP_INITIALIZATION_STATE=\[\[\[[\d.-]+,$LON,$LAT"""),
                    Pattern.compile("""\[(null,null,|null,\[)$LAT,$LON\]"""),
                )
                for (line in generateSequence { this@onEach.readLine() }) {
                    (line find linkPattern)?.let { m ->
                        yield(m)
                        break
                    }
                    pointPatterns.forEach { pattern ->
                        yieldAll(line findAll pattern)
                    }
                }
            }
        } doReturn { PositionMatch.LatLon(it, srs) }
    }

    override val conversionHtmlRedirectPattern = conversionPattern<String, RedirectMatch> {
        on { this find """data-url="(?P<url>[^"]+)"""" } doReturn { RedirectMatch(it) }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
}
