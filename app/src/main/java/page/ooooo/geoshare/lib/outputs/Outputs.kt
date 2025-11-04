package page.ooooo.geoshare.lib.outputs

import android.content.Context
import page.ooooo.geoshare.lib.Automation
import page.ooooo.geoshare.lib.*

object Outputs {
    private val default = CoordinatesOutput
    private val all = listOf(
        default,
        GeoUriOutput,
        GoogleMapsOutput,
        AppleMapsOutput,
        MagicEarthOutput,
        GpxOutput,
    )

    fun getText(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String =
        default.getText(position, uriQuote)

    fun getText(point: Point, uriQuote: UriQuote = DefaultUriQuote()): String =
        default.getText(point, uriQuote)

    fun getActions(
        position: Position,
        uriQuote: UriQuote = DefaultUriQuote(),
    ): List<Output.Item<Action>> =
        all.flatMap { it.getActions(position, uriQuote) }

    fun getActions(point: Point, uriQuote: UriQuote = DefaultUriQuote()): List<Output.Item<Action>> =
        all.flatMap { it.getActions(point, uriQuote) }

    fun getAutomations(context: Context): List<Automation> = all.flatMap { it.getAutomations(context) }

    fun findAutomation(type: Automation.Type, packageName: String?): Automation? =
        all.firstNotNullOfOrNull { it.findAutomation(type, packageName) }

    fun getChips(
        position: Position,
        uriQuote: UriQuote = DefaultUriQuote(),
    ): List<Output.Item<Action>> =
        all.flatMap { it.getChips(position, uriQuote) }

    fun genRandomUriString(uriQuote: UriQuote = DefaultUriQuote()): String? =
        all.randomOrNull()?.getActions(Position.genRandomPosition(), uriQuote)
            ?.firstNotNullOfOrNull { (it.action as? Action.Copy)?.text }
}
