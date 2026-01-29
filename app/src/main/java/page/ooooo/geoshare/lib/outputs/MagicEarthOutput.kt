package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.inputs.MagicEarthInput
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.getOrNull
import page.ooooo.geoshare.ui.components.AppIcon
import page.ooooo.geoshare.ui.components.TextIcon

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/, although it's outdated:
 * - drive_via doesn't work
 * - navigate_to doesn't work; use get_directions
 * - navigate_via doesn't work; it was an undocumented parameter that used to work for a while
 * - search_around seems to do the same as open_search
 */
object MagicEarthOutput : Output {

    open class CopyDisplayUriAction : CopyAction() {
        override fun getText(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatDisplayUriString(points, i, uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link_display, MagicEarthInput.NAME))
        }

        override fun getIcon() = @Composable {
            TextIcon("M")
        }
    }

    open class CopyNavigateToUriAction : CopyAction() {
        override fun getText(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatNavigateToUriString(points, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(R.string.conversion_succeeded_copy_link_drive_to, MagicEarthInput.NAME),
                Modifier.testTag("geoShareOutputMagicEarthCopyNavigateToUri"),
            )
        }
    }

    object ShareDisplayUriAction : OpenChooserAction() {
        override fun getUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatDisplayUriString(points, i, uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_open_app_display, MagicEarthInput.NAME))
        }
    }

    object ShareNavigateToUriAction : OpenChooserAction() {
        override fun getUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatNavigateToUriString(points, i, uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_open_app_navigate_to, MagicEarthInput.NAME))
        }
    }

    open class ShareDisplayUriWithAppAction(override val packageName: String) : OpenAppAction(packageName) {
        override fun getUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatDisplayUriString(points, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app_display,
                    AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label
                        ?: packageName,
                ),
            )
        }
    }

    open class ShareNavigateToUriWithAppAction(override val packageName: String) : OpenAppAction(packageName) {
        override fun getUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatNavigateToUriString(points, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app_navigate_to,
                    AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label
                        ?: packageName,
                )
            )
        }
    }

    object CopyDisplayUriAutomation : CopyDisplayUriAction(), BasicAutomation {
        override val type = Automation.Type.COPY_MAGIC_EARTH_URI
        override val packageName = ""
        override val testTag = null

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    object CopyNavigateToUriAutomation : CopyNavigateToUriAction(), BasicAutomation {
        override val type = Automation.Type.COPY_MAGIC_EARTH_NAVIGATE_TO_URI
        override val packageName = ""
        override val testTag = null

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    data class ShareNavigateToUriWithAppAutomation(override val packageName: String) :
        ShareNavigateToUriWithAppAction(packageName), Action.HasSuccessMessage, BasicAutomation, Automation.HasDelay {

        override val type = Automation.Type.OPEN_APP_MAGIC_EARTH_NAVIGATE_TO
        override val testTag = null

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app_navigate_to,
                    AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label
                        ?: packageName,
                )
            )
        }

        override fun getIcon() = @Composable {
            AppIcon(packageName)
        }

        @Composable
        override fun successText() = stringResource(
            R.string.conversion_automation_open_app_succeeded,
            AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label ?: packageName,
        )

        @Composable
        override fun errorText() = stringResource(
            R.string.conversion_automation_open_app_failed,
            AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label ?: packageName,
        )

        @Composable
        override fun waitingText(counterSec: Int) = pluralStringResource(
            R.plurals.conversion_automation_open_app_waiting,
            counterSec,
            AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label ?: packageName,
            counterSec,
        )
    }

    override fun getPositionActions() = listOf(
        CopyDisplayUriAction(),
        CopyNavigateToUriAction(),
    )

    override fun getPointActions(): List<BasicAction> = listOf(
        CopyDisplayUriAction(),
        CopyNavigateToUriAction(),
        ShareDisplayUriAction,
        ShareNavigateToUriAction,
    )

    override fun getAppActions(apps: List<AndroidTools.App>): List<Pair<String, Action>> = buildList {
        apps.filter { it.type == AndroidTools.AppType.MAGIC_EARTH }.forEach { app ->
            add(app.packageName to ShareDisplayUriWithAppAction(app.packageName))
            add(app.packageName to ShareNavigateToUriWithAppAction(app.packageName))
        }
    }

    override fun getRandomAction() = CopyDisplayUriAction()

    override fun getAutomations(apps: List<AndroidTools.App>): List<Automation> = buildList {
        add(CopyDisplayUriAutomation)
        add(CopyNavigateToUriAutomation)
        apps.filter { it.type == AndroidTools.AppType.MAGIC_EARTH }.forEach { app ->
            add(ShareNavigateToUriWithAppAutomation(app.packageName))
        }
    }

    override fun findAutomation(type: Automation.Type, packageName: String?): Automation? = when (type) {
        Automation.Type.COPY_MAGIC_EARTH_URI -> CopyDisplayUriAutomation

        Automation.Type.COPY_MAGIC_EARTH_NAVIGATE_TO_URI -> CopyNavigateToUriAutomation

        Automation.Type.OPEN_APP_MAGIC_EARTH_NAVIGATE_TO if packageName != null -> ShareNavigateToUriWithAppAutomation(
            packageName
        )

        else -> null
    }

    private fun formatDisplayUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote): String = Uri(
        scheme = "magicearth",
        path = "//",
        queryParams = buildMap {
            points.getOrNull(i)?.toWGS84().run {
                if (this != null && lat != null && lon != null) {
                    set("show_on_map", "")
                    latStr?.let { latStr ->
                        set("lat", latStr)
                    }
                    lonStr?.let { lonStr ->
                        set("lon", lonStr)
                    }
                    name?.let { name ->
                        set("name", name)
                    }
                } else if (this != null && name != null) {
                    set("open_search", "")
                    set("q", name)
                    // TODO Add support for search around
                    // set("search_around", "")
                    // set("lat", latStr)
                    // set("lon", lonStr)
                } else {
                    set("show_on_map", "")
                    set("lat", "0")
                    set("lon", "0")
                }
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()

    private fun formatNavigateToUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote): String = Uri(
        scheme = "magicearth",
        path = "//",
        queryParams = buildMap {
            set("get_directions", "")
            points.getOrNull(i)?.toWGS84().run {
                if (this != null && lat != null && lon != null) {
                    latStr?.let { latStr ->
                        set("lat", latStr)
                    }
                    lonStr?.let { lonStr ->
                        set("lon", lonStr)
                    }
                } else if (this != null && name != null) {
                    set("q", name)
                } else {
                    set("lat", "0")
                    set("lon", "0")
                }
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()
}
