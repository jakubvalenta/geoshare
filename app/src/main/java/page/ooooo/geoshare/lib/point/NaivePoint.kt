package page.ooooo.geoshare.lib.point

import androidx.compose.runtime.Immutable

@Immutable
data class NaivePoint(
    val lat: Double? = null,
    val lon: Double? = null,
    val z: Double? = null,
    val name: String? = null,
) {
    fun hasCoordinates(): Boolean = lat != null && lon != null

    fun hasName(): Boolean = !name.isNullOrEmpty()

    fun hasZ(): Boolean = z != null

    fun setDefaults(defaultZ: Double?, defaultName: String?): NaivePoint =
        this
            .run { if (!hasZ() && defaultZ != null) copy(z = defaultZ) else this }
            .run { if (!hasName() && !defaultName.isNullOrEmpty()) copy(name = defaultName) else this }

    fun asWGS84() = WGS84Point(lat, lon, z, name)

    fun asGCJ02() = GCJ02Point(lat, lon, z, name)

    fun asBD09MC() = BD09MCPoint(lat, lon, z, name)
}
