package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.extensions.doubleGroupOrNull
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.network.DESKTOP_USER_AGENT
import page.ooooo.geoshare.data.network.FetchTools
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppleMapsHtmlInput @Inject constructor(
    val engine: HttpClientEngine,
    val log: Log,
    val uriQuote: UriQuote,
) : BasicOnlineInput {
    override fun getName(resources: Resources) = resources.getString(R.string.input_apple_maps_html_name)
    override val group = InputGroup.APPLE_MAPS

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        fetchTools.getBodyAsChannel(
            match,
            engine,
            log,
            uriQuote,
            // Set a custom user agent, so that Apple Maps doesn't show "Unsupported browser"
            userAgent = DESKTOP_USER_AGENT,
        ) { data ->
            parseResultAsync {
                val latPattern = Regex("""<meta property="place:location:latitude" content="$LAT"""")
                val lonPattern = Regex("""<meta property="place:location:longitude" content="$LON"""")

                var lat: Double? = null
                var lon: Double? = null

                while (true) {
                    val line = data.readLine() ?: break
                    if (lat == null) {
                        latPattern.find(line)?.doubleGroupOrNull()?.let { lat = it }
                    }
                    if (lon == null) {
                        lonPattern.find(line)?.doubleGroupOrNull()?.let { lon = it }
                    }
                    if (lat != null && lon != null) {
                        points = persistentListOf(WGS84Point(lat, lon, source = Source.HTML))
                        return@parseResultAsync
                    }
                }
            }
        }

    override fun toString() = "AppleMapsHtmlInput"
}
