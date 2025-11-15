package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import kotlinx.io.Source
import kotlinx.io.readLine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.*
import page.ooooo.geoshare.lib.position.PositionBuilder
import page.ooooo.geoshare.lib.position.Srs

object GoogleMapsInput : Input.HasShortUri, Input.HasHtml {
    const val NAME = "Google Maps"
    private const val SHORT_URL = """((maps\.)?(app\.)?goo\.gl|g\.co)/[/A-Za-z0-9_-]+"""

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

    override fun parseUri(uri: Uri) = uri.run {
        PositionBuilder(srs).apply {
            // Try query parameters for all URLs
            setPointIfEmpty { LAT_LON_PATTERN matchLatLonZ queryParams["destination"] }
            setPointIfEmpty { LAT_LON_PATTERN matchLatLonZ queryParams["q"] }
            setPointIfEmpty { LAT_LON_PATTERN matchLatLonZ queryParams["query"] }
            setPointIfEmpty { LAT_LON_PATTERN matchLatLonZ queryParams["viewpoint"] }
            setPointIfEmpty { LAT_LON_PATTERN matchLatLonZ queryParams["center"] }
            setQIfEmpty { Q_PARAM_PATTERN matchQ queryParams["destination"] }
            setQIfEmpty { Q_PARAM_PATTERN matchQ queryParams["q"] }
            setQIfEmpty { Q_PARAM_PATTERN matchQ queryParams["query"] }
            setZIfEmpty { Z_PATTERN matchZ queryParams["zoom"] }

            val parseHtmlParts = setOf("", "@", "d", "placelists")
            val parseUriParts = setOf("dir", "place", "search")
            val parts = uri.pathParts.drop(1).dropWhile { it == "maps" }
            val firstPart = parts.firstOrNull()
            when {
                firstPart == null || firstPart in parseHtmlParts -> {
                    // Skip URI parsing and go to HTML parsing
                    setUriStringIfEmpty { uri.toString() }
                }

                firstPart in parseUriParts || firstPart.startsWith('@') -> {
                    // Parse URI
                    val pointPattern: Pattern = Pattern.compile("""$LAT,$LON.*""")
                    parts.dropWhile { it in parseUriParts }.forEachReversed { part ->
                        if (part.startsWith("data=")) {
                            setPointIfEmpty { """!3d$LAT!4d$LON""" findLatLonZ part }
                            addPoints { """!1d$LON!2d$LAT""" findAllLatLonZ part }
                        } else if (part.startsWith('@')) {
                            setDefaultPointIfEmpty { """@$LAT,$LON(,${Z}z)?.*""" matchLatLonZ part }
                        } else {
                            setPointIfEmpty { pointPattern matchLatLonZ part }
                            setQOrNameIfEmpty { Q_PATH_PATTERN matchQ part }
                        }
                    }
                    setUriStringIfEmpty { uri.toString() }
                }
            }
        }.toPair()
    }

    override fun parseHtml(source: Source) = source.run {
        PositionBuilder(srs).apply {
            val pointPattern = Pattern.compile("""\[(null,null,|null,\[)$LAT,$LON\]""")
            val defaultPointPattern1 = Pattern.compile("""/@$LAT,$LON""")
            val defaultPointPattern2 = Pattern.compile("""APP_INITIALIZATION_STATE=\[\[\[[\d.-]+,$LON,$LAT""")
            val uriPattern = Pattern.compile("""data-url="(?P<url>[^"]+)"""")
            for (line in generateSequence { source.readLine() }) {
                addPoints { pointPattern findAllLatLonZ line }
                setDefaultPointIfEmpty { defaultPointPattern1 findLatLonZ line }
                setDefaultPointIfEmpty { defaultPointPattern2 findLatLonZ line }
                setUriStringIfEmpty { uriPattern findUriString line }
            }
        }.toPair()
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
}
