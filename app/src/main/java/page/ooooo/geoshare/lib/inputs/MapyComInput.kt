package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.*
import page.ooooo.geoshare.lib.position.LatLonZ
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.position.buildPosition

object MapyComInput : Input.HasShortUri {
    private const val COORDS = """(?P<lat>\d{1,2}(\.\d{1,16})?)[NS], (?P<lon>\d{1,3}(\.\d{1,16})?)[WE]"""

    private val srs = Srs.WGS84

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern =
        Pattern.compile("""$COORDS|(https?://)?((hapticke|www)\.)?mapy\.[a-z]{2,3}[/?]\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.MAPY_COM,
        nameResId = R.string.converter_mapy_com_name,
        items = listOf(
            InputDocumentationItem.Url(23, "https://mapy.com"),
            InputDocumentationItem.Url(23, "https://mapy.cz"),
            InputDocumentationItem.Url(23, "https://www.mapy.com"),
            InputDocumentationItem.Url(23, "https://www.mapy.cz"),
        ),
    )
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?mapy\.[a-z]{2,3}/s/\S+""")
    override val shortUriMethod = Input.ShortUriMethod.GET

    override suspend fun parseUri(uri: Uri): ParseUriResult? {
        val position = buildPosition(srs) {
            uri.run {
                setPointIfNull {
                    (COORDS match path)?.let { m ->
                        m.toLatLon()?.let { (lat, lon) ->
                            val wholeMatch = m.groupOrNull()
                            val latSig = if (wholeMatch?.contains('S') == true) -1 else 1
                            val lonSig = if (wholeMatch?.contains('W') == true) -1 else 1
                            LatLonZ(latSig * lat, lonSig * lon, null)
                        }
                    }
                }
                setPointIfNull {
                    (LAT_PATTERN match queryParams["y"])?.toLat()?.let { lat ->
                        (LAT_PATTERN match queryParams["x"])?.toLat()?.let { lon ->
                            LatLonZ(lat, lon, null)
                        }
                    }
                }
                setZIfNull { Z_PATTERN matchZ queryParams["z"] }
            }
        }
        return ParseUriResult.from(position)
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_mapy_com_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_mapy_com_loading_indicator_title
}
