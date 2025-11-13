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

    override val conversionUriPattern = ConversionPattern.uriPattern(srs) {
        pointsSequence {
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
        pointsAndZoom { """/maps/@$LAT,$LON,${Z}z.*""" match path }
        points { """/maps/@$LAT,$LON.*""" match path }
        pointsAndZoom { """/maps/place/$LAT,$LON/@[\d.,+-]+,${Z}z.*""" match path }
        pointsAndZoom { """/maps/place/.*/@$LAT,$LON,${Z}z.*""" match path }
        points { """/maps/place/.*/@$LAT,$LON.*""" match path }
        points { """/maps/place/$LAT,$LON.*""" match path }
        points { """/maps/search/$LAT,$LON.*""" match path }
        pointsAndZoom { """/maps/dir/.*/$LAT,$LON/@[\d.,+-]+,${Z}z/?[^/]*""" match path }
        points { """/maps/dir/.*/$LAT,$LON/data[^/]*""" match path }
        points { """/maps/dir/.*/$LAT,$LON/?""" match path }
        pointsAndZoom { """/maps/dir/.*/@$LAT,$LON,${Z}z/?[^/]*""" match path }
        points { LAT_LON_PATTERN match queryParams["destination"] }
        points { LAT_LON_PATTERN match queryParams["q"] }
        points { LAT_LON_PATTERN match queryParams["query"] }
        points { LAT_LON_PATTERN match queryParams["viewpoint"] }
        points { LAT_LON_PATTERN match queryParams["center"] }
        query { Q_PARAM_PATTERN match queryParams["destination"] }
        query { Q_PARAM_PATTERN match queryParams["q"] }
        query { Q_PARAM_PATTERN match queryParams["query"] }
        query { """/maps/place/$Q_PATH.*""" match path }
        query { """/maps/search/$Q_PATH.*""" match path }
        query { """/maps/dir/.*/$Q_PATH/data[^/]*""" match path }
        query { """/maps/dir/.*/$Q_PATH/?""" match path }
        uriString { ("""/?""" match path)?.let { this } }
        uriString { ("""/maps/?""" match path)?.let { this } }
        uriString { ("""/maps/@""" match path)?.let { this } }
        uriString { ("""/maps/@/data=!3m1!4b1!4m3!11m2!2s.+!3e3""" match path)?.let { this } }
        uriString { ("""/maps/dir/.*""" match path)?.let { this } }
        uriString { ("""/maps/place/.*""" match path)?.let { this } }
        uriString { ("""/maps/placelists/list/.*""" match path)?.let { this } }
        uriString { ("""/maps/search/.*""" match path)?.let { this } }
        uriString { ("""/search/?""" match path)?.let { this } }
        uriString { if (("""/maps/d/(edit|viewer)""" match path) != null && !queryParams["mid"].isNullOrEmpty()) this else null }
        zoom { Z_PATTERN match scheme }
        zoom { Z_PATTERN match queryParams["zoom"] }
    }

    override val conversionHtmlPattern = ConversionPattern.htmlPattern(srs) {
        val pointPattern = Pattern.compile("""\[(null,null,|null,\[)$LAT,$LON\]""")
        val defaultPointPattern1 = Pattern.compile("""/@$LAT,$LON""")
        val defaultPointPattern2 = Pattern.compile("""APP_INITIALIZATION_STATE=\[\[\[[\d.-]+,$LON,$LAT""")
        val uriPattern = Pattern.compile("""data-url="(?P<url>[^"]+)"""")
        forEachLine {
            point { pointPattern find this }
            defaultPoints { defaultPointPattern1 find this }
            defaultPoints { defaultPointPattern2 find this }
            uriString { uriPattern find this }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
}
