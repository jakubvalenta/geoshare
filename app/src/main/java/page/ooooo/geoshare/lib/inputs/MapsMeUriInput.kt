package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.geo.decodeGe0Hash
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapsMeUriInput @Inject constructor(
    override val uriQuote: UriQuote,
) : UriInput {
    override val pattern = Regex("""((?:(?:https?://)?(?:comaps\.at|ge0\.me|omaps\.app)|ge0:/)/$URI_REST)""")
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.MAPS_ME,
        items = listOf(
            InputDocumentationItem.Url(25, "http://ge0.me/"),
            InputDocumentationItem.Url(25, "https://omaps.app/"),
            InputDocumentationItem.Url(25, "https://comaps.at/"),
        ),
    )

    override suspend fun parse(data: Uri, match: String, prevResult: ParseResult?) = buildParseResult {
        data.run {
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

    override fun toString() = "MapsMeUriInput"

    private companion object {
        private const val HASH = """[A-Za-z0-9\-_]{2,}"""
    }
}
