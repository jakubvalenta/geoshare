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

    override val conversionUriPattern = ConversionPattern.first<Uri, Position> {
        all {
            optional {
                first {
                    pattern { (Z_PATTERN match scheme)?.toZ(srs) }
                    pattern { (Z_PATTERN match queryParams["zoom"])?.toZ(srs) }
                    pattern { ("""/maps/.*/@[\d.,+-]+,${Z}z.*""" match path)?.toZ(srs) }
                }
                first {
                    pattern {
                        (LAT_LON_PATTERN match queryParams["destination"])?.toLatLon(srs)
                    }
                    pattern { (Q_PARAM_PATTERN match queryParams["destination"])?.toQ(srs) }
                    pattern { (LAT_LON_PATTERN match queryParams["q"])?.toLatLon(srs) }
                    pattern { (Q_PARAM_PATTERN match queryParams["q"])?.toQ(srs) }
                    pattern { (LAT_LON_PATTERN match queryParams["query"])?.toLatLon(srs) }
                    pattern { (Q_PARAM_PATTERN match queryParams["query"])?.toQ(srs) }
                    pattern { (LAT_LON_PATTERN match queryParams["viewpoint"])?.toLatLon(srs) }
                    pattern { (LAT_LON_PATTERN match queryParams["center"])?.toLatLon(srs) }
                }
            }
            first {
                listPattern {
                    sequence {
                        ("""/maps/.*/$DATA""" match path)?.groupOrNull("data")?.let { data ->
                            ("""!3d$LAT!4d$LON""" find data)?.let { m ->
                                yield(m)
                                return@sequence
                            }
                            yieldAll("""!1d$LON!2d$LAT""" findAll data)
                        }
                    }.mapNotNull { m -> m.toLatLon(srs) }.toList()
                }
                pattern { ("""/maps/@$LAT,$LON,${Z}z.*""" match path)?.toLatLonZ(srs) }
                pattern { ("""/maps/@$LAT,$LON.*""" match path)?.toLatLon(srs) }
                pattern { ("""/maps/@""" match path)?.let { Position(srs) } }
                pattern { ("""/maps/place/$LAT,$LON/@[\d.,+-]+,${Z}z.*""" match path)?.toLatLonZ(srs) }
                pattern { ("""/maps/place/.*/@$LAT,$LON,${Z}z.*""" match path)?.toLatLonZ(srs) }
                pattern { ("""/maps/place/.*/@$LAT,$LON.*""" match path)?.toLatLon(srs) }
                pattern { ("""/maps/place/$LAT,$LON.*""" match path)?.toLatLon(srs) }
                pattern { ("""/maps/place/$Q_PATH.*""" match path)?.toQ(srs) }
                pattern { ("""/maps/place//.*""" match path)?.let { Position(srs) } }
                pattern { ("""/maps/placelists/list/.*""" match path)?.let { Position(srs) } }
                pattern { ("""/maps/@/data=!3m1!4b1!4m3!11m2!2s.+!3e3""" match path)?.let { Position(srs) } }
                pattern { ("""/maps/search/$LAT,$LON.*""" match path)?.toLatLon(srs) }
                pattern { ("""/maps/search/$Q_PATH.*""" match path)?.toQ(srs) }
                pattern { ("""/maps/search/""" match path)?.let { Position(srs) } }
                pattern { ("""/maps/dir/.*/$LAT,$LON/@[\d.,+-]+,${Z}z/?[^/]*""" match path)?.toLatLonZ(srs) }
                pattern { ("""/maps/dir/.*/$LAT,$LON/data[^/]*""" match path)?.toLatLon(srs) }
                pattern { ("""/maps/dir/.*/$LAT,$LON/?""" match path)?.toLatLon(srs) }
                pattern { ("""/maps/dir/.*/@$LAT,$LON,${Z}z/?[^/]*""" match path)?.toLatLonZ(srs) }
                pattern { ("""/maps/dir/.*/$Q_PATH/data[^/]*""" match path)?.toQ(srs) }
                pattern { ("""/maps/dir/.*/$Q_PATH/?""" match path)?.toQ(srs) }
                pattern { ("""/maps/dir/""" match path)?.let { Position(srs) } }
                pattern {
                    if (("""/maps/d/(edit|viewer)""" match path) != null && !queryParams["mid"].isNullOrEmpty()) {
                        Position(srs)
                    } else {
                        null
                    }
                }
                pattern { ("""/maps/?""" match path)?.let { Position(srs) } }
                pattern { ("""/search/?""" match path)?.let { Position(srs) } }
                pattern { ("""/?""" match path)?.let { Position(srs) } }
            }
        }
    }

    override val conversionHtmlPattern = ConversionPattern.first<Source, Position> {
        listPattern {
            val mainPointPatterns = listOf(
                Pattern.compile("""/@$LAT,$LON"""),
                Pattern.compile("""APP_INITIALIZATION_STATE=\[\[\[[\d.-]+,$LON,$LAT"""),
            )
            val extraPointPattern = Pattern.compile("""\[(null,null,|null,\[)$LAT,$LON\]""")
            var mainPosition: Position? = null
            val extraPositions: MutableList<Position> = mutableListOf()
            for (line in generateSequence { this@listPattern.readLine() }) {
                if (mainPosition == null) {
                    mainPointPatterns
                        .firstNotNullOfOrNull { pattern -> pattern find line }?.toLatLon(srs)
                        ?.let { mainPosition = it }
                }
                (extraPointPattern findAll line)
                    .mapNotNull { m -> m.toLatLon(srs) }
                    .forEach { extraPositions.add(it) }
            }
            if (extraPositions.isNotEmpty()) {
                // If the URI is a place list, ignore the APP_INITIALIZATION_STATE point and only take the other points
                extraPositions
            } else if (mainPosition != null) {
                listOf(mainPosition)
            } else {
                emptyList()
            }
        }
    }

    override val conversionHtmlRedirectPattern = ConversionPattern.first<Source, String> {
        pattern {
            val pattern = Pattern.compile("""data-url="(?P<url>[^"]+)"""")
            generateSequence { this.readLine() }
                .firstNotNullOfOrNull { line -> pattern find line }
                ?.groupOrNull("url")
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
}
