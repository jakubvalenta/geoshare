package page.ooooo.geoshare.lib

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

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
        ): Position = Position(persistentListOf(Point.genRandomPoint(minLat, maxLat, minLon, maxLon)), z = 8.0)
    }

    constructor(
        lat: Double,
        lon: Double,
        q: String? = null,
        z: Double? = null,
        desc: String? = null,
    ) : this(persistentListOf(Point(lat, lon, desc = desc)), q, z)

    val mainPoint: Point? get() = points?.lastOrNull()

    val zStr: String? get() = z?.toScale(7)?.toTrimmedString()

    fun toGCJ(): Position = this.copy(points = points?.map { it.toGCJ() }?.toImmutableList())
}
