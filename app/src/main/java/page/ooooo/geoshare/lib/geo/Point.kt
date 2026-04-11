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

/**
 * A coordinate system used by Google Maps, in which points that are inside the land borders of mainland China use
 * GCJ-02, and all other points (including Chinese sea and Taiwan) use WGS 84.
 *
 * However, there are points in the sea where this logic predicts Google Maps to use WGS 84, but Google Maps seems to
 * use GCJ-02 is used instead, e.g. 38.30121535762941,120.81016278215878; we use WGS 84 for these points, which results
 * in inaccuracies.
 *
 * To calculate whether a point is inside the land borders of mainland China, we always start with a quick check using
 * [outOfChina], and only if it fails we do an exact calculation using [ChinaGeometry].
 */
@Immutable
data class GoogleMapsPoint(
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
