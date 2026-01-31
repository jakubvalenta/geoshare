package page.ooooo.geoshare.lib.inputs

import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.geo.decodeGe0Hash
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints
import page.ooooo.geoshare.lib.point.toParseUriResult

object MapsMeInput : Input {
    private const val HASH = """[A-Za-z0-9\-_]{2,}"""

    override val uriPattern = Regex("""(?:(?:https?://)?(?:comaps\.at|ge0\.me|omaps\.app)|ge0:/)/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.MAPS_ME,
        nameResId = R.string.converter_ge0_name,
        items = listOf(
            InputDocumentationItem.Url(25, "http://ge0.me/"),
            InputDocumentationItem.Url(25, "https://omaps.app/"),
            InputDocumentationItem.Url(25, "https://comaps.at/"),
        ),
    )

    override suspend fun parseUri(uri: Uri): ParseUriResult? =
        buildPoints {
            uri.run {
                (if (scheme == "ge0") host else pathParts.getOrNull(1))
                    ?.let { Regex(HASH).matchEntire(it) }
                    ?.value
                    ?.let { hash -> decodeGe0Hash(hash) }
                    ?.let { (lat, lon, z) -> NaivePoint(lat.toScale(7), lon.toScale(7), z) }
                    ?.also { points.add(it) }

                (if (scheme == "ge0") pathParts.getOrNull(1) else pathParts.getOrNull(2))
                    ?.let { Q_PATH_PATTERN.matchEntire(it) }
                    ?.groupOrNull()
                    ?.replace('_', ' ')
                    ?.also { defaultName = it }
            }
        }
            .asWGS84()
            .toParseUriResult()
}
