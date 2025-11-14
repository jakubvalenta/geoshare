package page.ooooo.geoshare.lib.position

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.max
import kotlin.math.min

class PositionBuilder(val srs: Srs) {

    private var points: MutableList<Point> = mutableListOf()
    private var defaultPoint: Point? = null
    private var q: String? = null
    private var z: Double? = null
    private var uriString: String? = null

    val position: Position
        get() = Position(
            points.takeIf { it.isNotEmpty() }?.toImmutableList()
                ?: defaultPoint?.let { persistentListOf(it) },
            q = q,
            z = z?.let { max(1.0, min(21.0, it)) },
        )

    fun toPair(): Pair<Position, String?> = position to uriString

    fun setPointIfEmpty(block: () -> LatLonZ?) {
        if (points.isEmpty()) {
            block()?.let { (lat, lon, newZ) ->
                points.add(Point(srs, lat, lon))
                if (newZ != null) {
                    z = newZ
                }
            }
        }
    }

    fun setDefaultPointIfEmpty(block: () -> LatLonZ?) {
        if (defaultPoint == null) {
            block()?.let { (lat, lon, newZ) ->
                defaultPoint = Point(srs, lat, lon)
                if (newZ != null) {
                    z = newZ
                }
            }
        }
    }

    fun addPoint(block: () -> LatLonZ?) {
        block()?.let { (lat, lon) -> points.add(Point(srs, lat, lon)) }
    }

    fun addPoints(block: () -> Sequence<LatLonZ>) {
        points.addAll(block().map { (lat, lon) -> Point(srs, lat, lon) })
    }

    fun setQIfEmpty(block: () -> String?) {
        if (q == null && defaultPoint == null && points.isEmpty()) {
            q = block()
        }
    }

    fun setQWithCenterIfEmpty(block: () -> Triple<String, Double, Double>?) {
        if (q == null && defaultPoint == null && points.isEmpty()) {
            block()?.let { (newQ, lat, lon) ->
                q = newQ
                points.add(Point(srs, lat, lon))
            }
        }
    }

    fun setZIfEmpty(block: () -> Double?) {
        if (z == null) {
            z = block()
        }
    }

    fun setUriStringIfEmpty(block: () -> String?) {
        if (uriString == null && defaultPoint == null && points.isEmpty()) {
            uriString = block()
        }
    }
}
