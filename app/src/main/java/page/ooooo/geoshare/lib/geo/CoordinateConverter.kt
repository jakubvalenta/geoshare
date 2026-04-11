package page.ooooo.geoshare.lib.geo

import com.github._46319943.bd09convertor.BD09Convertor
import com.github.wandergis.coordtransform.CoordTransform
import com.lbt05.evil_transform.GCJPointer
import com.lbt05.evil_transform.TransformUtil
import com.lbt05.evil_transform.WGSPointer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoordinateConverter @Inject constructor(
    private val chinaGeometry: ChinaGeometry,
) {
    fun toWGS84(point: Point): WGS84Point = point.run {
        when (this) {
            is WGS84Point -> this

            is GCJ02Point -> {
                if (lat == null || lon == null) {
                    WGS84Point(lat, lon, z, name, source)
                } else {
                    GCJPointer(lat, lon).toExactWGSPointer()
                        .run { WGS84Point(latitude, longitude, z, name, source) }
                }
            }

            is BD09MCPoint -> {
                toWGS84(toGCJ02(this))
            }

            is GoogleMapsPoint -> {
                toWGS84(toWGS84OrGCJ02Point(this))
            }
        }
    }

    fun toWGS84(points: Points): ImmutableList<WGS84Point> =
        points.map { toWGS84(it) }.toImmutableList()

    fun toGCJ02(point: Point): GCJ02Point = point.run {
        when (this) {
            is WGS84Point -> {
                if (lat == null || lon == null) {
                    GCJ02Point(lat, lon, z, name, source)
                } else {
                    WGSPointer(lat, lon).toGCJPointer()
                        .run { GCJ02Point(latitude, longitude, z, name, source) }
                }
            }

            is GCJ02Point -> this

            is BD09MCPoint -> {
                if (lat == null || lon == null) {
                    GCJ02Point(lat, lon, z, name, source)
                } else {
                    BD09Convertor.convertMC2LL(lat, lon)
                        .let { (bd09Lat, bd09Lon) -> CoordTransform.bd09toGCJ02(bd09Lat, bd09Lon) }
                        .let { (gcj02Lat, gcj02Lon) ->
                            GCJ02Point(
                                gcj02Lat,
                                gcj02Lon,
                                z,
                                name,
                                source
                            )
                        }
                }
            }

            is GoogleMapsPoint -> {
                toGCJ02(toWGS84OrGCJ02Point(this))
            }
        }
    }

    /**
     * See [GoogleMapsPoint]
     */
    // TODO Test
    fun toGoogleMaps(point: Point): GoogleMapsPoint = point.run {
        when (this) {
            is GCJ02Point -> {
                if (lat == null || lon == null || TransformUtil.outOfChina(lat, lon)) {
                    GoogleMapsPoint(lat, lon, z, name, source)
                } else {
                    val wgs84Pointer = GCJPointer(lat, lon).toExactWGSPointer()
                    if (chinaGeometry.containsPoint(wgs84Pointer.longitude, wgs84Pointer.latitude)) {
                        GoogleMapsPoint(lat, lon, z, name, source)
                    } else {
                        // The point is outside China, so convert it to WGS 84
                        GoogleMapsPoint(
                            wgs84Pointer.latitude,
                            wgs84Pointer.longitude,
                            z,
                            name,
                            source
                        )
                    }
                }
            }

            is WGS84Point -> {
                if (lat == null || lon == null || TransformUtil.outOfChina(lat, lon)) {
                    GoogleMapsPoint(lat, lon, z, name, source)
                } else {
                    if (chinaGeometry.containsPoint(lon, lat)) {
                        // The point is inside China, so convert it to GCJ-02
                        val gcJ02Pointer = WGSPointer(lat, lon).toGCJPointer()
                        GoogleMapsPoint(
                            gcJ02Pointer.latitude,
                            gcJ02Pointer.longitude,
                            z,
                            name,
                            source
                        )
                    } else {
                        GoogleMapsPoint(lat, lon, z, name, source)
                    }
                }
            }

            is BD09MCPoint -> toGoogleMaps(toGCJ02(point))

            is GoogleMapsPoint -> this
        }
    }

    private fun toWGS84OrGCJ02Point(point: GoogleMapsPoint): Point = point.run {
        if (lat == null || lon == null || TransformUtil.outOfChina(lat, lon)) {
            WGS84Point(lat, lon, z, name, source)
        } else {
            // Assume the point is a GCJ-02 point and calculate whether it's inside China
            if (
                GCJPointer(lat, lon).toExactWGSPointer()
                    .run { chinaGeometry.containsPoint(longitude, latitude) }
            ) {
                // The GCJ-02 point is inside China, which means it's indeed GCJ-02. Or it's actually a WGS 84 point outside
                // China, which we've accidentally placed inside China by mistakenly treating it as GCJ-02. There is no way
                // for us to know, so let's assume users are more likely to convert points inside China than points outside
                // China, because these are often on the sea, and not convert the point.
                GCJ02Point(lat, lon, z, name, source)
            } else {
                // The point is outside China, which means it's in WGS 84, so convert it to GCJ-02
                WGS84Point(lat, lon, z, name, source)
            }
        }
    }
}
