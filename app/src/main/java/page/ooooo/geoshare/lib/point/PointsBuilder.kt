package page.ooooo.geoshare.lib.point

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.max
import kotlin.math.min

class PointsBuilder() {
    private var points: MutableList<NaivePoint> = mutableListOf()
    private var defaultPoint: NaivePoint? = null  // Used only if points are empty
    private var q: String? = null // TODO Rename to defaultName
    private var z: Double? = null // TODO Rename to defaultZ
    private var name: String? = null // TODO Replace with defaultName

    fun toPoints(): ImmutableList<NaivePoint> =
        (points.takeIf { it.isNotEmpty() } ?: defaultPoint?.let { mutableListOf(it) } ?: emptyList()).run {
            // Set z and name on the last point
            if (z != null || name != null) {
                transformLast { lastPoint ->
                    lastPoint.copy(
                        z = lastPoint.z ?: z?.let { max(1.0, min(21.0, it)) },
                        name = lastPoint.name ?: name,
                    )
                }
            } else {
                this
            }
        }.toImmutableList()

    fun hasPoint(): Boolean = defaultPoint != null || points.isNotEmpty()

    fun setPointIfNull(block: () -> NaivePoint?): Boolean = if (points.isEmpty()) {
        block()?.let { (lat, lon, newZ, name) ->
            points.add(NaivePoint(lat, lon, name = name))
            if (newZ != null) {
                z = newZ
            }
            true
        } ?: false
    } else {
        false
    }

    fun setDefaultPointIfNull(block: () -> NaivePoint?): Boolean = if (defaultPoint == null) {
        block()?.let { (lat, lon, newZ, name) ->
            defaultPoint = NaivePoint(lat, lon, name = name)
            if (newZ != null) {
                z = newZ
            }
            true
        } ?: false
    } else {
        false
    }

    fun addPoints(block: () -> Sequence<NaivePoint>): Boolean =
        points.addAll(block().map { (lat, lon, _, name) -> NaivePoint(lat, lon, name = name) })

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
                points.add(NaivePoint(lat, lon))
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

suspend fun buildPoints(block: suspend PointsBuilder.() -> Unit): ImmutableList<NaivePoint> =
    PointsBuilder().apply { this.block() }.toPoints()
