package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.DefaultUriQuote
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

    fun getAllTexts(position: Position, uriQuote: UriQuote = DefaultUriQuote()): List<String> = buildList {
        all.forEach { output ->
            output.getMainText(position, uriQuote)?.let { mainText ->
                add(mainText)
            }
            addAll(output.getExtraTexts(position, uriQuote))
        }
    }

    fun getOpenAppUriString(packageName: String, position: Position, uriQuote: UriQuote = DefaultUriQuote()): String =
        extra.find { it.packageNames.contains(packageName) }?.getMainUriString(position, uriQuote)
            ?: default.getMainUriString(position, uriQuote)
}
