package page.ooooo.geoshare.lib.outputs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.inputs.MagicEarthInput
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.seconds

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/, although it's outdated:
 * - drive_via doesn't work
 * - navigate_to doesn't work; use get_directions
 * - navigate_via doesn't work; it was an undocumented parameter that used to work for a while
 * - search_around seems to do the same as open_search
 */
object MagicEarthOutput : Output {

    open class CopyDisplayUriAction : CopyAction() {
        override fun getText(position: Position, i: Int?, uriQuote: UriQuote) =
            formatDisplayUriString(position, i, uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link_display, MagicEarthInput.NAME))
        }
    }

    open class CopyNavigateToUriAction : CopyAction() {
        override fun getText(position: Position, i: Int?, uriQuote: UriQuote) =
            formatNavigateToUriString(position, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(R.string.conversion_succeeded_copy_link_drive_to, MagicEarthInput.NAME),
                Modifier.testTag("geoShareOutputMagicEarthCopyNavigateToUri"),
            )
        }
    }

    object ShareDisplayUriAction : OpenChooserAction() {
        override fun getUriString(position: Position, i: Int?, uriQuote: UriQuote) =
            formatDisplayUriString(position, i, uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_open_app_display, MagicEarthInput.NAME))
        }
    }

    object ShareNavigateToUriAction : OpenChooserAction() {
        override fun getUriString(position: Position, i: Int?, uriQuote: UriQuote) =
            formatNavigateToUriString(position, i, uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_open_app_navigate_to, MagicEarthInput.NAME))
        }
    }

    open class ShareDisplayUriWithAppAction(override val packageName: String) : OpenAppAction(packageName) {
        override fun getUriString(position: Position, i: Int?, uriQuote: UriQuote) =
            formatDisplayUriString(position, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app_display,
                    queryAppDetails()?.label ?: packageName,
                ),
            )
        }
    }

    open class ShareNavigateToUriWithAppAction(override val packageName: String) : OpenAppAction(packageName) {
        override fun getUriString(position: Position, i: Int?, uriQuote: UriQuote) =
            formatNavigateToUriString(position, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app_navigate_to,
                    queryAppDetails()?.label ?: packageName,
                ),
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
        ShareNavigateToUriWithAppAction(packageName),
        Action.HasSuccessMessage,
        BasicAutomation,
        Automation.HasDelay {

        override val type = Automation.Type.OPEN_APP_MAGIC_EARTH_NAVIGATE_TO
        override val testTag = null
        override val delay = 5.seconds

        @Composable
        override fun Label() {
            val spacing = LocalSpacing.current
            queryAppDetails()?.let { appDetails ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.tiny),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        rememberDrawablePainter(appDetails.icon),
                        appDetails.label,
                        Modifier.widthIn(max = 24.dp),
                    )
                    Text(
                        stringResource(
                            R.string.conversion_succeeded_open_app_navigate_to,
                            appDetails.label
                        )
                    )
                }
            } ?: Text(
                stringResource(
                    R.string.conversion_succeeded_open_app_navigate_to,
                    packageName
                )
            )
        }

        @Composable
        override fun successText() =
            stringResource(
                R.string.conversion_automation_open_app_succeeded,
                queryAppDetails()?.label ?: packageName,
            )

        @Composable
        override fun errorText() =
            stringResource(
                R.string.conversion_automation_open_app_failed,
                queryAppDetails()?.label ?: packageName,
            )

        @Composable
        override fun waitingText(counterSec: Int) =
            stringResource(
                R.string.conversion_automation_open_app_waiting,
                queryAppDetails()?.label ?: packageName,
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
        Automation.Type.COPY_MAGIC_EARTH_URI ->
            CopyDisplayUriAutomation

        Automation.Type.COPY_MAGIC_EARTH_NAVIGATE_TO_URI ->
            CopyNavigateToUriAutomation

        Automation.Type.OPEN_APP_MAGIC_EARTH_NAVIGATE_TO if packageName != null ->
            ShareNavigateToUriWithAppAutomation(packageName)

        else -> null
    }

    private fun formatDisplayUriString(position: Position, i: Int?, uriQuote: UriQuote): String =
        Uri(
            scheme = "magicearth",
            path = "//",
            queryParams = buildMap {
                (position.getPoint(i) ?: Point(Srs.WGS84)).let { point ->
                    point.toStringPair(Srs.WGS84).let { (latStr, lonStr) ->
                        if (position.q == null) {
                            set("show_on_map", "")
                            set("lat", latStr)
                            set("lon", lonStr)
                            point.name?.let { name ->
                                set("name", name)
                            }
                        } else {
                            if (point.lat == 0.0 && point.lon == 0.0) {
                                set("open_search", "")
                            } else {
                                set("search_around", "")
                                set("lat", latStr)
                                set("lon", lonStr)
                            }
                            set("q", position.q)
                        }
                    }
                }
            }.toImmutableMap(),
            uriQuote = uriQuote,
        ).toString()

    private fun formatNavigateToUriString(position: Position, i: Int?, uriQuote: UriQuote): String =
        Uri(
            scheme = "magicearth",
            path = "//",
            queryParams = buildMap {
                set("get_directions", "")
                (position.getPoint(i) ?: Point(Srs.WGS84))
                    .toStringPair(Srs.WGS84).let { (latStr, lonStr) ->
                        set("lat", latStr)
                        set("lon", lonStr)
                    }
            }.toImmutableMap(),
            uriQuote = uriQuote,
        ).toString()
}
