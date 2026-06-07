package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.WGS84Point

data class ParseResult(
    val points: Points = persistentListOf(),
    val next: MatchedInput<*>? = null,
)

class ParseResultScope {
    var points: Points = persistentListOf()
    var next: MatchedInput<*>? = null

    internal fun build() = ParseResult(points, next)
}

suspend fun parseResult(block: suspend ParseResultScope.() -> Unit): ParseResult =
    ParseResultScope().apply { this.block() }.build()

/**
 * Returns the newest result from a list sorted from newest to oldest. If the newest result doesn't have a zoom or name,
 * it tries to find a zoom and name in older results, and copies them to the newest result.
 */
fun List<ParseResult>.merge(): ParseResult {
    // Find points in the newest result or older results
    val newestResult = firstOrNull() ?: return ParseResult()
    if (size <= 1) return newestResult
    val newestPoint = newestResult.points.lastOrNull()
    val anyPoint = newestPoint ?: firstNotNullOfOrNull { it.points.firstOrNull() } ?: return newestResult

    // Find zoom and name in older results
    val z = newestPoint?.z ?: drop(1).firstNotNullOfOrNull { it.points.lastOrNull()?.z }
    val name = newestPoint?.name ?: drop(1).firstNotNullOfOrNull { it.points.lastOrNull()?.name }
    return if (
        (z != null && z != newestPoint?.z) ||
        (name != null && name != newestPoint?.name)
    ) {
        if (newestPoint != null) {
            // Copy zoom and name from older results to the newest result
            newestResult.copy(
                points = newestResult.points
                    .take(newestResult.points.size - 1)
                    .plus(newestPoint.copy(z = z, name = name))
                    .toImmutableList(),
            )
        } else {
            // Create new point with zoom and/or name from older results
            newestResult.copy(
                points = persistentListOf(
                    WGS84Point(z = z, name = name, source = anyPoint.source),
                ),
            )
        }
    } else {
        newestResult
    }
}
