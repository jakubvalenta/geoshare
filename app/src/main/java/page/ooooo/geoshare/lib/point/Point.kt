package page.ooooo.geoshare.lib.point

import androidx.compose.runtime.Immutable
import com.github._46319943.bd09convertor.BD09Convertor
import com.github.wandergis.coordtransform.CoordTransform
import com.lbt05.evil_transform.GCJPointer
import com.lbt05.evil_transform.WGSPointer
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.extensions.toTrimmedString
import page.ooooo.geoshare.lib.geo.isPointInChina
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
            return if (isPointInChina(lon, lat)) {
                GCJ02Point(lat, lon, z, name, source)
            } else {
                WGS84Point(lat, lon, z, name, source)
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

    fun toWGS84(): WGS84Point

    fun toGCJ02(): GCJ02Point

    fun formatUriString(
        coordsUriTemplate: String,
        nameUriTemplate: String = "",
        srs: Srs = Srs.WGS84,
        defaultZ: Double = 16.0,
        uriQuote: UriQuote = DefaultUriQuote,
    ): String? =
        when (srs) {
            Srs.WGS84 -> toWGS84()
            Srs.GCJ02 -> toGCJ02()
        }
            .run {
                latStr?.let { latStr ->
                    lonStr?.let { lonStr ->
                        val zOrDefaultStr = (z ?: defaultZ).toScale(7).toTrimmedString()
                        coordsUriTemplate
                            .replace("{lat}", uriQuote.encode(latStr))
                            .replace("{lon}", uriQuote.encode(lonStr))
                            .replace("{z}", uriQuote.encode(zOrDefaultStr))
                            .replace("{name}", uriQuote.encode(cleanName.orEmpty()))
                            .takeIf { it.isNotEmpty() }
                    }
                } ?: cleanName?.let { cleanName ->
                    nameUriTemplate
                        .replace("{q}", uriQuote.encode(cleanName))
                        .takeIf { it.isNotEmpty() }
                }
            }
}

@Immutable
data class WGS84Point(
    override val lat: Double? = null,
    override val lon: Double? = null,
    override val z: Double? = null,
    override val name: String? = null,
    override val source: Source,
) : Point {
    override fun toWGS84() = this

    /**
     * @See GCJ02Point.toWGS84
     */
    override fun toGCJ02() = if (lat == null || lon == null || !isPointInChina(lon, lat)) {
        GCJ02Point(lat, lon, z, name, source)
    } else {
        WGSPointer(lat, lon).toGCJPointer().run { GCJ02Point(latitude, longitude, z, name, source) }
    }
}

@Immutable
data class GCJ02Point(
    override val lat: Double? = null,
    override val lon: Double? = null,
    override val z: Double? = null,
    override val name: String? = null,
    override val source: Source,
) : Point {
    /**
     * Notice that we use a custom check whether a point is in China on top of Evil Transform's check. The reason is
     * that Evil Transform's check is only rough and considers a part of Japan as China, thus using GCJ02 for this part
     * of Japan, which is incorrect; the whole Japan uses WGS84.
     *
     * Also notice that our custom check whether a point is in China is imprecise too, because:
     * 1. We give it coordinates that we don't know whether they're WGS84 or GCJ02.
     * 2. We use a low resolution administrative boundary of China from Natural Earth, which might not exactly match the
     *    area where GCJ02 is used.
     *
     * @see isPointInChina
     */
    override fun toWGS84() = if (lat == null || lon == null || !isPointInChina(lon, lat)) {
        WGS84Point(lat, lon, z, name, source)
    } else {
        GCJPointer(lat, lon).toExactWGSPointer().run { WGS84Point(latitude, longitude, z, name, source) }
    }

    override fun toGCJ02() = this
}

@Immutable
data class BD09MCPoint(
    override val lat: Double? = null,
    override val lon: Double? = null,
    override val z: Double? = null,
    override val name: String? = null,
    override val source: Source,
) : Point {
    override fun toWGS84() = toGCJ02().toWGS84()

    override fun toGCJ02() = if (lat == null || lon == null) {
        GCJ02Point(lat, lon, z, name, source)
    } else {
        BD09Convertor.convertMC2LL(lat, lon).let { (bd09Lat, bd09Lon) -> CoordTransform.bd09toGCJ02(bd09Lat, bd09Lon) }
            .let { (gcj02Lat, gcj02Lon) -> GCJ02Point(gcj02Lat, gcj02Lon, z, name, source) }
    }
}
