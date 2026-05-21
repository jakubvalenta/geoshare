package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
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
