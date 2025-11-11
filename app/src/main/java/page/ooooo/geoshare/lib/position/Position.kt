package page.ooooo.geoshare.lib.position

import androidx.compose.runtime.Immutable
import com.google.re2j.Matcher
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.extensions.toTrimmedString
import kotlin.math.max
import kotlin.math.min

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
        srs: Srs,
        lat: Double,
        lon: Double,
        q: String? = null,
        z: Double? = null,
        desc: String? = null,
    ) : this(persistentListOf(Point(srs, lat, lon, desc)), q, z)

    constructor(
        srs: Srs,
        q: String? = null,
        z: Double? = null,
        desc: String? = null,
    ) : this(persistentListOf(), q, z)

    val mainPoint: Point? get() = points?.lastOrNull()

    val pointCount: Int get() = points?.size ?: 0

    val zStr: String? get() = z?.toScale(7)?.toTrimmedString()
}

fun Matcher.toLatLon(srs: Srs): Position? =
    this.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
        this.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
            Position(srs, lat = lat, lon = lon)
        }
    }

fun Matcher.toLatLonZ(srs: Srs): Position? =
    this.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
        this.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
            this.groupOrNull("z")?.toDoubleOrNull()?.let { z ->
                Position(srs, lat = lat, lon = lon, z = max(1.0, min(21.0, z)))
            }
        }
    }

fun Matcher.toQ(srs: Srs): Position? =
    this.groupOrNull("q")?.let { q ->
        Position(srs, q = q)
    }

fun Matcher.toZ(srs: Srs): Position? =
    this.groupOrNull("z")?.toDoubleOrNull()?.let {
        Position(srs, z = max(1.0, min(21.0, it)))
    }

fun List<Position>.merge(): Position {
    val points: MutableList<Point> = mutableListOf()
    var q: String? = null
    var z: Double? = null
    for (position in this) {
        if (position.points != null) {
            points.addAll(position.points)
        }
        if (position.q != null) {
            q = position.q
        }
        if (position.z != null) {
            z = position.z
        }
    }
    return Position(points.takeIf { it.isNotEmpty() }?.toImmutableList(), q = q, z = z)
}
