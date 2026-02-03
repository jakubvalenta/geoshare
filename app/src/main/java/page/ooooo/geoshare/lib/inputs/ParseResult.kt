package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.point.Point

sealed interface ParseUriResult {

    class Builder {
        var points: ImmutableList<Point> = persistentListOf()
        var htmlUriString: String? = null
        var webUriString: String? = null

        fun build(): ParseUriResult =
            points.takeIf { it.lastOrNull()?.hasCoordinates() == true }?.let {
                Succeeded(points)
            } ?: htmlUriString?.let { htmlUriString ->
                SucceededAndSupportsHtmlParsing(points, htmlUriString)
            } ?: webUriString?.let { webUriString ->
                SucceededAndSupportsWebParsing(points, webUriString)
            } ?: points.takeIf { it.lastOrNull()?.hasName() == true }?.let {
                Succeeded(points)
            } ?: Failed()
    }

    data class Succeeded(val points: ImmutableList<Point>) : ParseUriResult

    data class SucceededAndSupportsHtmlParsing(val points: ImmutableList<Point>, val htmlUriString: String) :
        ParseUriResult

    data class SucceededAndSupportsWebParsing(val points: ImmutableList<Point>, val webUrlString: String) :
        ParseUriResult

    class Failed : ParseUriResult
}

sealed interface ParseHtmlResult {

    class Builder {
        lateinit var points: ImmutableList<Point>
        var redirectUriString: String? = null
        var webUriString: String? = null

        fun build(): ParseHtmlResult =
            points.takeIf { it.lastOrNull()?.hasCoordinates() == true }?.let {
                Succeeded(points)
            } ?: redirectUriString?.let { redirectUriString ->
                RequiresRedirect(redirectUriString)
            } ?: webUriString?.let { webUriString ->
                RequiresWebParsing(webUriString)
            } ?: points.takeIf { it.lastOrNull()?.hasName() == true }?.let {
                Succeeded(points)
            } ?: Failed()
    }

    data class Succeeded(val points: ImmutableList<Point>) : ParseHtmlResult

    data class RequiresRedirect(val redirectUriString: String) : ParseHtmlResult

    data class RequiresWebParsing(val webUrlString: String) : ParseHtmlResult

    class Failed : ParseHtmlResult
}

suspend fun buildParseUriResult(block: suspend ParseUriResult.Builder.() -> Unit): ParseUriResult =
    ParseUriResult.Builder().apply { this.block() }.build()

suspend fun buildParseHtmlResult(block: suspend ParseHtmlResult.Builder.() -> Unit): ParseHtmlResult =
    ParseHtmlResult.Builder().apply { this.block() }.build()
