package page.ooooo.geoshare.lib.geo

import com.lbt05.evil_transform.TransformUtil.outOfChina
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.io.ParseException
import org.locationtech.jts.io.WKTReader
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.ILog

private fun isPointInGeometry(
    x: Double,
    y: Double,
    @Suppress("SameParameterValue")
    wellKnownText: String,
    log: ILog = DefaultLog,
): Boolean {
    val geometryFactory = GeometryFactory()
    val geometry = try {
        WKTReader(geometryFactory).read(wellKnownText)
    } catch (e: ParseException) {
        log.e(null, "Failed to parse WKT file", e)
        return false
    }
    val point = geometryFactory.createPoint(Coordinate(x, y))
    return geometry.contains(point)
}

fun exactIsPointInChina(x: Double, y: Double, log: ILog = DefaultLog): Boolean =
    isPointInGeometry(x, y, CHINA_WELL_KNOWN_TEXT, log)

fun quickIsPointInChina(x: Double, y: Double): Boolean =
    !outOfChina(y, x)

fun isPointInChina(x: Double, y: Double, log: ILog = DefaultLog): Boolean =
    quickIsPointInChina(x, y) && exactIsPointInChina(x, y, log)
