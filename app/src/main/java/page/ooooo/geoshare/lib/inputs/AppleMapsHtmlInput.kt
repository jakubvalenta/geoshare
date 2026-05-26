package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.client.engine.HttpClientEngine
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.network.DESKTOP_USER_AGENT
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppleMapsHtmlInput @Inject constructor(
    override val engine: HttpClientEngine,
    override val log: Log,
    override val uriQuote: UriQuote,
) : BodyAsChannelInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title

    // Use custom user agent instead of BrowserUserAgent, so that Apple Maps doesn't show "Unsupported browser"
    override val userAgent = DESKTOP_USER_AGENT

    override suspend fun parse(data: ByteReadChannel, match: String) = parseResult {
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
                return@parseResult
            }
        }
    }

    override fun toString() = "AppleMapsHtmlInput"
}
