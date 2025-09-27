package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import page.ooooo.geoshare.lib.uriPattern

@Suppress("SpellCheckingInspection")
class MapyComUrlConverter : UrlConverter.WithUriPattern, UrlConverter.WithShortUriPattern {
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?((hapticke|www)\.)?mapy\.[a-z]{2,3}[/?]\S+""")
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?mapy\.[a-z]{2,3}/s/\S+""")
    override val shortUriMethod = ShortUriMethod.GET

    override val conversionUriPattern = uriPattern {
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
