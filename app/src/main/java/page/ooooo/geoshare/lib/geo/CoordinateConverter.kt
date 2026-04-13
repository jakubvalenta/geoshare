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

/**
 * Convert between the WGS 84, GCJ-02, and BD02MC spatial reference systems.
 *
 * Use several variants of GCJ-02, because different map services apply the GCJ-02 coordinate transformation to a
 * different area, e.g. mainland China only, mainland China and Taiwan, etc. See: [GCJ02ChinaPoint] and
 * [GCJ02ChinaAndTaiwanPoint].
 *
 * Notice that there is an unsolvable logical problem when converting from GCJ-02. To check whether a point is within
 * the area where GCJ-02 transformation applies, we need to know whether the coordinates are transformed, but we don't
 * know whether the coordinates are transformed before checking the area. So we assume users are more likely to use
 * points within the area, because these are more often on land, and we transform the coordinates first and then check
 * the area.
 */
@Singleton
class CoordinateConverter @Inject constructor(
    private val geometries: Geometries,
) {
    fun toWGS84(point: Point): WGS84Point = point.run {
        when (this) {
            is WGS84Point -> this

            is GCJ02Point -> if (lat == null || lon == null) {
                WGS84Point(lat, lon, z, name, source)
            } else {
                GCJPointer(lat, lon).toExactWGSPointer().run {
                    WGS84Point(latitude, longitude, z, name, source)
                }
            }

            is GCJ02ChinaPoint -> if (lat == null || lon == null || TransformUtil.outOfChina(lat, lon)) {
                WGS84Point(lat, lon, z, name, source)
            } else {
                // If the point is within mainland China, transform its coordinates from GCJ-02 to WGS84
                val wgs84Coords = GCJPointer(lat, lon).toExactWGSPointer()
                if (geometries.china.containsPoint(wgs84Coords.longitude, wgs84Coords.latitude)) {
                    WGS84Point(wgs84Coords.latitude, wgs84Coords.longitude, z, name, source)
                } else {
                    WGS84Point(lat, lon, z, name, source)
                }
            }

            is GCJ02ChinaAndTaiwanPoint -> if (lat == null || lon == null || TransformUtil.outOfChina(lat, lon)) {
                WGS84Point(lat, lon, z, name, source)
            } else {
                // If the point is within mainland China or Taiwan, transform its coordinates from GCJ-02 to WGS84
                val wgs84Coords = GCJPointer(lat, lon).toExactWGSPointer()
                if (geometries.chinaAndTaiwan.containsPoint(wgs84Coords.longitude, wgs84Coords.latitude)) {
                    WGS84Point(wgs84Coords.latitude, wgs84Coords.longitude, z, name, source)
                } else {
                    WGS84Point(lat, lon, z, name, source)
                }
            }

            is BD09MCPoint -> toWGS84(toGCJ02(this))
        }
    }

    fun toWGS84(points: Points): ImmutableList<WGS84Point> =
        points.map { toWGS84(it) }.toImmutableList()

    fun toGCJ02(point: Point): GCJ02Point = point.run {
        when (this) {
            is WGS84Point -> if (lat == null || lon == null) {
                GCJ02Point(lat, lon, z, name, source)
            } else {
                WGSPointer(lat, lon).toGCJPointer().run {
                    GCJ02Point(latitude, longitude, z, name, source)
                }
            }

            is GCJ02Point -> this

            is GCJ02ChinaPoint -> if (lat == null || lon == null || TransformUtil.outOfChina(lat, lon)) {
                GCJ02Point(lat, lon, z, name, source)
            } else {
                // If the point is outside mainland China, transform its coordinates from WGS 84 to GCJ-02
                val wgs84Coords = GCJPointer(lat, lon).toExactWGSPointer()
                if (geometries.china.containsPoint(wgs84Coords.longitude, wgs84Coords.latitude)) {
                    GCJ02Point(lat, lon, z, name, source)
                } else {
                    WGSPointer(lat, lon).toGCJPointer().run {
                        GCJ02Point(latitude, longitude, z, name, source)
                    }
                }
            }

            is GCJ02ChinaAndTaiwanPoint -> if (lat == null || lon == null || TransformUtil.outOfChina(lat, lon)) {
                GCJ02Point(lat, lon, z, name, source)
            } else {
                // If the point is outside mainland China or Taiwan, transform its coordinates from WGS 84 to GCJ-02
                val wgs84Coords = GCJPointer(lat, lon).toExactWGSPointer()
                if (geometries.chinaAndTaiwan.containsPoint(wgs84Coords.longitude, wgs84Coords.latitude)) {
                    GCJ02Point(lat, lon, z, name, source)
                } else {
                    WGSPointer(lat, lon).toGCJPointer().run {
                        GCJ02Point(latitude, longitude, z, name, source)
                    }
                }
            }

            is BD09MCPoint -> if (lat == null || lon == null) {
                GCJ02Point(lat, lon, z, name, source)
            } else {
                BD09Convertor.convertMC2LL(lat, lon)
                    .let { (bd09Lat, bd09Lon) -> CoordTransform.bd09toGCJ02(bd09Lat, bd09Lon) }
                    .let { (gcj02Lat, gcj02Lon) -> GCJ02Point(gcj02Lat, gcj02Lon, z, name, source) }
            }
        }
    }

    fun toGCJ02China(point: Point): GCJ02ChinaPoint = point.run {
        when (this) {
            is WGS84Point -> if (lat == null || lon == null || TransformUtil.outOfChina(lat, lon)) {
                GCJ02ChinaPoint(lat, lon, z, name, source)
            } else {
                // If the point is within mainland China, transform its coordinates from WGS 84 to GCJ-02
                if (geometries.china.containsPoint(lon, lat)) {
                    WGSPointer(lat, lon).toGCJPointer().run {
                        GCJ02ChinaPoint(latitude, longitude, z, name, source)
                    }
                } else {
                    GCJ02ChinaPoint(lat, lon, z, name, source)
                }
            }

            is GCJ02Point -> if (lat == null || lon == null || TransformUtil.outOfChina(lat, lon)) {
                GCJ02ChinaPoint(lat, lon, z, name, source)
            } else {
                // If the point is outside mainland China, transform its coordinates from GCJ-02 to WGS 84
                val wgs84Coords = GCJPointer(lat, lon).toExactWGSPointer()
                if (geometries.china.containsPoint(wgs84Coords.longitude, wgs84Coords.latitude)) {
                    GCJ02ChinaPoint(lat, lon, z, name, source)
                } else {
                    GCJ02ChinaPoint(wgs84Coords.latitude, wgs84Coords.longitude, z, name, source)
                }
            }

            is GCJ02ChinaPoint -> this

            is GCJ02ChinaAndTaiwanPoint -> if (lat == null || lon == null || TransformUtil.outOfChina(lat, lon)) {
                GCJ02ChinaPoint(lat, lon, z, name, source)
            } else {
                // If the point is within Taiwan, transform its coordinates from GCJ-02 to WGS 84
                val wgs84Coords = GCJPointer(lat, lon).toExactWGSPointer()
                if (
                    !geometries.china.containsPoint(wgs84Coords.longitude, wgs84Coords.latitude) &&
                    geometries.chinaAndTaiwan.containsPoint(wgs84Coords.longitude, wgs84Coords.latitude)
                ) {
                    GCJ02ChinaPoint(wgs84Coords.latitude, wgs84Coords.longitude, z, name, source)
                } else {
                    GCJ02ChinaPoint(lat, lon, z, name, source)
                }
            }

            is BD09MCPoint -> toGCJ02China(toGCJ02(point))
        }
    }

    fun toGCJ02ChinaAndTaiwan(point: Point): GCJ02ChinaAndTaiwanPoint = point.run {
        when (this) {
            is WGS84Point -> if (lat == null || lon == null || TransformUtil.outOfChina(lat, lon)) {
                GCJ02ChinaAndTaiwanPoint(lat, lon, z, name, source)
            } else {
                // If the point is within mainland China or Taiwan, transform its coordinates from WGS 84 to GCJ-02
                if (geometries.chinaAndTaiwan.containsPoint(lon, lat)) {
                    WGSPointer(lat, lon).toGCJPointer().run {
                        GCJ02ChinaAndTaiwanPoint(latitude, longitude, z, name, source)
                    }
                } else {
                    GCJ02ChinaAndTaiwanPoint(lat, lon, z, name, source)
                }
            }

            is GCJ02Point -> if (lat == null || lon == null || TransformUtil.outOfChina(lat, lon)) {
                GCJ02ChinaAndTaiwanPoint(lat, lon, z, name, source)
            } else {
                // If the point is outside mainland China or Taiwan, transform its coordinates from GCJ-02 to WGS 84
                val wgs84Coords = GCJPointer(lat, lon).toExactWGSPointer()
                if (geometries.chinaAndTaiwan.containsPoint(wgs84Coords.longitude, wgs84Coords.latitude)) {
                    GCJ02ChinaAndTaiwanPoint(lat, lon, z, name, source)
                } else {
                    GCJ02ChinaAndTaiwanPoint(wgs84Coords.latitude, wgs84Coords.longitude, z, name, source)
                }
            }

            is GCJ02ChinaPoint -> if (lat == null || lon == null || TransformUtil.outOfChina(lat, lon)) {
                GCJ02ChinaAndTaiwanPoint(lat, lon, z, name, source)
            } else {
                // If the point is within Taiwan, transform its coordinates from WGS 84 to GCJ-02
                val wgs84Coords = GCJPointer(lat, lon).toExactWGSPointer()
                if (
                    !geometries.china.containsPoint(wgs84Coords.longitude, wgs84Coords.latitude) &&
                    geometries.chinaAndTaiwan.containsPoint(wgs84Coords.longitude, wgs84Coords.latitude)
                ) {
                    WGSPointer(lat, lon).toGCJPointer().run {
                        GCJ02ChinaAndTaiwanPoint(latitude, longitude, z, name, source)
                    }
                } else {
                    GCJ02ChinaAndTaiwanPoint(lat, lon, z, name, source)
                }
            }

            is GCJ02ChinaAndTaiwanPoint -> this

            is BD09MCPoint -> toGCJ02ChinaAndTaiwan(toGCJ02(point))
        }
    }
}
