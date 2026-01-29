package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.matchHash
import page.ooooo.geoshare.lib.extensions.matchQ
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.geo.decodeGe0Hash
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints

object MapsMeInput : Input {
    private const val HASH = """(?P<hash>[A-Za-z0-9\-_]{2,})"""

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""((https?://)?(comaps\.at|ge0\.me|omaps\.app)|ge0:/)/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.MAPS_ME,
        nameResId = R.string.converter_ge0_name,
        items = listOf(
            InputDocumentationItem.Url(25, "http://ge0.me/"),
            InputDocumentationItem.Url(25, "https://omaps.app/"),
            InputDocumentationItem.Url(25, "https://comaps.at/"),
        ),
    )

    override suspend fun parseUri(uri: Uri): ParseUriResult? {
        val points = buildPoints {
            uri.run {
                setPointIfNull {
                    (HASH matchHash if (scheme == "ge0") host else pathParts.getOrNull(1))
                        ?.let { hash -> decodeGe0Hash(hash) }
                        ?.let { (lat, lon, z) -> NaivePoint(lat.toScale(7), lon.toScale(7), z) }

                }
                setQOrNameIfEmpty {
                    (Q_PATH_PATTERN matchQ if (scheme == "ge0") pathParts.getOrNull(1) else pathParts.getOrNull(2))
                        ?.replace('_', ' ')
                }
            }
        }
        return ParseUriResult.from(points.asWGS84())
    }
}
