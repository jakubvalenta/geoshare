package page.ooooo.geoshare.lib.outputs

import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.converters.MagicEarthUrlConverter

object MagicEarthOutput : Output {
    @Suppress("SpellCheckingInspection")
    override val packageNames = listOf("com.generalmagic.magicearth")

    override fun getPositionText(position: Position, uriQuote: UriQuote) =
        Output.Item(formatDisplayUriString(position, uriQuote)) {
            stringResource(R.string.conversion_succeeded_copy_link_display, MagicEarthUrlConverter.NAME)
        }

    override fun getPositionExtraTexts(position: Position, uriQuote: UriQuote) = emptyList<Output.Item>()

    override fun getPositionChipTexts(position: Position, uriQuote: UriQuote) = emptyList<Output.Item>()

    override fun getPositionUriString(position: Position, uriQuote: UriQuote) =
        Output.Item(formatDisplayUriString(position, uriQuote)) {
            stringResource(R.string.conversion_succeeded_open_app_display, MagicEarthUrlConverter.NAME)
        }

    override fun getPositionExtraUriStrings(position: Position, uriQuote: UriQuote) = listOf(
        Output.Item(formatDriveToUriString(position.mainPoint ?: Point(), uriQuote)) {
            stringResource(R.string.conversion_succeeded_open_app_drive_to, MagicEarthUrlConverter.NAME)
        },
        Output.Item(formatDriveViaUriString(position.mainPoint ?: Point(), uriQuote)) {
            stringResource(R.string.conversion_succeeded_open_app_drive_via, MagicEarthUrlConverter.NAME)
        },
    )

    override fun getPointText(point: Point, uriQuote: UriQuote) = null

    override fun getPointExtraTexts(point: Point, uriQuote: UriQuote) = listOf(
        Output.Item(formatDriveToUriString(point, uriQuote)) {
            stringResource(R.string.conversion_succeeded_copy_link_drive_to, MagicEarthUrlConverter.NAME)
        },
        Output.Item(formatDriveViaUriString(point, uriQuote)) {
            stringResource(R.string.conversion_succeeded_copy_link_drive_via, MagicEarthUrlConverter.NAME)
        },
    )

    override fun getPointUriStrings(point: Point, uriQuote: UriQuote) = listOf(
        Output.Item(formatDriveToUriString(point, uriQuote)) {
            stringResource(R.string.conversion_succeeded_open_app_drive_to, MagicEarthUrlConverter.NAME)
        },
        Output.Item(formatDriveViaUriString(point, uriQuote)) {
            stringResource(R.string.conversion_succeeded_open_app_drive_via, MagicEarthUrlConverter.NAME)
        },
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
