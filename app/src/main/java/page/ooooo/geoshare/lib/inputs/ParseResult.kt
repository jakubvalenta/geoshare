package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.point.Point

data class ParseUriResult(
    val points: ImmutableList<Point> = persistentListOf(),
    val htmlUriString: String? = null,
    val webUriString: String? = null,
) {
    class Builder {
        var points: ImmutableList<Point> = persistentListOf()
        var htmlUriString: String? = null
        var webUriString: String? = null

        fun build(): ParseUriResult = ParseUriResult(
            points = points,
            htmlUriString = htmlUriString,
            webUriString = webUriString,
        )
    }
}

data class ParseHtmlResult(
    val points: ImmutableList<Point> = persistentListOf(),
    val redirectUriString: String? = null,
    val webUriString: String? = null,
) {
    class Builder {
        var points: ImmutableList<Point> = persistentListOf()
        var redirectUriString: String? = null
        var webUriString: String? = null

        fun build(): ParseHtmlResult = ParseHtmlResult(
            points = points,
            redirectUriString = redirectUriString,
            webUriString = webUriString,
        )
    }
}

suspend fun buildParseUriResult(block: suspend ParseUriResult.Builder.() -> Unit): ParseUriResult =
    ParseUriResult.Builder().apply { this.block() }.build()

suspend fun buildParseHtmlResult(block: suspend ParseHtmlResult.Builder.() -> Unit): ParseHtmlResult =
    ParseHtmlResult.Builder().apply { this.block() }.build()
