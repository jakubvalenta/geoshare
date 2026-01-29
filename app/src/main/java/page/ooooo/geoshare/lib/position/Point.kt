package page.ooooo.geoshare.lib.position

import androidx.compose.runtime.Immutable
import com.github._46319943.bd09convertor.BD09Convertor
import com.github.wandergis.coordtransform.CoordTransform
import com.lbt05.evil_transform.GCJPointer
import com.lbt05.evil_transform.WGSPointer
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.extensions.toTrimmedString
import page.ooooo.geoshare.lib.geo.isPointInChina
import kotlin.random.Random

@Immutable
data class Point(val srs: Srs, val lat: Double = 0.0, val lon: Double = 0.0, val name: String? = null) {
    companion object {
        val example: Point = genRandomPoint(minLat = 0.0, maxLon = -100.0)

        fun genRandomPoint(
            minLat: Double = -50.0,
            maxLat: Double = 80.0,
            minLon: Double = -180.0,
            maxLon: Double = 180.0,
            name: String? = null,
        ): Point {
            val lat = Random.nextDouble(minLat, maxLat).toScale(6)
            val lon = Random.nextDouble(minLon, maxLon).toScale(6)
            val srs = if (isPointInChina(lon, lat)) Srs.GCJ02 else Srs.WGS84
            return Point(srs, lat, lon, name)
        }
    }

    val latStr get() = lat.toScale(7).toTrimmedString()
    val lonStr get() = lon.toScale(7).toTrimmedString()

    /**
     * Notice that we use a custom check whether a point is in China on top of Evil Transform's check. The reason is
     * that Evil Transform's check is only rough and considers a part of Japan as China, thus using GCJ02 for this part
     * of Japan, which is incorrect; the whole Japan uses WGS84.
     *
     * Also notice that our custom check whether a point is in China is imprecise too, because (1) we give it
     * coordinates that we don't know whether they're WGS84 or GCJ02, and (2) we use a low resolution administrative
     * boundary of China from Natural Earth, which might not exactly match the area where GCJ02 is used.
     *
     * @see isPointInChina
     */
    fun toWGS84(): Point = when (srs) {
        Srs.BD09MC -> toGCJ02().toWGS84()

        Srs.GCJ02 -> if (isPointInChina(lon, lat)) {
            GCJPointer(lat, lon).toExactWGSPointer().run { Point(Srs.WGS84, latitude, longitude) }
        } else {
            Point(Srs.WGS84, lat, lon)
        }

        Srs.WGS84 -> this
    }

    /**
     * @See toWGS84
     */
    fun toGCJ02(): Point = when (srs) {
        Srs.BD09MC -> {
            BD09Convertor.convertMC2LL(lat, lon)
                .let { (bd09Lat, bd09Lon) -> CoordTransform.bd09toGCJ02(bd09Lat, bd09Lon) }
                .let { (gcj02Lat, gcj02Lon) -> Point(Srs.GCJ02, gcj02Lat, gcj02Lon) }
        }

        Srs.GCJ02 -> this
        Srs.WGS84 -> if (isPointInChina(lon, lat)) {
            WGSPointer(lat, lon).toGCJPointer().run { Point(Srs.GCJ02, latitude, longitude) }
        } else {
            Point(Srs.GCJ02, lat, lon)
        }
    }
}
