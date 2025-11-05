package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.MagicEarthUrlConverter

object MagicEarthOutputManager : OutputManager {
    @Suppress("SpellCheckingInspection")
    const val PACKAGE_NAME = "com.generalmagic.magicearth"

    object CopyLinkAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_MAGIC_EARTH_URI
        override val packageName = ""
        override val testTag = null

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(position, uriQuote))

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link, MagicEarthUrlConverter.NAME))
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    object CopyDisplayUriOutput : Output.Action {
        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(position, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_display, MagicEarthUrlConverter.NAME)
    }

    object CopyDriveToUriOutput : Output.Action {
        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatDriveToUriString(position, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_drive_to, MagicEarthUrlConverter.NAME)
    }

    object CopyDriveViaUriOutput : Output.Action {
        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(formatDriveViaUriString(position, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_drive_via, MagicEarthUrlConverter.NAME)
    }

    object OpenAppDisplayUriOutput : Output.AppAction {
        override val packageName = PACKAGE_NAME

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.OpenApp(PACKAGE_NAME, formatDisplayUriString(position, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_display, MagicEarthUrlConverter.NAME)
    }

    object OpenAppDriveToUriOutput : Output.AppAction {
        override val packageName = PACKAGE_NAME

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.OpenApp(PACKAGE_NAME, formatDriveToUriString(position, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_drive_to, MagicEarthUrlConverter.NAME)
    }

    object OpenAppDriveViaUriOutput : Output.AppAction {
        override val packageName = PACKAGE_NAME

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.OpenApp(PACKAGE_NAME, formatDriveToUriString(position, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_drive_via, MagicEarthUrlConverter.NAME)
    }

    object PointCopyDisplayUriOutput : Output.PointAction {
        override fun getAction(point: Point, uriQuote: UriQuote) =
            Action.Copy(formatDisplayUriString(point, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_display, MagicEarthUrlConverter.NAME)
    }

    object PointCopyDriveToUriOutput : Output.PointAction {
        override fun getAction(point: Point, uriQuote: UriQuote) =
            Action.Copy(formatDriveToUriString(point, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_drive_to, MagicEarthUrlConverter.NAME)
    }

    object PointCopyDriveViaUriOutput : Output.PointAction {
        override fun getAction(point: Point, uriQuote: UriQuote) =
            Action.Copy(formatDriveViaUriString(point, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_copy_link_drive_via, MagicEarthUrlConverter.NAME)
    }

    object PointOpenChooserDisplayUriOutput : Output.PointAction {
        override fun getAction(point: Point, uriQuote: UriQuote) =
            Action.OpenChooser(formatDisplayUriString(point, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_display, MagicEarthUrlConverter.NAME)
    }

    object PointOpenChooserDriveToUriOutput : Output.PointAction {
        override fun getAction(point: Point, uriQuote: UriQuote) =
            Action.OpenChooser(formatDriveToUriString(point, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_drive_to, MagicEarthUrlConverter.NAME)
    }

    object PointOpenChooserDriveViaUriOutput : Output.PointAction {
        override fun getAction(point: Point, uriQuote: UriQuote) =
            Action.OpenChooser(formatDriveViaUriString(point, uriQuote))

        @Composable
        override fun label() =
            stringResource(R.string.conversion_succeeded_open_app_drive_via, MagicEarthUrlConverter.NAME)
    }

    override fun getOutputs(packageNames: List<String>) = listOf(
        CopyDisplayUriOutput,
        CopyDriveToUriOutput,
        CopyDriveViaUriOutput,
        OpenAppDisplayUriOutput,
        OpenAppDriveToUriOutput,
        OpenAppDriveViaUriOutput,
        PointCopyDisplayUriOutput,
        PointCopyDriveToUriOutput,
        PointCopyDriveViaUriOutput,
        PointOpenChooserDisplayUriOutput,
        PointOpenChooserDriveToUriOutput,
        PointOpenChooserDriveViaUriOutput,
    )

    override fun getAutomations(packageNames: List<String>): List<Automation> = listOf(
        CopyLinkAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.COPY_MAGIC_EARTH_URI -> CopyLinkAutomation
        else -> null
    }

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

    private fun formatDisplayUriString(point: Point, uriQuote: UriQuote): String = Uri(
        scheme = "magicearth",
        path = "//",
        queryParams = buildMap {
            point.apply {
                set("lat", lat)
                set("lon", lon)
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()

    private fun formatDriveToUriString(position: Position, uriQuote: UriQuote): String =
        formatDriveToUriString(position.mainPoint ?: Point(), uriQuote)

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

    private fun formatDriveViaUriString(position: Position, uriQuote: UriQuote): String =
        formatDriveViaUriString(position.mainPoint ?: Point(), uriQuote)

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
