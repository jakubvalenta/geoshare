package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

object Outputs {
    val default = GeneralOutput
    val extra = listOf(
        GoogleMapsOutput,
        AppleMapsOutput,
        MagicEarthOutput,
    )
    val all get() = listOf(default, *extra.toTypedArray())

    fun getPositionAllTexts(position: Position, uriQuote: UriQuote = DefaultUriQuote()): List<String> = buildList {
        all.forEach { output ->
            output.getPositionText(position, uriQuote)?.let { add(it) }
            addAll(output.getPositionExtraTexts(position, uriQuote))
        }
    }

    fun getPointAllTexts(point: Point, uriQuote: UriQuote = DefaultUriQuote()): List<String> = buildList {
        all.forEach { output ->
            output.getPointText(point, uriQuote)?.let { add(it) }
            addAll(output.getPointExtraTexts(point, uriQuote))
        }
    }

    fun getPointUriStrings(point: Point, uriQuote: UriQuote = DefaultUriQuote()): List<String> =
        all.flatMap { it.getPointUriStrings(point, uriQuote) }

    fun getOpenAppUriString(packageName: String, position: Position, uriQuote: UriQuote = DefaultUriQuote()): String =
        extra.find { it.packageNames.contains(packageName) }?.getPositionUriString(position, uriQuote)
            ?: default.getPositionUriString(position, uriQuote)

    fun getOpenAppAllUriStrings(
        packageName: String,
        position: Position,
        uriQuote: UriQuote = DefaultUriQuote(),
    ): List<String> = buildList {
        extra.find { it.packageNames.contains(packageName) }?.let { output ->
            output.getPositionUriString(position, uriQuote)?.let { add(it) }
            addAll(output.getPositionExtraUriStrings(position, uriQuote))
        } ?: add(default.getPositionUriString(position, uriQuote))
    }
}
