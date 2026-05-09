package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.geo.Points

data class ParseResult(
    val points: Points = persistentListOf(),
    val nextInput: Input<*>? = null,
    val nextMatch: String? = null,
)

class ParseResultScope {
    var points: Points = persistentListOf()
    var nextInput: Input<*>? = null
    var nextMatch: String? = null

    internal fun build() = ParseResult(points, nextInput, nextMatch)
}

suspend fun buildParseResult(block: suspend ParseResultScope.() -> Unit): ParseResult =
    ParseResultScope().apply { this.block() }.build()
