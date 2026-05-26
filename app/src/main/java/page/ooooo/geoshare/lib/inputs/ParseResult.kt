package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.WGS84Point

data class ParseResult(
    val points: Points = persistentListOf(),
    val nextStep: NextStep? = null,
)

data class NextStep(
    val input: Input,
    val match: String,
)

class ParseResultScope {
    var points: Points = persistentListOf()
    var nextStep: NextStep? = null

    internal fun build() = ParseResult(points, nextStep)
}

suspend fun parseResult(block: suspend ParseResultScope.() -> Unit): ParseResult =
    ParseResultScope().apply { this.block() }.build()

/**
 * Returns the latest result from a list sorted from latest to oldest. If the latest result doesn't have a zoom or name,
 * it tries to find a zoom and name in previous results, and copies them to the latest result.
 */
fun List<ParseResult>.merge(): ParseResult {
    // Find points in latest result or previous results
    val latestResult = firstOrNull() ?: return ParseResult()
    if (size <= 1) return latestResult
    val latestPoint = latestResult.points.lastOrNull()
    val anyPoint = latestPoint ?: firstNotNullOfOrNull { it.points.firstOrNull() } ?: return latestResult

    // Find zoom and name in previous results
    val z = latestPoint?.z ?: drop(1).firstNotNullOfOrNull { it.points.lastOrNull()?.z }
    val name = latestPoint?.name ?: drop(1).firstNotNullOfOrNull { it.points.lastOrNull()?.name }
    return if (
        (z != null && z != latestPoint?.z) ||
        (name != null && name != latestPoint?.name)
    ) {
        if (latestPoint != null) {
            // Copy zoom and name from previous results to latest result
            latestResult.copy(
                points = latestResult.points
                    .take(latestResult.points.size - 1)
                    .plus(latestPoint.copy(z = z, name = name))
                    .toImmutableList(),
            )
        } else {
            // Create new point with zoom and/or name from previous results
            latestResult.copy(
                points = persistentListOf(
                    WGS84Point(z = z, name = name, source = anyPoint.source),
                ),
            )
        }
    } else {
        latestResult
    }
}
