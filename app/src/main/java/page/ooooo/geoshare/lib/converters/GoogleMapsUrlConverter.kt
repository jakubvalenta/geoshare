package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R

class GoogleMapsUrlConverter() : UrlConverter {
    override val name = "Google Maps"

    override val host: Pattern = Pattern.compile("""((www|maps)\.)?google(\.[a-z]{2,3})?\.[a-z]{2,3}""")
    override val shortUrlHost: Pattern = Pattern.compile("""(maps\.)?(app\.)?goo\.gl""")

    @Suppress("SpellCheckingInspection")
    override val urlPattern: UrlPattern = all {
        query("zoom", zoomPattern)
        first {
            query("destination", coordPattern)
            query("destination", queryPattern)
            query("q", coordPattern)
            query("q", queryPattern)
            query("query", coordPattern)
            query("query", queryPattern)
            query("viewpoint", coordPattern)
            query("center", coordPattern)
        }
        first {
            path(Pattern.compile("""^/maps/.*/@[\d.,+-]+,${zoomRegex}z/data=.*$dataCoordRegex.*$"""))
            path(Pattern.compile("""^/maps/.*/data=.*$dataCoordRegex.*$"""))
            path(Pattern.compile("""^/maps/@$coordRegex,${zoomRegex}z.*$"""))
            path(Pattern.compile("""^/maps/@$coordRegex.*$"""))
            path(Pattern.compile("""^/maps/@$"""))
            path(Pattern.compile("""^/maps/place/$coordRegex/@[\d.,+-]+,${zoomRegex}z.*$"""))
            path(Pattern.compile("""^/maps/place/$placeRegex/@$coordRegex,${zoomRegex}z.*$"""))
            path(Pattern.compile("""^/maps/place/$placeRegex/@$coordRegex.*$"""))
            path(Pattern.compile("""^/maps/place/$coordRegex.*$"""))
            path(Pattern.compile("""^/maps/place/$placeRegex.*$"""))
            path(Pattern.compile("""^/maps/place//.*$"""))
            path(Pattern.compile("""^/maps/placelists/list/.*$"""))
            path(Pattern.compile("""^/maps/search/$coordRegex.*$"""))
            path(Pattern.compile("""^/maps/search/$placeRegex.*$"""))
            path(Pattern.compile("""^/maps/search/$"""))
            path(Pattern.compile("""^/maps/dir/.*/$coordRegex/data[^/]*$"""))
            path(Pattern.compile("""^/maps/dir/.*/$placeRegex/data[^/]*$"""))
            path(Pattern.compile("""^/maps/dir/.*/$coordRegex$"""))
            path(Pattern.compile("""^/maps/dir/.*/@$coordRegex,${zoomRegex}z.*$"""))
            path(Pattern.compile("""^/maps/dir/.*/$placeRegex$"""))
            path(Pattern.compile("""^/maps/dir/$"""))
            path(Pattern.compile("""^/maps/?$"""))
            path(Pattern.compile("""^/search/?$"""))
            path(Pattern.compile("""^/?$"""))
        }
    }

    override val htmlPattern = html {
        text(Pattern.compile("""/@$coordRegex"""))
        text(Pattern.compile("""\[null,null,$coordRegex\]"""))
    }

    override val htmlRedirectPattern = html {
        text(Pattern.compile("""data-url="(?P<url>[^"]+)"""))
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title

    private val coordRegex = """\+?(?P<lat>-?\d{1,2}(\.\d{1,16})?),[+\s]?(?P<lon>-?\d{1,3}(\.\d{1,16})?)"""
    private val coordPattern: Pattern = Pattern.compile(coordRegex)
    private val dataCoordRegex = """!3d(?P<lat>-?\d{1,2}(\.\d{1,16})?)!4d(?P<lon>-?\d{1,3}(\.\d{1,16})?)"""
    private val zoomRegex = """(?P<z>\d{1,2}(\.\d{1,16})?)"""
    private val zoomPattern: Pattern = Pattern.compile(zoomRegex)
    private val queryPattern: Pattern = Pattern.compile("""(?P<q>.+)""")
    private val placeRegex = """(?P<q>[^/]+)"""
}
