package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.transformLast

sealed interface ParseUriResult {
    companion object {
        fun from(
            points: ImmutableList<Point>,
            htmlUriString: String? = null,
        ): ParseUriResult? = if (points.lastOrNull()?.run { lat != null && lon != null } == true) {
            Succeeded(points)
        } else if (htmlUriString != null) {
            SucceededAndSupportsHtmlParsing(points, htmlUriString)
        } else if (!points.lastOrNull()?.name.isNullOrEmpty()) {
            Succeeded(points)
        } else {
            null
        }
    }

    data class Succeeded(val points: ImmutableList<Point>) : ParseUriResult
    data class SucceededAndSupportsHtmlParsing(val points: ImmutableList<Point>, val htmlUriString: String) :
        ParseUriResult
}

sealed interface ParseHtmlResult {
    companion object {
        fun from(
            pointsFromUri: ImmutableList<Point>,
            pointsFromHtml: ImmutableList<Point>,
            redirectUriString: String? = null,
        ): ParseHtmlResult? {
            // Copy search query parsed from URI to the points parsed from HTML
            val pointsMerged = if (pointsFromHtml.isNotEmpty()) {
                val nameFromUri = pointsFromUri.lastOrNull()?.name
                if (!nameFromUri.isNullOrEmpty()) {
                    pointsFromHtml.transformLast { lastPoint ->
                        if (lastPoint.name.isNullOrEmpty()) {
                            // TODO Test that URI name is used on when HTML name is not empty
                            lastPoint.setName(nameFromUri)
                        } else {
                            lastPoint
                        }
                    }.toImmutableList()
                } else {
                    pointsFromHtml
                }
            } else {
                pointsFromHtml
            }
            return if (pointsMerged.lastOrNull()?.run { lat != null && lon != null } == true) {
                Succeeded(pointsMerged)
            } else if (redirectUriString != null) {
                RequiresRedirect(redirectUriString)
            } else if (!pointsMerged.lastOrNull()?.name.isNullOrEmpty()) {
                Succeeded(pointsMerged)
            } else {
                null
            }
        }
    }

    data class Succeeded(val points: ImmutableList<Point>) : ParseHtmlResult
    data class RequiresRedirect(val redirectUriString: String) : ParseHtmlResult
}
