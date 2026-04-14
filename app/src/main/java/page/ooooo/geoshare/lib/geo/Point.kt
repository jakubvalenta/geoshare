package page.ooooo.geoshare.lib.geo

import androidx.compose.runtime.Immutable
import com.lbt05.evil_transform.TransformUtil
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.extensions.toTrimmedString
import kotlin.random.Random

sealed interface Point {
    val lat: Double?
    val lon: Double?
    val z: Double?
    val name: String?
    val source: Source

    companion object {
        val example: Point = genRandomPoint(minLat = 0.0, maxLon = -100.0)

        fun genRandomPoint(
            minLat: Double = -50.0,
            maxLat: Double = 80.0,
            minLon: Double = -180.0,
            maxLon: Double = 180.0,
            z: Double = 16.0,
            name: String? = null,
            source: Source = Source.GENERATED,
        ): Point {
            val lat = Random.nextDouble(minLat, maxLat).toScale(6)
            val lon = Random.nextDouble(minLon, maxLon).toScale(6)
            return if (TransformUtil.outOfChina(lon, lat)) {
                WGS84Point(lat, lon, z, name, source)
            } else {
                GCJ02Point(lat, lon, z, name, source)
            }
        }
    }

    val latStr: String?
        get() = lat?.toScale(7)?.toTrimmedString()
    val lonStr: String?
        get() = lon?.toScale(7)?.toTrimmedString()
    val zStr: String?
        get() = z?.toScale(7)?.toTrimmedString()
    val cleanName: String?
        get() = name?.replace('+', ' ')

    fun hasCoordinates(): Boolean = lat != null && lon != null

    fun hasName(): Boolean = !name.isNullOrEmpty()
}

/**
 * Point that has coordinates in the international WGS 84 spatial reference system.
 *
 * Used by OpenStreetMap and many other services.
 */
@Immutable
data class WGS84Point(
    override val lat: Double? = null,
    override val lon: Double? = null,
    override val z: Double? = null,
    override val name: String? = null,
    override val source: Source,
) : Point {
    constructor(naivePoint: NaivePoint) : this(
        naivePoint.lat,
        naivePoint.lon,
        naivePoint.z,
        naivePoint.name,
        naivePoint.source,
    )
}

/**
 * Point that has coordinates in the GCJ-02 spatial reference system when it's within a large rectangle that includes
 * mainland China, Taiwan, Korea, and western Japan. Everywhere else the coordinates are in WGS 84.
 *
 * Used by Evil Transform.
 */
@Immutable
data class GCJ02Point(
    override val lat: Double? = null,
    override val lon: Double? = null,
    override val z: Double? = null,
    override val name: String? = null,
    override val source: Source,
) : Point {
    constructor(naivePoint: NaivePoint) : this(
        naivePoint.lat,
        naivePoint.lon,
        naivePoint.z,
        naivePoint.name,
        naivePoint.source,
    )
}

/**
 * Point that has coordinates in the GCJ-02 spatial reference system when it's within mainland China. Everywhere else
 * the coordinates are in WGS 84.
 *
 * Used by Google Maps.
 */
@Immutable
data class GCJ02MainlandChinaPoint(
    override val lat: Double? = null,
    override val lon: Double? = null,
    override val z: Double? = null,
    override val name: String? = null,
    override val source: Source,
) : Point {
    constructor(naivePoint: NaivePoint) : this(
        naivePoint.lat,
        naivePoint.lon,
        naivePoint.z,
        naivePoint.name,
        naivePoint.source,
    )
}

/**
 * Point that has coordinates in the GCJ-02 spatial reference system when it's within mainland China or Taiwan.
 * Everywhere else the coordinates are in WGS 84.
 *
 * Used by Amap.
 */
@Immutable
data class GCJ02MainlandChinaAndTaiwanPoint(
    override val lat: Double? = null,
    override val lon: Double? = null,
    override val z: Double? = null,
    override val name: String? = null,
    override val source: Source,
) : Point {
    constructor(naivePoint: NaivePoint) : this(
        naivePoint.lat,
        naivePoint.lon,
        naivePoint.z,
        naivePoint.name,
        naivePoint.source,
    )
}

/**
 * Point that has coordinates in the Baidu Map spatial reference system.
 */
@Immutable
data class BD09MCPoint(
    override val lat: Double? = null,
    override val lon: Double? = null,
    override val z: Double? = null,
    override val name: String? = null,
    override val source: Source,
) : Point {
    constructor(naivePoint: NaivePoint) : this(
        naivePoint.lat,
        naivePoint.lon,
        naivePoint.z,
        naivePoint.name,
        naivePoint.source,
    )
}
