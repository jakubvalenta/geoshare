package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote

object MagicEarthOutput : Output {
    @Suppress("SpellCheckingInspection")
    override val packageNames = listOf("com.generalmagic.magicearth")

    override fun getMainText(position: Position, uriQuote: UriQuote) = formatDisplayUriString(position, uriQuote)

    override fun getExtraTexts(position: Position, uriQuote: UriQuote) = emptyList<String>()

    override fun getMainUriString(position: Position, uriQuote: UriQuote) = formatDisplayUriString(position, uriQuote)

    override fun getExtraUriStrings(point: Point, uriQuote: UriQuote) = listOf(
        formatDriveToUriString(point, uriQuote),
        formatDriveViaUriString(point, uriQuote),
    )

    private fun formatDisplayUriString(position: Position, uriQuote: UriQuote): String = Uri(
        scheme = "magicearth",
        path = "//",
        queryParams = buildMap {
            position.apply {
                mainPoint?.apply {
                    set("lat", lat)
                    set("lon", lon)
                }
                q?.let { q ->
                    set("q", q)
                }
                z?.let { z ->
                    set("zoom", z)
                }
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()

    private fun formatDriveToUriString(point: Point, uriQuote: UriQuote): String =
        Uri(
            scheme = "magicearth",
            path = "//",
            queryParams = buildMap {
                set("drive_to", "1")
                point.run {
                    set("lat", lat)
                    set("lon", lon)
                }
            }.toImmutableMap(),
            uriQuote = uriQuote,
        ).toString()

    private fun formatDriveViaUriString(point: Point, uriQuote: UriQuote): String =
        Uri(
            scheme = "magicearth",
            path = "//",
            queryParams = buildMap {
                set("drive_via", "1")
                point.run {
                    set("lat", lat)
                    set("lon", lon)
                }
            }.toImmutableMap(),
            uriQuote = uriQuote,
        ).toString()
}
