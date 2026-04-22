package page.ooooo.geoshare.lib.formatters

import com.google.openlocationcode.OpenLocationCode
import page.ooooo.geoshare.lib.geo.Point

object PlusCodeFormatter {
    /**
     * Returns Plus Code string for [point].
     *
     * The method accepts [point] in any coordinate system. It's a responsibility of the caller to convert the point to
     * desired coordinate system. For example when used in Google Maps, Plus Codes should be in the GCJ02MainlandChina
     * coordinate system.
     */
    fun formatPlusCode(point: Point): String? =
        point.lat?.let { lat ->
            point.lon?.let { lon ->
                OpenLocationCode(lat, lon, 11).code
            }
        }
}
