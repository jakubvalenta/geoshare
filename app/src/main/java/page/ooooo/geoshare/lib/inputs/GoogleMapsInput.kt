package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import io.ktor.utils.io.*
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.*
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.position.buildPosition

object GoogleMapsInput : Input.HasShortUri, Input.HasHtml {
    const val NAME = "Google Maps"
    private const val SHORT_URL = """((maps\.)?(app\.)?goo\.gl|g\.co)/[/A-Za-z0-9_-]+"""

    private val srs = Srs.GCJ02

    override val uriPattern: Pattern =
        Pattern.compile("""(https?://)?((www|maps)\.)?(google(\.[a-z]{2,3})?\.[a-z]{2,3}[/?#]\S+|$SHORT_URL)""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.GOOGLE_MAPS,
        nameResId = R.string.converter_google_maps_name,
        items = listOf(
            InputDocumentationItem.Url(5, "https://maps.app.goo.gl"),
            InputDocumentationItem.Url(5, "https://app.goo.gl/maps"),
            InputDocumentationItem.Url(5, "https://maps.google.com"),
            InputDocumentationItem.Url(5, "https://goo.gl/maps"),
            InputDocumentationItem.Url(5, "https://google.com/maps"),
            InputDocumentationItem.Url(5, "https://www.google.com/maps"),
            InputDocumentationItem.Url(10, "https://g.co/kgs"),
        ),
    )
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?$SHORT_URL""")
    override val shortUriMethod = Input.ShortUriMethod.HEAD

    override suspend fun parseUri(uri: Uri): ParseUriResult? {
        var htmlUriString: String? = null
        val position = buildPosition(srs) {
            uri.run {
                // Try query parameters for all URLs
                setPointIfNull { LAT_LON_PATTERN matchLatLonZ queryParams["destination"] }
                setPointIfNull { LAT_LON_PATTERN matchLatLonZ queryParams["q"] }
                setPointIfNull { LAT_LON_PATTERN matchLatLonZ queryParams["query"] }
                setPointIfNull { LAT_LON_PATTERN matchLatLonZ queryParams["ll"] }
                setPointIfNull { LAT_LON_PATTERN matchLatLonZ queryParams["viewpoint"] }
                setPointIfNull { LAT_LON_PATTERN matchLatLonZ queryParams["center"] }
                setQIfNull { Q_PARAM_PATTERN matchQ queryParams["destination"] }
                setQIfNull { Q_PARAM_PATTERN matchQ queryParams["q"] }
                setQIfNull { Q_PARAM_PATTERN matchQ queryParams["query"] }
                setZIfNull { Z_PATTERN matchZ queryParams["zoom"] }

                val parseUriParts = setOf("dir", "place", "search")
                val parseHtmlParts = setOf("", "@", "d", "placelists")
                val parseHtmlExcludeParts = setOf("search")
                val parts = uri.pathParts.drop(1).dropWhile { it == "maps" }
                val firstPart = parts.firstOrNull()
                when {
                    firstPart == null || firstPart in parseHtmlParts -> {
                        // Skip URI parsing and go to HTML parsing
                        htmlUriString = uri.toString()
                    }

                    firstPart in parseUriParts || firstPart.startsWith('@') -> {
                        // Parse URI
                        val pointPattern: Pattern = Pattern.compile("""$LAT,$LON.*""")
                        parts.dropWhile { it in parseUriParts }.forEachReversed { part ->
                            if (part.startsWith("data=")) {
                                setPointIfNull { """!3d$LAT!4d$LON""" findLatLonZ part }
                                addPoints { """!1d$LON!2d$LAT""" findAllLatLonZ part }
                            } else if (part.startsWith('@')) {
                                setDefaultPointIfNull { """@$LAT,$LON(,${Z}z)?.*""" matchLatLonZ part }
                            } else {
                                setPointIfNull { pointPattern matchLatLonZ part }
                                setQOrNameIfEmpty { Q_PATH_PATTERN matchQ part }
                            }
                        }
                        if (!hasPoint() && firstPart !in parseHtmlExcludeParts) {
                            // Go to HTML parsing if needed
                            htmlUriString = uri.toString()
                        }
                    }
                }
            }
        }
        return ParseUriResult.from(position, htmlUriString)
    }

    override suspend fun parseHtml(channel: ByteReadChannel, positionFromUri: Position, log: ILog): ParseHtmlResult? {
        var redirectUriString: String? = null
        val positionFromHtml = buildPosition(srs) {
            val pointPattern = Pattern.compile("""\[(null,null,|null,\[)$LAT,$LON\]""")
            val defaultPointPattern1 = Pattern.compile("""/@$LAT,$LON""")
            val defaultPointPattern2 = Pattern.compile("""APP_INITIALIZATION_STATE=\[\[\[[\d.-]+,$LON,$LAT""")
            val uriPattern = Pattern.compile("""data-url="(?P<url>[^"]+)"""")
            while (true) {
                val line = channel.readUTF8Line() ?: break
                if (addPoints { (pointPattern findAllLatLonZ line) }) {
                    log.d("GoogleMapsInput", "HTML Pattern: Point pattern matched line $line")
                }
                if (setDefaultPointIfNull { (defaultPointPattern1 findLatLonZ line) }) {
                    log.d("GoogleMapsInput", "HTML Pattern: Default point pattern 1 matched line $line")
                }
                if (setDefaultPointIfNull { (defaultPointPattern2 findLatLonZ line) }) {
                    log.d("GoogleMapsInput", "HTML Pattern: Default point pattern 2 matched line $line")
                }
                if (redirectUriString == null) {
                    (uriPattern findUriString line)?.let {
                        redirectUriString = it
                        log.d("GoogleMapsInput", "HTML Pattern: URI pattern matched line $line")
                    }
                }
            }
        }
        return ParseHtmlResult.from(positionFromUri, positionFromHtml, redirectUriString)
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
}
