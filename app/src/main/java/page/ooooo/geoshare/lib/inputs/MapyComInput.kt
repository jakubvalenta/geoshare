package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.*

object MapyComInput : Input.HasShortUri {
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

    override fun parseUri(uri: Uri) = uri.run {
        PositionBuilder(srs).apply {
            setLatLon {
                (COORDS match path)?.let { m ->
                    m.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
                        m.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
                            val latSig = if (m.groupOrNull()?.contains('S') == true) -1 else 1
                            val lonSig = if (m.groupOrNull()?.contains('W') == true) -1 else 1
                            latSig * lat to lonSig * lon
                        }
                    }
                }
            }
            setLatLon {
                (LAT_PATTERN match queryParams["y"])?.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
                    (LON_PATTERN match queryParams["x"])?.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
                        lat to lon
                    }
                }
            }
            setZoomFromMatcher { Z_PATTERN match queryParams["z"] }
        }.toPair()
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_mapy_com_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_mapy_com_loading_indicator_title
}
