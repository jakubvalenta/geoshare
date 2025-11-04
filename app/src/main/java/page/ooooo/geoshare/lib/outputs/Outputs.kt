package page.ooooo.geoshare.lib.outputs

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

    fun getSupportingText(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String =
        default.getSupportingText(position, uriQuote)

    fun getActions(
        position: Position,
        packageNames: List<String>,
        uriQuote: UriQuote = DefaultUriQuote(),
    ): List<Output.Item<Action>> =
        all.flatMap { it.getActions(position, packageNames, uriQuote) }

    fun getActions(point: Point, uriQuote: UriQuote = DefaultUriQuote()): List<Output.Item<Action>> =
        all.flatMap { it.getActions(point, uriQuote) }

    fun getAutomations(packageNames: List<String>): List<Automation> = all.flatMap { it.getAutomations(packageNames) }

    fun findAutomation(type: Automation.Type, packageName: String?): Automation? =
        all.firstNotNullOfOrNull { it.findAutomation(type, packageName) }

    fun getChips(
        position: Position,
        uriQuote: UriQuote = DefaultUriQuote(),
    ): List<Output.Item<Action>> =
        all.flatMap { it.getChips(position, uriQuote) }

    fun genRandomUriString(uriQuote: UriQuote = DefaultUriQuote()): String? =
        all.randomOrNull()?.getActions(Position.genRandomPosition(), emptyList(), uriQuote)
            ?.firstNotNullOfOrNull { (it.action as? Action.Copy)?.text }
}
