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
import page.ooooo.geoshare.lib.network.DESKTOP_USER_AGENT
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
        fetchTools.getBodyAsChannel(
            match,
            engine,
            log,
            uriQuote,
            // Set a custom user agent, so that Yandex Maps returns an HTML with the '...ll%3D...' value.
            userAgent = DESKTOP_USER_AGENT,
        ) { data ->
            parseResult {
                val uri = Uri.parse(match, uriQuote)
                val ptPattern = Regex("""pt=$LON%2C$LAT""")
                val llPattern = if (!uri.queryParams.contains("ll")) {
                    // Use the '...ll%3D...' pattern only if the place URL doesn't already contain a map center (e.g.
                    // https://yandex.com/maps/org/zapretny_gorod/5867973238). Because if the place URL contains a map
                    // center (e.g.
                    // https://yandex.com/maps/org/zapretny_gorod/5867973238/?ll=116.096354%2C40.045755&z=13), then the
                    // HTML '...ll%3D...' value contains the same coordinates as the page URL, instead of the correct
                    // coordinates of the place.
                    Regex("${Regex.escape(uriQuote.encode(match))}%2F%3Fll%3D$LON%252C$LAT%26z%3D$Z")
                } else {
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
                    if (name == null) {
                        namePattern.find(line)?.groupOrNull()?.let {
                            name = it
                            continue
                        }
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
