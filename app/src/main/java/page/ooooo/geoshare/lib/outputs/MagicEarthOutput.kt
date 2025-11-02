package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote

object MagicEarthOutput : Output {
    @Suppress("SpellCheckingInspection")
    override val packageNames = listOf("com.generalmagic.magicearth")

    override fun getPositionText(position: Position, uriQuote: UriQuote) = formatDisplayUriString(position, uriQuote)

    override fun getPositionExtraTexts(position: Position, uriQuote: UriQuote) = emptyList<String>()

    override fun getPositionUriString(position: Position, uriQuote: UriQuote) =
        formatDisplayUriString(position, uriQuote)

    override fun getPositionExtraUriStrings(position: Position, uriQuote: UriQuote) = listOf(
        formatDriveToUriString(position.mainPoint ?: Point(), uriQuote),
        formatDriveViaUriString(position.mainPoint ?: Point(), uriQuote),
    )

    override fun getPointText(point: Point, uriQuote: UriQuote) = null

    override fun getPointExtraTexts(point: Point, uriQuote: UriQuote) = getPointUriStrings(point, uriQuote)

    override fun getPointUriStrings(point: Point, uriQuote: UriQuote) = listOf(
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
                set("drive_to", "")
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
                set("drive_via", "")
                point.run {
                    set("lat", lat)
                    set("lon", lon)
                }
            }.toImmutableMap(),
            uriQuote = uriQuote,
        ).toString()
}
