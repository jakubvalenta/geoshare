package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.conversion.ConversionPattern
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LAT
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LAT_LON_PATTERN
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LAT_NUM
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LON
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LON_NUM
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.Q_PARAM_PATTERN
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.Q_PATH
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.Z
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.Z_PATTERN
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.findAll
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.Srs

object GoogleMapsInput : Input.HasShortUri, Input.HasHtml {
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

    override val conversionUriPattern = ConversionPattern.uriPattern(srs) {
        addPointsFromSequenceOfMatchers {
            sequence {
                ("""/maps/.*/$DATA""" match path)?.groupOrNull("data")?.let { data ->
                    ("""!3d$LAT!4d$LON""" find data)?.let {
                        yield(it)
                        return@sequence
                    }
                    yieldAll("""!1d$LON!2d$LAT""" findAll data)
                }
            }
        }
        setPointAndZoomFromMatcher { """/maps/@$LAT,$LON,${Z}z.*""" match path }
        setPointFromMatcher { """/maps/@$LAT,$LON.*""" match path }
        setPointAndZoomFromMatcher { """/maps/place/$LAT,$LON/@[\d.,+-]+,${Z}z.*""" match path }
        setPointAndZoomFromMatcher { """/maps/place/.*/@$LAT,$LON,${Z}z.*""" match path }
        setPointFromMatcher { """/maps/place/.*/@$LAT,$LON.*""" match path }
        setPointFromMatcher { """/maps/place/$LAT,$LON.*""" match path }
        setPointFromMatcher { """/maps/search/$LAT,$LON.*""" match path }
        setPointAndZoomFromMatcher { """/maps/dir/.*/$LAT,$LON/@[\d.,+-]+,${Z}z/?[^/]*""" match path }
        setPointFromMatcher { """/maps/dir/.*/$LAT,$LON/data[^/]*""" match path }
        setPointFromMatcher { """/maps/dir/.*/$LAT,$LON/?""" match path }
        setPointAndZoomFromMatcher { """/maps/dir/.*/@$LAT,$LON,${Z}z/?[^/]*""" match path }
        setPointFromMatcher { LAT_LON_PATTERN match queryParams["destination"] }
        setPointFromMatcher { LAT_LON_PATTERN match queryParams["q"] }
        setPointFromMatcher { LAT_LON_PATTERN match queryParams["query"] }
        setPointFromMatcher { LAT_LON_PATTERN match queryParams["viewpoint"] }
        setPointFromMatcher { LAT_LON_PATTERN match queryParams["center"] }
        setQueryFromMatcher { Q_PARAM_PATTERN match queryParams["destination"] }
        setQueryFromMatcher { Q_PARAM_PATTERN match queryParams["q"] }
        setQueryFromMatcher { Q_PARAM_PATTERN match queryParams["query"] }
        setQueryFromMatcher { """/maps/place/$Q_PATH.*""" match path }
        setQueryFromMatcher { """/maps/search/$Q_PATH.*""" match path }
        setQueryFromMatcher { """/maps/dir/.*/$Q_PATH/data[^/]*""" match path }
        setQueryFromMatcher { """/maps/dir/.*/$Q_PATH/?""" match path }
        setUrl { if (("""/?""" match path) != null) this.toUrl() else null }
        setUrl { if (("""/maps/?""" match path) != null) this.toUrl() else null }
        setUrl { if (("""/maps/@""" match path) != null) this.toUrl() else null }
        setUrl { if (("""/maps/@/data=!3m1!4b1!4m3!11m2!2s.+!3e3""" match path) != null) this.toUrl() else null }
        setUrl { if (("""/maps/dir/.*""" match path) != null) this.toUrl() else null }
        setUrl { if (("""/maps/place/.*""" match path) != null) this.toUrl() else null }
        setUrl { if (("""/maps/placelists/list/.*""" match path) != null) this.toUrl() else null }
        setUrl { if (("""/maps/search/.*""" match path) != null) this.toUrl() else null }
        setUrl { if (("""/search/?""" match path) != null) this.toUrl() else null }
        setUrl { if (("""/maps/d/(edit|viewer)""" match path) != null && !queryParams["mid"].isNullOrEmpty()) this.toUrl() else null }
        setZoomFromMatcher { Z_PATTERN match scheme }
        setZoomFromMatcher { Z_PATTERN match queryParams["zoom"] }
    }

    override val conversionHtmlPattern = ConversionPattern.htmlPattern(srs) {
        val pointPattern = Pattern.compile("""\[(null,null,|null,\[)$LAT,$LON\]""")
        val defaultPointPattern1 = Pattern.compile("""/@$LAT,$LON""")
        val defaultPointPattern2 = Pattern.compile("""APP_INITIALIZATION_STATE=\[\[\[[\d.-]+,$LON,$LAT""")
        val uriPattern = Pattern.compile("""data-url="(?P<url>[^"]+)"""")
        forEachLine {
            addPointsFromSequenceOfMatchers { pointPattern findAll this }
            setDefaultPointFromMatcher { defaultPointPattern1 find this }
            setDefaultPointFromMatcher { defaultPointPattern2 find this }
            setUrlFromMatcher { uriPattern find this }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
}
