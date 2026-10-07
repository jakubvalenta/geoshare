package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.extensions.toLonLatZPoint
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.network.FetchTools
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YandexMapsHtmlInput @Inject constructor(
    val engine: HttpClientEngine,
    val log: Log,
    val uriQuote: UriQuote,
) : BasicInput, Input.HasPermission {
    override fun getName(resources: Resources) = resources.getString(R.string.input_yandex_html_name)
    override val group = InputGroup.YANDEX_MAPS

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        fetchTools.getBodyAsChannel(match, engine, log, uriQuote) { data ->
            parseResult {
                val uri = Uri.parse(match, uriQuote)
                val ptPattern = Regex("""pt=$LON%2C$LAT""")
                val llPattern = if (!uri.queryParams.contains("ll")) {
                    // If the match (place URL) doesn't contain map center, then the '%2F%3Fll%3D...' pattern returns
                    // correct place location. Example match:
                    // https://yandex.com/maps/org/zapretny_gorod/5867973238
                    Regex("${Regex.escape(uriQuote.encode(match))}%2F%3Fll%3D$LON%252C$LAT%26z%3D$Z")
                } else {
                    // If the match (place URL) contains map center, then the '%2F%3Fll%3D...' pattern returns the same
                    // map center. This is not the point the user expects when processing a place URL, so the pattern
                    // must not be used. Example match:
                    // https://yandex.com/maps/org/zapretny_gorod/5867973238/?ll=116.096354%2C40.045755&z=13
                    null
                }
                val namePattern = Regex("""itemProp="name"[^>]*>([^<]+)""")

                var naivePoint: NaivePoint? = null
                var name: String? = null

                while (true) {
                    val line = data.readLine() ?: break
                    ptPattern.find(line)?.toLonLatPoint(Source.HTML)?.let {
                        naivePoint = it
                        continue
                    }
                    llPattern?.find(line)?.toLonLatZPoint(Source.HTML)?.let {
                        naivePoint = it
                        continue
                    }
                    namePattern.find(line)?.groupOrNull()?.let {
                        name = it
                        break
                    }
                }

                if (naivePoint != null) {
                    points = persistentListOf(WGS84Point(naivePoint, name = name))
                } else if (name != null) {
                    points = persistentListOf(WGS84Point(name = name, source = Source.HTML))
                }
            }
        }

    override fun toString() = "YandexMapsHtmlInput"
}
