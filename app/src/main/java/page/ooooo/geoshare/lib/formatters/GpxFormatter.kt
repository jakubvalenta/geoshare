package page.ooooo.geoshare.lib.formatters

import io.ktor.util.escapeHTML
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Points
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpxFormatter @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
) {
    fun writeGpxPoints(points: Points, writer: Appendable) = writeGpx(writer) {
        coordinateConverter.toWGS84(points).filter { it.lat != null && it.lon != null }.forEach { point ->
            point.run {
                append("<wpt lat=\"$latStr\" lon=\"$lonStr\"")
            }
            if (point.name != null) {
                append(">\n")
                append("    <name>${point.name.escapeHTML()}</name>\n")
                append("</wpt>\n")
            } else {
                append(" />\n")
            }
        }
    }

    fun writeGpxRoute(points: Points, writer: Appendable) = writeGpx(writer) {
        append("<rte>\n")
        coordinateConverter.toWGS84(points).filter { it.lat != null && it.lon != null }.forEach { point ->
            point.run {
                @Suppress("SpellCheckingInspection")
                append("<rtept lat=\"$latStr\" lon=\"$lonStr\"")
            }
            if (point.name != null) {
                append(">\n")
                append("    <name>${point.name.escapeHTML()}</name>\n")
                @Suppress("SpellCheckingInspection")
                append("</rtept>\n")
            } else {
                append(" />\n")
            }
        }
        append("</rte>\n")
    }

    private fun writeGpx(writer: Appendable, block: Appendable.() -> Unit) {
        writer.apply {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n")
            append("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\"\n")
            append("     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n")
            append("     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n")
            writer.block()
            append("</gpx>\n")
        }
    }
}
