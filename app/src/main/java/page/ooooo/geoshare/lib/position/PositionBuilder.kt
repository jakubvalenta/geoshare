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

    fun toPosition(): Position = Position(
        (points.takeIf { it.isNotEmpty() } ?: defaultPoint?.let { mutableListOf(it) })?.apply {
            // Set name on the last point
            if (lastOrNull()?.name == null) {
                removeLastOrNull()?.copy(name = name)?.let { add(it) }
            }
        }?.toImmutableList(),
        q = q,
        z = z?.let { max(1.0, min(21.0, it)) },
    )

    fun hasPoint(): Boolean = defaultPoint != null || points.isNotEmpty()

    fun setPointIfNull(block: () -> LatLonZName?): Boolean = if (points.isEmpty()) {
        block()?.let { (lat, lon, newZ, name) ->
            points.add(Point(srs, lat, lon, name))
            if (newZ != null) {
                z = newZ
            }
            true
        } ?: false
    } else {
        false
    }

    fun setDefaultPointIfNull(block: () -> LatLonZName?): Boolean = if (defaultPoint == null) {
        block()?.let { (lat, lon, newZ, name) ->
            defaultPoint = Point(srs, lat, lon, name)
            if (newZ != null) {
                z = newZ
            }
            true
        } ?: false
    } else {
        false
    }

    fun addPoints(block: () -> Sequence<LatLonZName>): Boolean =
        points.addAll(block().map { (lat, lon, _, name) -> Point(srs, lat, lon, name) })

    fun setQIfNull(block: () -> String?): Boolean = if (q == null && defaultPoint == null && points.isEmpty()) {
        block()?.let { newQ ->
            q = newQ
            true
        } ?: false
    } else {
        false
    }

    fun setQOrNameIfEmpty(block: () -> String?): Boolean = if (q == null && defaultPoint == null && points.isEmpty()) {
        block()?.let { newQ ->
            q = newQ
            true
        } ?: false
    } else if (name == null) {
        block()?.let { newName ->
            name = newName
            true
        } ?: false
    } else {
        false
    }

    fun setQWithCenterIfNull(block: () -> Triple<String, Double, Double>?): Boolean = if (q == null) {
        block()?.let { (newQ, lat, lon) ->
            if (defaultPoint == null && points.isEmpty()) {
                q = newQ
                points.add(Point(srs, lat, lon))
            } else {
                name = newQ
            }
            true
        } ?: false
    } else {
        false
    }

    fun setZIfNull(block: () -> Double?): Boolean = if (z == null) {
        block()?.let { newZ ->
            z = newZ
            true
        } ?: false
    } else {
        false
    }
}

suspend fun buildPosition(srs: Srs, block: suspend PositionBuilder.() -> Unit): Position =
    PositionBuilder(srs).apply { this.block() }.toPosition()
