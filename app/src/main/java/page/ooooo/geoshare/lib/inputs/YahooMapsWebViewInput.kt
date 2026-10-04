package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import android.webkit.WebSettings
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.network.DESKTOP_USER_AGENT
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YahooMapsWebViewInput @Inject constructor(
    private val log: Log,
) : WebViewInput {
    @Serializable
    private data class ExtractedPoint(val lat: Double, val lon: Double)

    override fun getName(resources: Resources) = resources.getString(R.string.input_yahoo_maps_web_view_name)
    override val group = InputGroup.YAHOO_MAPS

    /**
     * Extracts point from the page URL after the page JavaScript has changed the URL.
     *
     * First time the function is run it only remembers the extracted point and when it's run again and the point is
     * different, then it returns the point.
     */
    // language=JavaScript
    override fun getUnsafeExtractionJavaScript(match: String) = """
        () => {
            const queryParams = new URLSearchParams(location.search);
            const lat = parseFloat(queryParams.get("lat"));
            const lon = parseFloat(queryParams.get("lon"));
            if (!Number.isNaN(lat) && !Number.isNaN(lon)) {
                if (window.__firstLat === undefined || window.__firstLon === undefined) {
                    window.__firstLat = lat;
                    window.__firstLon = lon;
                } else if (window.__firstLat !== lat && window.__firstLon !== lon) {
                    return JSON.stringify({ lat: lat, lon: lon });
                }
            }
            return undefined;
        };
    """.trimIndent()

    override suspend fun parse(data: String, match: String, resources: Resources) = parseResult {
        val json = Json {
            explicitNulls = false
        }
        try {
            json.decodeFromString<ExtractedPoint>(data)
        } catch (tr: IllegalArgumentException) {
            log.e(TAG, "Deserialization error", tr)
            null
        }?.run {
            points = persistentListOf(WGS84Point(lat, lon, source = Source.URI))
        }
    }

    /**
     * Sets a desktop user agent, so that Yahoo! Maps don't render a mobile version of the page, which contains some UI
     * elements that shift the map view and cause a coordinate offset.
     */
    override fun extendWebSettings(settings: WebSettings) {
        settings.userAgentString = DESKTOP_USER_AGENT
    }

    /**
     * It doesn't block CSS, map tiles, Mapbox fonts, or Mapbox styles, so the page loads.
     */
    override fun shouldInterceptRequest(requestUrlString: String) =
        // Assets
        requestUrlString.endsWith(".ico")
            || requestUrlString.contains(".png")
            || requestUrlString.contains(".webp")

            // 3D map tiles
            || requestUrlString.contains(".glb")

            // Tracking
            || requestUrlString.contains("clb.yahoo.co.jp/")
            || requestUrlString.contains("events.mapbox.com/")
            || requestUrlString.contains("yjtag.yahoo.co.jp/")

            // Unknown
            || requestUrlString.contains("/map-sessions")
            || requestUrlString.contains("/photo")
            || requestUrlString.contains("/review")
            || requestUrlString.contains("dsb.yahoo.co.jp/")
            || requestUrlString.contains("mapapp-pctr.c.yimg.jp/")

    override fun toString() = TAG

    private companion object {
        private const val TAG = "YahooMapsWebViewInput"
    }
}
