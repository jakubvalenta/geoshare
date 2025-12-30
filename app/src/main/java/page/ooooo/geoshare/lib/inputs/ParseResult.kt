package page.ooooo.geoshare.lib.inputs

import page.ooooo.geoshare.lib.position.Position

sealed interface ParseUriResult {
    companion object {
        fun from(
            position: Position,
            htmlUriString: String? = null,
        ): ParseUriResult? = if (!position.points.isNullOrEmpty()) {
            Succeeded(position)
        } else if (htmlUriString != null) {
            SucceededAndSupportsHtmlParsing(position, htmlUriString)
        } else if (!position.q.isNullOrEmpty()) {
            Succeeded(position)
        } else {
            null
        }
    }

    data class Succeeded(val position: Position) : ParseUriResult
    data class SucceededAndSupportsHtmlParsing(val position: Position, val htmlUriString: String) : ParseUriResult
}

sealed interface ParseHtmlResult {
    companion object {
        fun from(
            positionFromUri: Position,
            positionFromHtml: Position,
            redirectUriString: String? = null,
        ): ParseHtmlResult? = if (!positionFromHtml.points.isNullOrEmpty()) {
            val positionMerged = if (positionFromUri.q != null) {
                // Use query from the position parsed from URI as the name of the position parsed from HTML
                positionFromHtml.setLastPointName(positionFromUri.q)
            } else {
                positionFromHtml
            }
            Succeeded(positionMerged)
        } else if (redirectUriString != null) {
            RequiresRedirect(redirectUriString)
        } else {
            null
        }
    }

    data class Succeeded(val position: Position) : ParseHtmlResult
    data class RequiresRedirect(val redirectUriString: String) : ParseHtmlResult
}
