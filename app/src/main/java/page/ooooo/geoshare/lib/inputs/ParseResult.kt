package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.point.Point

data class ParseUriResult(
    val points: ImmutableList<Point> = persistentListOf(),
    val htmlUriString: String? = null,
    val webUriString: String? = null,
)

class ParseUriResultScope {
    var points: ImmutableList<Point> = persistentListOf()
    var htmlUriString: String? = null
    var webUriString: String? = null

    internal fun build() = ParseUriResult(points, htmlUriString, webUriString)
}

data class ParseHtmlResult(
    val points: ImmutableList<Point> = persistentListOf(),
    val redirectUriString: String? = null,
    val webUriString: String? = null,
)

class ParseHtmlResultScope {
    var points: ImmutableList<Point> = persistentListOf()
    var redirectUriString: String? = null
    var webUriString: String? = null

    internal fun build() = ParseHtmlResult(points, redirectUriString, webUriString)
}

suspend fun buildParseUriResult(block: suspend ParseUriResultScope.() -> Unit): ParseUriResult =
    ParseUriResultScope().apply { this.block() }.build()

suspend fun buildParseHtmlResult(block: suspend ParseHtmlResultScope.() -> Unit): ParseHtmlResult =
    ParseHtmlResultScope().apply { this.block() }.build()
