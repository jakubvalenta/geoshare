package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.geo.Points

data class ParseResult(
    val points: Points = persistentListOf(),
    val nextData: String? = null,
    val nextInput: NewInput? = null,
)

class ParseResultScope {
    var points: Points = persistentListOf()
    var nextData: String? = null
    var nextInput: NewInput? = null

    internal fun build() = ParseResult(points, nextData, nextInput)
}

suspend fun buildParseResult(block: suspend ParseResultScope.() -> Unit): ParseResult =
    ParseResultScope().apply { this.block() }.build()

@Deprecated("Use ParseResult")
data class ParseUriResult(
    val points: Points = persistentListOf(),
    val htmlUriString: String? = null,
    val webUriString: String? = null,
)

@Deprecated("Use ParseResultScope")
class ParseUriResultScope {
    var points: Points = persistentListOf()
    var htmlUriString: String? = null
    var webUriString: String? = null

    internal fun build() = ParseUriResult(points, htmlUriString, webUriString)
}

@Deprecated("Use ParseResult")
data class ParseHtmlResult(
    val points: Points = persistentListOf(),
    val redirectUriString: String? = null,
    val webUriString: String? = null,
)

@Deprecated("Use ParseResultScope")
class ParseHtmlResultScope {
    var points: Points = persistentListOf()
    var redirectUriString: String? = null
    var webUriString: String? = null

    internal fun build() = ParseHtmlResult(points, redirectUriString, webUriString)
}

@Deprecated("Use buildParseResult")
suspend fun buildParseUriResult(block: suspend ParseUriResultScope.() -> Unit): ParseUriResult =
    ParseUriResultScope().apply { this.block() }.build()

@Deprecated("Use buildParseResult")
suspend fun buildParseHtmlResult(block: suspend ParseHtmlResultScope.() -> Unit): ParseHtmlResult =
    ParseHtmlResultScope().apply { this.block() }.build()
