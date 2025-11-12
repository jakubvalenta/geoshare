package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.conversion.ConversionPattern
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LAT_PATTERN
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LON_PATTERN
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.Z_PATTERN
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.position.toZ

object MapyComInput : Input.HasUri, Input.HasShortUri {
    private const val COORDS = """(?P<lat>\d{1,2}(\.\d{1,16})?)[NS], (?P<lon>\d{1,3}(\.\d{1,16})?)[WE]"""

    private val srs = Srs.WGS84

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern =
        Pattern.compile("""$COORDS|(https?://)?((hapticke|www)\.)?mapy\.[a-z]{2,3}[/?]\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_mapy_com_name,
        inputs = listOf(
            Input.DocumentationInput.Url(23, "https://mapy.com"),
            Input.DocumentationInput.Url(23, "https://mapy.cz"),
            Input.DocumentationInput.Url(23, "https://www.mapy.com"),
            Input.DocumentationInput.Url(23, "https://www.mapy.cz"),
        ),
    )
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?mapy\.[a-z]{2,3}/s/\S+""")
    override val shortUriMethod = Input.ShortUriMethod.GET

    override val conversionUriPattern = ConversionPattern.first<Uri, Position> {
        pattern {
            (COORDS match path)?.let { m ->
                m.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
                    m.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
                        val latSig = if (m.groupOrNull()?.contains('S') == true) -1 else 1
                        val lonSig = if (m.groupOrNull()?.contains('W') == true) -1 else 1
                        Position(srs, latSig * lat, lonSig * lon)
                    }
                }
            }
        }
        all {
            optional {
                pattern { (Z_PATTERN match queryParams["z"])?.toZ(srs) }
            }
            pattern {
                (LAT_PATTERN match queryParams["y"])?.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
                    (LON_PATTERN match queryParams["x"])?.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
                        Position(srs, lat, lon)
                    }
                }
            }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_mapy_com_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_mapy_com_loading_indicator_title
}
