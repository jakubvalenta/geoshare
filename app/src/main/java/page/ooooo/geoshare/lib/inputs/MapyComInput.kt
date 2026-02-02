package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints

object MapyComInput : Input.HasShortUri {
    private const val COORDS = """(\d{1,2}(?:\.\d{1,16})?)[NS], (\d{1,3}(?:\.\d{1,16})?)[WE]"""

    override val uriPattern =
        Regex("""$COORDS|(?:https?://)?(?:(?:hapticke|www)\.)?mapy\.[a-z]{2,3}[/?]\S+""")
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
    override val shortUriPattern = Regex("""(?:https?://)?(?:www\.)?mapy\.[a-z]{2,3}/s/\S+""")
    override val shortUriMethod = Input.ShortUriMethod.GET

    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        points = buildPoints {
            uri.run {
                Regex(COORDS).matchEntire(path)
                    ?.let { m ->
                        m.groupValues[0].let { entireMatch ->
                            m.doubleGroupOrNull(1)?.let { lat ->
                                m.doubleGroupOrNull(2)?.let { lon ->
                                    val latSig = if (entireMatch.contains('S')) -1 else 1
                                    val lonSig = if (entireMatch.contains('W')) -1 else 1
                                    NaivePoint(latSig * lat, lonSig * lon)
                                }
                            }
                        }
                    }
                    ?.also { points.add(it) }
                    ?: LAT_PATTERN.matchEntire(queryParams["y"])?.doubleGroupOrNull()?.let { lat ->
                        LAT_PATTERN.matchEntire(queryParams["x"])?.doubleGroupOrNull()?.let { lon ->
                            NaivePoint(lat, lon)
                        }
                    }?.also { points.add(it) }

                Z_PATTERN.matchEntire(queryParams["z"])?.doubleGroupOrNull()?.also { defaultZ = it }
            }
        }.asWGS84()
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_mapy_com_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_mapy_com_loading_indicator_title
}
