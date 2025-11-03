package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

object Outputs {
    val default = CoordinatesOutput
    val extra = listOf(
        GoogleMapsOutput,
        AppleMapsOutput,
        MagicEarthOutput,
        GpxOutput,
    )
    val all get() = listOf(default, *extra.toTypedArray())

    fun getText(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String =
        default.getText(position, uriQuote)

    fun getText(point: Point, uriQuote: UriQuote = DefaultUriQuote()): String =
        default.getText(point, uriQuote)

    fun getActions(
        position: Position,
        uriQuote: UriQuote = DefaultUriQuote(),
    ): List<Output.LabeledAction<Output.Action>> =
        all.flatMap { it.getActions(position, uriQuote) }

    fun getActions(point: Point, uriQuote: UriQuote = DefaultUriQuote()): List<Output.LabeledAction<Output.Action>> =
        all.flatMap { it.getActions(point, uriQuote) }

    fun getChips(
        position: Position,
        uriQuote: UriQuote = DefaultUriQuote(),
    ): List<Output.LabeledAction<Output.Action>> =
        all.flatMap { it.getChips(position, uriQuote) }

    fun genRandomUriString(uriQuote: UriQuote = DefaultUriQuote()): String? =
        all.randomOrNull()?.getActions(Position.genRandomPosition(), uriQuote)
            ?.firstNotNullOfOrNull { (it.action as? Output.Action.Copy)?.text }
}
