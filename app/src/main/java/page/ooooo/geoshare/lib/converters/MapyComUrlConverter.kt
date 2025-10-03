package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import page.ooooo.geoshare.lib.uriPattern

@Suppress("SpellCheckingInspection")
class MapyComUrlConverter : UrlConverter.WithUriPattern, UrlConverter.WithShortUriPattern {
    companion object {
        const val COORDS = """(?P<lat>\d{1,2}(\.\d{1,16})?)[NS], (?P<lon>\d{1,3}(\.\d{1,16})?)[WE]"""
    }

    @StringRes
    override val nameResId = R.string.converter_mapy_com_name

    override val uriPattern: Pattern =
        Pattern.compile("""$COORDS|(https?://)?((hapticke|www)\.)?mapy\.[a-z]{2,3}[/?]\S+""")
    override val supportedUriStrings = listOf(
        "https://mapy.com",
        "https://mapy.cz",
        "https://www.mapy.com",
        "https://www.mapy.cz",
    )
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?mapy\.[a-z]{2,3}/s/\S+""")
    override val shortUriMethod = ShortUriMethod.GET

    override val conversionUriPattern = uriPattern {
        path(object : PositionRegex(COORDS) {
            override val points: List<Point>?
                get() = groupOrNull("lat")?.let { lat ->
                    groupOrNull("lon")?.let { lon ->
                        val latSig = if (groupOrNull()?.contains('S') == true) "-" else ""
                        val lonSig = if (groupOrNull()?.contains('W') == true) "-" else ""
                        listOf(latSig + lat to lonSig + lon)
                    }
                }
        })
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
