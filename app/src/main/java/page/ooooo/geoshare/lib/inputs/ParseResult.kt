package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.geo.Points

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
// TODO Test
fun List<ParseResult>.merge(): ParseResult {
    // Find points in latest result or previous results
    val latestResult = first()
    val lastPoint = latestResult.points.lastOrNull()
    // Find zoom and name in previous results
    val z = lastPoint?.z ?: drop(1).firstNotNullOfOrNull { parseResult -> parseResult.points.lastOrNull()?.z }
    val name = lastPoint?.name ?: drop(1).firstNotNullOfOrNull { parseResult -> parseResult.points.lastOrNull()?.name }
    return if (
        (z != null && z != lastPoint?.z) ||
        (name != null && name != lastPoint?.name)
    ) {
        if (lastPoint != null) {
            // Copy zoom and name from previous results to latest result
            latestResult.copy(
                points = latestResult.points
                    .take(latestResult.points.size - 1)
                    .plus(lastPoint.copy(z = z, name = name))
                    .toImmutableList(),
            )
        } else {
            // Create new point with zoom and/or name from previous results
            val templatePoint = firstNotNullOf { it.points.lastOrNull() }
            latestResult.copy(
                points = persistentListOf(
                    templatePoint.copy(z = z, name = name),
                ),
            )
        }
    } else {
        latestResult
    }
}
