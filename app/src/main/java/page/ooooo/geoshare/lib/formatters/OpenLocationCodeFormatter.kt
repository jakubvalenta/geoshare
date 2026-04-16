package page.ooooo.geoshare.lib.formatters

import page.ooooo.geoshare.lib.geo.Point

object OpenLocationCodeFormatter {
    // TODO Check if Google uses WGS 84 or GCJ-02 for Plus Codes
    fun formatOpenLocationCode(point: Point): String =
        OpenLocationCode(point.lat, point.lon, length = 11).getCode()
}
