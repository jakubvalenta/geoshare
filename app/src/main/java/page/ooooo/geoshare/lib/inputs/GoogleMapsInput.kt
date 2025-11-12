package page.ooooo.geoshare.lib.inputs

import android.R.attr.data
import android.R.attr.path
import android.R.attr.scheme
import androidx.annotation.StringRes
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.io.Source
import kotlinx.io.readLine
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
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.findAll
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.*

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

    override val conversionUriPattern = ConversionPattern<Uri>(srs) {
        val data = ("""/maps/.*/$DATA""" match path)?.groupOrNull("data")

        latLonAll { """!1d$LON!2d$LAT""" findAll data }
        latLon { """!3d$LAT!4d$LON""" find data }
        latLonZ { """/maps/@$LAT,$LON,${Z}z.*""" match path }
        latLon { """/maps/@$LAT,$LON.*""" match path }
        latLonZ { """/maps/place/$LAT,$LON/@[\d.,+-]+,${Z}z.*""" match path }
        latLonZ { """/maps/place/.*/@$LAT,$LON,${Z}z.*""" match path }
        latLon { """/maps/place/.*/@$LAT,$LON.*""" match path }
        latLon { """/maps/place/$LAT,$LON.*""" match path }
        latLon { """/maps/search/$LAT,$LON.*""" match path }
        latLonZ { """/maps/dir/.*/$LAT,$LON/@[\d.,+-]+,${Z}z/?[^/]*""" match path }
        latLon { """/maps/dir/.*/$LAT,$LON/data[^/]*""" match path }
        latLon { """/maps/dir/.*/$LAT,$LON/?""" match path }
        latLonZ { """/maps/dir/.*/@$LAT,$LON,${Z}z/?[^/]*""" match path }
        latLon { LAT_LON_PATTERN match queryParams["destination"] }
        latLon { LAT_LON_PATTERN match queryParams["q"] }
        latLon { LAT_LON_PATTERN match queryParams["query"] }
        latLon { LAT_LON_PATTERN match queryParams["viewpoint"] }
        latLon { LAT_LON_PATTERN match queryParams["center"] }
        q { Q_PARAM_PATTERN match queryParams["destination"] }
        q { Q_PARAM_PATTERN match queryParams["q"] }
        q { Q_PARAM_PATTERN match queryParams["query"] }
        q { """/maps/place/$Q_PATH.*""" match path }
        q { """/maps/search/$Q_PATH.*""" match path }
        q { """/maps/dir/.*/$Q_PATH/data[^/]*""" match path }
        q { """/maps/dir/.*/$Q_PATH/?""" match path }
        htmlUri { ("""/?""" match path)?.let { this } }
        htmlUri { ("""/maps/?""" match path)?.let { this } }
        htmlUri { ("""/maps/@""" match path)?.let { this } }
        htmlUri { ("""/maps/@/data=!3m1!4b1!4m3!11m2!2s.+!3e3""" match path)?.let { this } }
        htmlUri { ("""/maps/dir/.*""" match path)?.let { this } }
        htmlUri { ("""/maps/place/.*""" match path)?.let { this } }
        htmlUri { ("""/maps/placelists/list/.*""" match path)?.let { this } }
        htmlUri { ("""/maps/search/.*""" match path)?.let { this } }
        htmlUri { ("""/search/?""" match path)?.let { this } }
        htmlUri { if (("""/maps/d/(edit|viewer)""" match path) != null && !queryParams["mid"].isNullOrEmpty()) this else null }
        z { Z_PATTERN match scheme }
        z { Z_PATTERN match queryParams["zoom"] }
    }

    override val conversionHtmlPattern = ConversionPattern<Source>(srs) {
        latLonAll {
            val mainPointPatterns = listOf(
                Pattern.compile("""/@$LAT,$LON"""),
                Pattern.compile("""APP_INITIALIZATION_STATE=\[\[\[[\d.-]+,$LON,$LAT"""),
            )
            val extraPointPattern = Pattern.compile("""\[(null,null,|null,\[)$LAT,$LON\]""")
            val redirectPattern = Pattern.compile("""data-url="(?P<url>[^"]+)"""")
            var mainPosition: Position? = null
            val extraPositions: MutableList<Position> = mutableListOf()
            var redirectUriString: String? = null
            for (line in generateSequence { this@listPattern.readLine() }) {
                if (mainPosition == null) {
                    mainPointPatterns
                        .firstNotNullOfOrNull { pattern -> pattern find line }?.toLatLon(srs)
                        ?.let { mainPosition = it }
                }
                if (redirectUriString == null) {
                    (redirectPattern find line)?.groupOrNull("url")?.let { redirectUriString = it }
                }
                (extraPointPattern findAll line)
                    .mapNotNull { m -> m.toLatLon(srs) }
                    .forEach { extraPositions.add(it) }
            }
            if (extraPositions.isNotEmpty()) {
                // If the URI is a place list, ignore the APP_INITIALIZATION_STATE point and only take the other points
                extraPositions.map { ConversionHtmlResult.Finished(it) }
            } else if (mainPosition != null) {
                listOf(ConversionHtmlResult.Finished(mainPosition))
            } else if (redirectUriString != null) {
                listOf(ConversionHtmlResult.RequiresRedirect(redirectUriString))
            } else {
                emptyList()
            }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
}
