package page.ooooo.geoshare.lib.position

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.extensions.toTrimmedString

@Immutable
data class Position(
    val points: ImmutableList<Point>? = null,
    val q: String? = null,
    val z: Double? = null,
) {
    companion object {
        val example: Position = Position(persistentListOf(Point.example), z = 8.0)

        fun genRandomPosition(
            minLat: Double = -50.0,
            maxLat: Double = 80.0,
            minLon: Double = -180.0,
            maxLon: Double = 180.0,
            name: String = "My point",
        ): Position = Position(persistentListOf(Point.genRandomPoint(minLat, maxLat, minLon, maxLon, name)), z = 8.0)
    }

    constructor(
        srs: Srs,
        lat: Double,
        lon: Double,
        q: String? = null,
        z: Double? = null,
        name: String? = null,
    ) : this(persistentListOf(Point(srs, lat, lon, name)), q, z)

    constructor(
        srs: Srs,
        q: String? = null,
        z: Double? = null,
    ) : this(null, q, z)

    val mainPoint: Point? get() = points?.lastOrNull()

    val pointCount: Int get() = points?.size ?: 0

    val zStr: String? get() = z?.toScale(7)?.toTrimmedString()
}
