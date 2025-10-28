package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z

@Suppress("SpellCheckingInspection")
class MapyComUrlConverter : UrlConverter.WithUriPattern, UrlConverter.WithShortUriPattern {
    companion object {
        const val COORDS = """(?P<lat>\d{1,2}(\.\d{1,16})?)[NS], (?P<lon>\d{1,3}(\.\d{1,16})?)[WE]"""
    }

    class EncodedPositionMatch(matcher: Matcher) : PositionMatch(matcher) {
        override val points: List<Point>?
            get() = matcher.groupOrNull("lat")?.let { lat ->
                matcher.groupOrNull("lon")?.let { lon ->
                    val latSig = if (matcher.groupOrNull()?.contains('S') == true) "-" else ""
                    val lonSig = if (matcher.groupOrNull()?.contains('W') == true) "-" else ""
                    persistentListOf(Point(latSig + lat, lonSig + lon))
                }
            }
    }

    class EncodedPositionRegex(regex: String) : PositionRegex(regex) {
        override fun matches(input: String) = pattern.matcherIfMatches(input)?.let { EncodedPositionMatch(it) }
        override fun find(input: String) = pattern.matcherIfFind(input)?.let { EncodedPositionMatch(it) }
    }

    override val uriPattern: Pattern =
        Pattern.compile("""$COORDS|(https?://)?((hapticke|www)\.)?mapy\.[a-z]{2,3}[/?]\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_mapy_com_name,
        inputs = listOf(
            DocumentationInput.Url(23, "https://mapy.com"),
            DocumentationInput.Url(23, "https://mapy.cz"),
            DocumentationInput.Url(23, "https://www.mapy.com"),
            DocumentationInput.Url(23, "https://www.mapy.cz"),
        ),
    )
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?mapy\.[a-z]{2,3}/s/\S+""")
    override val shortUriMethod = ShortUriMethod.GET

    override val conversionUriPattern = uriPattern {
        path(EncodedPositionRegex(COORDS))
        all {
            optional {
                query("z", PositionRegex(Z))
            }
            query("x", PositionRegex(LON))
            query("y", PositionRegex(LAT))
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_mapy_com_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_mapy_com_loading_indicator_title
}
