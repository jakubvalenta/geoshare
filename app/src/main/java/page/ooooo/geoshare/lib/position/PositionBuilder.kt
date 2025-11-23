package page.ooooo.geoshare.lib.position

import kotlinx.collections.immutable.toImmutableList
import kotlin.math.max
import kotlin.math.min

class PositionBuilder(val srs: Srs) {

    private var points: MutableList<Point> = mutableListOf()
    private var defaultPoint: Point? = null  // Used only if points are empty
    private var q: String? = null
    private var z: Double? = null
    private var name: String? = null
    private var uriString: String? = null

    val position: Position
        get() = Position(
            (points.takeIf { it.isNotEmpty() } ?: defaultPoint?.let { mutableListOf(it) })?.apply {
                // Set name on the last point
                removeLastOrNull()?.copy(name = name)?.let { add(it) }
            }?.toImmutableList(),
            q = q,
            z = z?.let { max(1.0, min(21.0, it)) },
        )

    fun toPair(): Pair<Position, String?> = position to uriString

    fun setPointIfNull(block: () -> LatLonZ?) {
        if (points.isEmpty()) {
            block()?.let { (lat, lon, newZ) ->
                points.add(Point(srs, lat, lon))
                if (newZ != null) {
                    z = newZ
                }
            }
        }
    }

    fun setDefaultPointIfNull(block: () -> LatLonZ?) {
        if (defaultPoint == null) {
            block()?.let { (lat, lon, newZ) ->
                defaultPoint = Point(srs, lat, lon)
                if (newZ != null) {
                    z = newZ
                }
            }
        }
    }

    fun addPoints(block: () -> Sequence<LatLonZ>) {
        points.addAll(block().map { (lat, lon) -> Point(srs, lat, lon) })
    }

    fun setQIfNull(block: () -> String?) {
        if (q == null && defaultPoint == null && points.isEmpty()) {
            q = block()
        }
    }

    fun setQOrNameIfEmpty(block: () -> String?) {
        if (q == null && defaultPoint == null && points.isEmpty()) {
            q = block()
        } else if (name == null) {
            name = block()
        }
    }

    fun setQWithCenterIfNull(block: () -> Triple<String, Double, Double>?) {
        if (q == null) {
            block()?.let { (newQ, lat, lon) ->
                if (defaultPoint == null && points.isEmpty()) {
                    q = newQ
                    points.add(Point(srs, lat, lon))
                } else {
                    name = newQ
                }
            }
        }
    }

    fun setZIfNull(block: () -> Double?) {
        if (z == null) {
            z = block()
        }
    }

    fun setUriStringIfNull(block: () -> String?) {
        if (uriString == null && defaultPoint == null && points.isEmpty()) {
            uriString = block()
        }
    }
}
