package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.point.Points

data class ParseUriResult(
    val points: Points = persistentListOf(),
    val htmlUriString: String? = null,
    val webUriString: String? = null,
)

class ParseUriResultScope {
    var points: Points = persistentListOf()
    var htmlUriString: String? = null
    var webUriString: String? = null

    internal fun build() = ParseUriResult(points, htmlUriString, webUriString)
}

data class ParseHtmlResult(
    val points: Points = persistentListOf(),
    val redirectUriString: String? = null,
    val webUriString: String? = null,
)

class ParseHtmlResultScope {
    var points: Points = persistentListOf()
    var redirectUriString: String? = null
    var webUriString: String? = null

    internal fun build() = ParseHtmlResult(points, redirectUriString, webUriString)
}

suspend fun buildParseUriResult(block: suspend ParseUriResultScope.() -> Unit): ParseUriResult =
    ParseUriResultScope().apply { this.block() }.build()

suspend fun buildParseHtmlResult(block: suspend ParseHtmlResultScope.() -> Unit): ParseHtmlResult =
    ParseHtmlResultScope().apply { this.block() }.build()
