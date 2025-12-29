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

    fun setPointIfNull(block: () -> LatLonZ?): Boolean =
        if (points.isEmpty()) {
            block()?.let { (lat, lon, newZ) ->
                points.add(Point(srs, lat, lon))
                if (newZ != null) {
                    z = newZ
                }
                true
            } ?: false
        } else {
            false
        }

    fun setDefaultPointIfNull(block: () -> LatLonZ?): Boolean =
        if (defaultPoint == null) {
            block()?.let { (lat, lon, newZ) ->
                defaultPoint = Point(srs, lat, lon)
                if (newZ != null) {
                    z = newZ
                }
                true
            } ?: false
        } else {
            false
        }

    fun addPoints(block: () -> Sequence<LatLonZ>): Boolean =
        points.addAll(block().map { (lat, lon) -> Point(srs, lat, lon) })

    fun setQIfNull(block: () -> String?): Boolean =
        if (q == null && defaultPoint == null && points.isEmpty()) {
            block()?.let { newQ ->
                q = newQ
                true
            } ?: false
        } else {
            false
        }

    fun setQOrNameIfEmpty(block: () -> String?): Boolean =
        if (q == null && defaultPoint == null && points.isEmpty()) {
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

    fun setQWithCenterIfNull(block: () -> Triple<String, Double, Double>?): Boolean =
        if (q == null) {
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

    fun setZIfNull(block: () -> Double?): Boolean =
        if (z == null) {
            block()?.let { newZ ->
                z = newZ
                true
            } ?: false
        } else {
            false
        }

    fun setUriStringIfNull(block: () -> String?): Boolean =
        if (uriString == null && defaultPoint == null && points.isEmpty()) {
            block()?.let { newUriString ->
                uriString = newUriString
                true
            } ?: false
        } else {
            false
        }
}
