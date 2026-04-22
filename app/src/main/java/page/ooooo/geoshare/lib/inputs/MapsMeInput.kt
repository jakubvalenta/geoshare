package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.geo.decodeGe0Hash

object MapsMeInput : Input {
    private const val HASH = """[A-Za-z0-9\-_]{2,}"""

    override val uriPattern = Regex("""((?:(?:https?://)?(?:comaps\.at|ge0\.me|omaps\.app)|ge0:/)/$URI_REST)""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.MAPS_ME,
        nameResId = R.string.converter_ge0_name,
        items = listOf(
            InputDocumentationItem.Url(25, "http://ge0.me/"),
            InputDocumentationItem.Url(25, "https://omaps.app/"),
            InputDocumentationItem.Url(25, "https://comaps.at/"),
        ),
    )

    override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = buildParseUriResult {
        uri.run {
            val name = (if (scheme == "ge0") pathParts.getOrNull(1) else pathParts.getOrNull(2))
                ?.let { Q_PATH_PATTERN.matchEntire(it) }
                ?.groupOrNull()
                ?.replace('_', ' ')

            (if (scheme == "ge0") host else pathParts.getOrNull(1))
                ?.let { Regex(HASH).matchEntire(it)?.value }
                ?.let { hash -> decodeGe0Hash(hash) }
                ?.let {
                    points = persistentListOf(
                        WGS84Point(it).copy(
                            lat = it.lat?.toScale(7),
                            lon = it.lon?.toScale(7),
                            name = name
                        )
                    )
                    return@run
                }

            if (name != null) {
                points = persistentListOf(WGS84Point(name = name, source = Source.URI))
            }
        }
    }
}
