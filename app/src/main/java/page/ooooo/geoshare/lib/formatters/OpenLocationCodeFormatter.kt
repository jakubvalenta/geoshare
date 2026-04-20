package page.ooooo.geoshare.lib.formatters

import com.google.openlocationcode.OpenLocationCode
import page.ooooo.geoshare.lib.geo.Point

object OpenLocationCodeFormatter {
    // TODO Check if Google uses WGS 84 or GCJ-02 for Plus Codes
    fun formatOpenLocationCode(point: Point): String? =
        point.lat?.let { lat ->
            point.lon?.let { lon ->
                OpenLocationCode(lat, lon, 11).code
            }
        }
}
