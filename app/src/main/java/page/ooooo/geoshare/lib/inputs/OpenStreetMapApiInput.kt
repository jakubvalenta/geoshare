package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenStreetMapApiInput @Inject constructor(
    override val engine: HttpClientEngine,
    override val log: Log,
    override val uriQuote: UriQuote,
) : BodyAsTextInput {
    override fun getName(resources: Resources) = "OpenStreetMap API"

    override suspend fun parse(data: String, match: String, resources: Resources) = parseResult {
        // Use a simple regex instead of JSON parsing, because it works fine
        val pattern = Regex(""""lat":$LAT,"lon":$LON""")

        points = pattern
            .findAll(data)
            .mapNotNull { m -> m.toLatLonPoint(Source.API)?.let { WGS84Point(it) } }
            .toImmutableList()
    }

    override fun toString() = "OpenStreetMapApiInput"
}
