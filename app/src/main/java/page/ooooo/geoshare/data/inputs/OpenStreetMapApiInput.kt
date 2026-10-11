package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.extensions.toLatLonPoint
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.network.FetchTools
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenStreetMapApiInput @Inject constructor(
    val engine: HttpClientEngine,
    val log: Log,
    val uriQuote: UriQuote,
) : BasicOnlineInput {
    override fun getName(resources: Resources) = resources.getString(R.string.input_open_street_map_api_name)
    override val group = InputGroup.OPEN_STREET_MAP

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        fetchTools.getBodyAsText(match, engine, log, uriQuote) { data ->
            parseResult {
                // Use a simple regex instead of JSON parsing, because it works fine
                val pattern = Regex(""""lat":$LAT,"lon":$LON""")

                points = pattern
                    .findAll(data)
                    .mapNotNull { m -> m.toLatLonPoint(Source.API)?.let { WGS84Point(it) } }
                    .toImmutableList()
            }
        }

    override fun toString() = "OpenStreetMapApiInput"

    companion object {
        enum class ElementType(val value: String) {
            NODE("node"),
            RELATION("relation"),
            WAY("way"),
        }

        fun formatUrlString(type: ElementType, id: Long): String =
            "https://www.openstreetmap.org/api/0.6/${type.value}/$id${if (type != ElementType.NODE) "/full" else ""}.json"
    }
}
