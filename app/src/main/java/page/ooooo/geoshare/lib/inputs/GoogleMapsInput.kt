package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import kotlinx.io.Source
import kotlinx.io.readLine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ConversionPattern
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LAT
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LAT_LON_PATTERN
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LAT_NUM
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LON
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LON_NUM
import page.ooooo.geoshare.lib.ConversionPattern.Companion.Q_PARAM_PATTERN
import page.ooooo.geoshare.lib.ConversionPattern.Companion.Q_PATH
import page.ooooo.geoshare.lib.ConversionPattern.Companion.Z
import page.ooooo.geoshare.lib.ConversionPattern.Companion.Z_PATTERN
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.findAll
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matches
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

    override val conversionUriPattern = ConversionPattern.first<Uri, Position> {
        all {
            optional {
                first {
                    pattern { (scheme matches Z_PATTERN)?.toZ(srs) }
                    pattern { queryParams["zoom"]?.let { it matches Z_PATTERN }?.toZ(srs) }
                    pattern { (path matches """/maps/.*/@[\d.,+-]+,${Z}z.*""")?.toZ(srs) }
                }
                first {
                    pattern { queryParams["destination"]?.let { it matches LAT_LON_PATTERN }?.toLatLon(srs) }
                    pattern { queryParams["destination"]?.let { it matches Q_PARAM_PATTERN }?.toQ(srs) }
                    pattern { queryParams["q"]?.let { it matches LAT_LON_PATTERN }?.toLatLon(srs) }
                    pattern { queryParams["q"]?.let { it matches Q_PARAM_PATTERN }?.toQ(srs) }
                    pattern { queryParams["query"]?.let { it matches LAT_LON_PATTERN }?.toLatLon(srs) }
                    pattern { queryParams["query"]?.let { it matches Q_PARAM_PATTERN }?.toQ(srs) }
                    pattern { queryParams["viewpoint"]?.let { it matches LAT_LON_PATTERN }?.toLatLon(srs) }
                    pattern { queryParams["center"]?.let { it matches LAT_LON_PATTERN }?.toLatLon(srs) }
                }
            }
            first {
                listPattern {
                    sequence {
                        (path matches """/maps/.*/$DATA""")?.groupOrNull("data")?.let { data ->
                            (data find """!3d$LAT!4d$LON""")?.let { m ->
                                yield(m)
                                return@sequence
                            }
                            yieldAll(data findAll """!1d$LON!2d$LAT""")
                        }
                    }.mapNotNull { m -> m.toLatLon(srs) }.toList()
                }
                pattern { (path matches """/maps/@$LAT,$LON,${Z}z.*""")?.toLatLonZ(srs) }
                pattern { (path matches """/maps/@$LAT,$LON.*""")?.toLatLon(srs) }
                pattern { (path matches """/maps/@""")?.let { Position(srs) } }
                pattern { (path matches """/maps/place/$LAT,$LON/@[\d.,+-]+,${Z}z.*""")?.toLatLonZ(srs) }
                pattern { (path matches """/maps/place/.*/@$LAT,$LON,${Z}z.*""")?.toLatLonZ(srs) }
                pattern { (path matches """/maps/place/.*/@$LAT,$LON.*""")?.toLatLon(srs) }
                pattern { (path matches """/maps/place/$LAT,$LON.*""")?.toLatLon(srs) }
                pattern { (path matches """/maps/place/$Q_PATH.*""")?.toQ(srs) }
                pattern { (path matches """/maps/place//.*""")?.let { Position(srs) } }
                pattern { (path matches """/maps/placelists/list/.*""")?.let { Position(srs) } }
                pattern { (path matches """/maps/@/data=!3m1!4b1!4m3!11m2!2s.+!3e3""")?.let { Position(srs) } }
                pattern { (path matches """/maps/search/$LAT,$LON.*""")?.toLatLon(srs) }
                pattern { (path matches """/maps/search/$Q_PATH.*""")?.toQ(srs) }
                pattern { (path matches """/maps/search/""")?.let { Position(srs) } }
                pattern { (path matches """/maps/dir/.*/$LAT,$LON/@[\d.,+-]+,${Z}z/?[^/]*""")?.toLatLonZ(srs) }
                pattern { (path matches """/maps/dir/.*/$LAT,$LON/data[^/]*""")?.toLatLon(srs) }
                pattern { (path matches """/maps/dir/.*/$LAT,$LON/?""")?.toLatLon(srs) }
                pattern { (path matches """/maps/dir/.*/@$LAT,$LON,${Z}z/?[^/]*""")?.toLatLonZ(srs) }
                pattern { (path matches """/maps/dir/.*/$Q_PATH/data[^/]*""")?.toQ(srs) }
                pattern { (path matches """/maps/dir/.*/$Q_PATH/?""")?.toQ(srs) }
                pattern { (path matches """/maps/dir/""")?.let { Position(srs) } }
                pattern {
                    queryParams["mid"].takeIf { (path matches """/maps/d/(edit|viewer)""") != null }
                        ?.isNotEmpty()?.let { Position(srs) }
                }
                pattern { (path matches """/maps/?""")?.let { Position(srs) } }
                pattern { (path matches """/search/?""")?.let { Position(srs) } }
                pattern { (path matches """/?""")?.let { Position(srs) } }
            }
        }
    }

    override val conversionHtmlPattern = ConversionPattern.first<Source, Position> {
        listPattern {
            val linkPattern = Pattern.compile("""/@$LAT,$LON""")
            val pointPatterns = listOf(
                Pattern.compile("""APP_INITIALIZATION_STATE=\[\[\[[\d.-]+,$LON,$LAT"""),
                Pattern.compile("""\[(null,null,|null,\[)$LAT,$LON\]"""),
            )
            sequence {
                for (line in generateSequence { this@listPattern.readLine() }) {
                    (line find linkPattern)?.let { m ->
                        yield(m)
                        break
                    }
                    pointPatterns.forEach { pattern ->
                        yieldAll(line findAll pattern)
                    }
                }
            }.mapNotNull { m -> m.toLatLon(srs) }.toList()
        }
    }

    override val conversionHtmlRedirectPattern = ConversionPattern.first<Source, String> {
        pattern {
            generateSequence { this.readLine() }
                .firstNotNullOfOrNull { line -> line find """data-url="(?P<url>[^"]+)"""" }
                ?.groupOrNull("url")
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
}
