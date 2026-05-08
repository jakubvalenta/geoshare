package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.geo.Points

data class ParseResult(
    val points: Points = persistentListOf(),
    val nextMatch: String? = null,
    val nextInput: Input<*>? = null,
)

class ParseResultScope {
    var points: Points = persistentListOf()
    var nextMatch: String? = null
    var nextInput: Input<*>? = null

    internal fun build() = ParseResult(points, nextMatch, nextInput)
}

suspend fun buildParseResult(block: suspend ParseResultScope.() -> Unit): ParseResult =
    ParseResultScope().apply { this.block() }.build()
