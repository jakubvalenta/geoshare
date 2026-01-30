package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.lib.point.Point

sealed interface ParseUriResult {
    data class Succeeded(val points: ImmutableList<Point>) : ParseUriResult
    data class SucceededAndSupportsHtmlParsing(val points: ImmutableList<Point>, val htmlUriString: String) :
        ParseUriResult
}

sealed interface ParseHtmlResult {
    data class Succeeded(val points: ImmutableList<Point>) : ParseHtmlResult
    data class RequiresRedirect(val redirectUriString: String) : ParseHtmlResult
}
