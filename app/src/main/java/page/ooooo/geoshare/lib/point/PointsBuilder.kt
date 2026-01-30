package page.ooooo.geoshare.lib.point

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.max
import kotlin.math.min

class PointsBuilder() {
    var points: MutableList<NaivePoint> = mutableListOf()

    /**
     * Default z will be used as the z of the last point in [points], unless the point already has a z
     */
    var defaultZ: Double? = null
        set(value) {
            field = value?.let { max(1.0, min(21.0, it)) }
        }

    /**
     * Default name will be used as the name of the last point in [points], unless the point already has a z
     */
    var defaultName: String? = null

    fun build(): ImmutableList<NaivePoint> =
        // Take points and set defaults on the last point
        points.takeIf { it.isNotEmpty() }?.let { points ->
            points.lastOrNull()?.let { lastPoint ->
                lastPoint.setDefaults(defaultZ, defaultName)
                    .takeIf { newLastPoint -> newLastPoint != lastPoint }
                    ?.let { newLastPoint ->
                        points.dropLast(1) + newLastPoint
                    }
            } ?: points
        }?.toImmutableList()
        // Or create an empty point with defaults
            ?: run {
                if (defaultZ != null || !defaultName.isNullOrEmpty()) {
                    persistentListOf(NaivePoint(z = defaultZ, name = defaultName))
                } else {
                    null
                }
            }
            // Or return empty list
            ?: persistentListOf()
}

suspend fun buildPoints(block: suspend PointsBuilder.() -> Unit): ImmutableList<NaivePoint> =
    PointsBuilder().apply { this.block() }.build()
