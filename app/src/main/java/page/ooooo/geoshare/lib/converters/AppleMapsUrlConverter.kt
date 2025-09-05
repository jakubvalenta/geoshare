package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Position

class AppleMapsUrlConverter() : UrlConverter {
    override val name = "Apple Maps"
    override val host: Pattern = Pattern.compile("""maps\.apple(\.com)?""")
    override val shortUrlHost = null
    override val pattern = all {
        query("z", zoomPattern)
        first {
            all {
                host(Pattern.compile("maps.apple"))
                path(Pattern.compile("/p/.+"))
            }
            query("ll", coordPattern)
            query("coordinate", coordPattern)
            query("q", coordPattern)
            query("address", queryPattern)
            query("name", queryPattern)
            all {
                query("auid", anyPattern)
                query("q", queryPattern)
            }
            all {
                query("place-id", anyPattern)
                query("q", queryPattern)
            }
            query("auid", anyPattern)
            query("place-id", anyPattern)
            all {
                query("q", queryPattern)
                query("sll", coordPattern)
            }
            query("q", queryPattern)
            query("sll", coordPattern)
            query("center", coordPattern)
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title

    val latRegex = """\+?(?P<lat>-?\d{1,2}(\.\d{1,16})?)"""
    val lonRegex = """\+?(?P<lon>-?\d{1,3}(\.\d{1,16})?)"""
    val coordRegex = "$latRegex,$lonRegex"
    val coordPattern: Pattern = Pattern.compile(coordRegex)
    val zoomRegex = """(?P<z>\d{1,2}(\.\d{1,16})?)"""
    val zoomPattern: Pattern = Pattern.compile(zoomRegex)
    val queryPattern: Pattern = Pattern.compile("""(?P<q>.+)""")
    val anyPattern: Pattern = Pattern.compile(".")

    val htmlPatterns = listOf(
        Pattern.compile("""<meta property="place:location:latitude" content="$latRegex""""),
        Pattern.compile("""<meta property="place:location:longitude" content="$lonRegex""""),
    )

    override val parseHtml = { html: String ->
        htmlPatterns.mapNotNull { it.matcher(html).takeIf { m -> m.find() }?.let { m -> Position.fromMatcher(m) } }
            .reduceRightOrNull { sum, element -> sum.union(element) }?.let { ParseHtmlResult.Parsed(it) }
    }
}
