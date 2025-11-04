package page.ooooo.geoshare.lib

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class Position(
    val points: ImmutableList<Point>? = null,
    val q: String? = null,
    val z: String? = null,
) {
    companion object {
        val example: Position = Position(persistentListOf(Point.example), z = "8")

        fun genRandomPosition(
            minLat: Double = -50.0,
            maxLat: Double = 80.0,
            minLon: Double = -180.0,
            maxLon: Double = 180.0,
        ): Position = Position(persistentListOf(Point.genRandomPoint(minLat, maxLat, minLon, maxLon)), z = "8")
    }

    constructor(
        lat: String,
        lon: String,
        q: String? = null,
        z: String? = null,
    ) : this(persistentListOf(Point(lat, lon)), q, z)

    val mainPoint: Point? get() = points?.lastOrNull()
}
