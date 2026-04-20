package page.ooooo.geoshare.lib.formatters

import com.google.openlocationcode.OpenLocationCode
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint

object PlusCodeFormatter {
    /**
     * Returns Plus Code string for [point].
     *
     * See [page.ooooo.geoshare.lib.inputs.PlusCodeInput] for explanation on why we use [GCJ02MainlandChinaPoint].
     */
    fun formatPlusCode(point: GCJ02MainlandChinaPoint): String? =
        point.lat?.let { lat ->
            point.lon?.let { lon ->
                OpenLocationCode(lat, lon, 11).code
            }
        }
}
