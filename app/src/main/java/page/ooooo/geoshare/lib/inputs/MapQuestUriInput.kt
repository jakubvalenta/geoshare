package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.FetchTools
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapQuestUriInput @Inject constructor(
    val mapQuestHtmlInput: dagger.Lazy<MapQuestHtmlInput>,
    val uriQuote: UriQuote,
) : BasicInput, Input.HasPattern {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.MAP_QUEST
    override val changelog = persistentListOf(
        InputChangelogItem.Url(51, "https://mapq.st"),
        InputChangelogItem.Url(51, "https://www.mapquest.com"),
    )

    override val pattern = Regex("""((?:https?://)?(?:(?:www\.)?mapquest\.com|mapq\.st)[/?#]$URI_REST)""")

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        Uri.parse(match, uriQuote).run {
            parseResult {
                // Search
                // https://www.mapquest.com/search/{q}
                if (pathParts.getOrNull(1) == "search") {
                    pathParts.getOrNull(2)?.takeIf { it.isNotBlank() }?.let { q ->
                        points = persistentListOf(GCJ02MainlandChinaPoint(name = q, source = Source.URI))
                    }
                    return@parseResult
                }

                // Place or short link (go to HTML parsing)
                // https://www.mapquest.com/{country}/{name}-{id}
                // https://www.mapquest.com/{country}/{region}/{name}-{id}
                // https://mapq.st/{id}
                next = MatchedInput(mapQuestHtmlInput.get(), match)
            }
        }

    override fun toString() = "MapQuestUriInput"
}
