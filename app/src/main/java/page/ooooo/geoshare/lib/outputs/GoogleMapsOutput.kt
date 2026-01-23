package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.inputs.GoogleMapsInput
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.ui.components.AppIcon
import page.ooooo.geoshare.ui.components.TextIcon

/**
 * See https://developers.google.com/maps/documentation/urls/get-started
 */
object GoogleMapsOutput : Output {

    open class CopyLinkAction : CopyAction() {
        override fun getText(position: Position, i: Int?, uriQuote: UriQuote) =
            formatDisplayUriString(position, i, uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link, GoogleMapsInput.NAME))
        }

        override fun getIcon() = @Composable {
            TextIcon("G")
        }
    }

    open class CopyNavigateToAction : CopyAction() {
        override fun getText(position: Position, i: Int?, uriQuote: UriQuote) =
            formatNavigateToUriString(position, i, uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link_drive_to, GoogleMapsInput.NAME))
        }
    }

    open class CopyStreetViewAction : CopyAction() {
        override fun getText(position: Position, i: Int?, uriQuote: UriQuote) =
            formatStreetViewUriString(position, i, uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link_street_view, GoogleMapsInput.NAME))
        }

        override fun isEnabled(position: Position, i: Int?) = position.getPoint(i) != null
    }

    object CopyLinkChipAction : CopyLinkAction() {
        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_google_maps))
        }
    }

    data class ShareNavigateToWithAppAction(override val packageName: String) : OpenAppAction(packageName) {
        override fun getUriString(position: Position, i: Int?, uriQuote: UriQuote) =
            formatNavigateToUriString(position, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app_navigate_to,
                    AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label
                        ?: packageName,
                ),
            )
        }
    }

    data class ShareStreetViewWithAppAction(override val packageName: String) : OpenAppAction(packageName) {
        override fun getUriString(position: Position, i: Int?, uriQuote: UriQuote) =
            formatStreetViewUriString(position, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app_street_view,
                    AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label
                        ?: packageName,
                ),
            )
        }

        override fun isEnabled(position: Position, i: Int?) = position.getPoint(i) != null
    }

    object CopyLinkAutomation : CopyLinkAction(), BasicAutomation {
        override val type = Automation.Type.COPY_GOOGLE_MAPS_URI
        override val packageName = ""
        override val testTag = null

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    object CopyNavigateToAutomation : CopyNavigateToAction(), BasicAutomation {
        override val type = Automation.Type.COPY_GOOGLE_MAPS_NAVIGATE_TO_URI
        override val packageName = ""
        override val testTag = null

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    object CopyStreetViewAutomation : CopyStreetViewAction(), BasicAutomation {
        override val type = Automation.Type.COPY_GOOGLE_MAPS_STREET_VIEW_URI
        override val packageName = ""
        override val testTag = null

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    data class ShareNavigateToWithAppAutomation(override val packageName: String) :
        OpenAppAction(packageName),
        Action.HasSuccessMessage,
        Action.HasErrorMessage,
        BasicAutomation,
        Automation.HasDelay {

        override val type = Automation.Type.OPEN_APP_GOOGLE_MAPS_NAVIGATE_TO
        override val testTag = null

        override fun getUriString(position: Position, i: Int?, uriQuote: UriQuote) =
            formatNavigateToUriString(position, i, uriQuote)

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
        override fun waitingText(counterSec: Int) = stringResource(
            R.string.conversion_automation_open_app_waiting,
            AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label ?: packageName,
            counterSec,
        )
    }

    data class ShareStreetViewWithAppAutomation(override val packageName: String) :
        OpenAppAction(packageName),
        Action.HasSuccessMessage,
        Action.HasErrorMessage,
        BasicAutomation,
        Automation.HasDelay {

        override val type = Automation.Type.OPEN_APP_GOOGLE_MAPS_STREET_VIEW
        override val testTag = null

        override fun getUriString(position: Position, i: Int?, uriQuote: UriQuote) =
            formatStreetViewUriString(position, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app_street_view,
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
        override fun waitingText(counterSec: Int) = stringResource(
            R.string.conversion_automation_open_app_waiting,
            AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label ?: packageName,
            counterSec,
        )
    }

    object ShareNavigateToAction : OpenChooserAction() {
        override fun getUriString(position: Position, i: Int?, uriQuote: UriQuote) =
            formatNavigateToUriString(position, i, uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_open_app_navigate_to, GoogleMapsInput.NAME))
        }
    }

    object ShareStreetViewAction : OpenChooserAction() {
        override fun getUriString(position: Position, i: Int?, uriQuote: UriQuote) =
            formatStreetViewUriString(position, i, uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_open_app_street_view, GoogleMapsInput.NAME))
        }
    }

    override fun getPositionActions() = listOf(
        CopyLinkAction(),
        CopyNavigateToAction(),
        CopyStreetViewAction(),
    )

    override fun getPointActions(): List<BasicAction> = listOf(
        CopyLinkAction(),
        CopyNavigateToAction(),
        CopyStreetViewAction(),
        ShareNavigateToAction,
        ShareStreetViewAction,
    )

    override fun getAppActions(apps: List<AndroidTools.App>): List<Pair<String, Action>> = buildList {
        apps.filter { it.packageName in PackageNames.GOOGLE_MAPS_LIKE && it.packageName != PackageNames.GMAPS_WV }
            .forEach { app ->
                add(app.packageName to ShareNavigateToWithAppAction(app.packageName))
                add(app.packageName to ShareStreetViewWithAppAction(app.packageName))
            }
    }

    override fun getChipActions() = listOf(CopyLinkChipAction)

    override fun getRandomAction() = listOf(CopyLinkAction(), CopyNavigateToAction()).randomOrNull()

    override fun getAutomations(apps: List<AndroidTools.App>): List<Automation> = buildList {
        add(CopyLinkAutomation)
        add(CopyNavigateToAutomation)
        add(CopyStreetViewAutomation)
        apps.filter { it.packageName in PackageNames.GOOGLE_MAPS_LIKE && it.packageName != PackageNames.GMAPS_WV }
            .forEach { app ->
                add(ShareNavigateToWithAppAutomation(app.packageName))
                add(ShareStreetViewWithAppAutomation(app.packageName))
            }
    }

    override fun findAutomation(type: Automation.Type, packageName: String?): Automation? = when (type) {
        Automation.Type.COPY_GOOGLE_MAPS_URI ->
            CopyLinkAutomation

        Automation.Type.COPY_GOOGLE_MAPS_NAVIGATE_TO_URI ->
            CopyNavigateToAutomation

        Automation.Type.COPY_GOOGLE_MAPS_STREET_VIEW_URI ->
            CopyStreetViewAutomation

        Automation.Type.OPEN_APP_GOOGLE_MAPS_NAVIGATE_TO if packageName != null ->
            ShareNavigateToWithAppAutomation(packageName)

        Automation.Type.OPEN_APP_GOOGLE_MAPS_STREET_VIEW if packageName != null ->
            ShareStreetViewWithAppAutomation(packageName)

        else -> null
    }

    private fun formatDisplayUriString(position: Position, i: Int?, uriQuote: UriQuote): String = Uri(
        scheme = "https",
        host = "www.google.com",
        path = "/maps",
        queryParams = buildMap {
            position.getPoint(i)
                ?.toStringPair(Srs.GCJ02)
                ?.let { (latStr, lonStr) ->
                    set("q", "$latStr,$lonStr")
                } ?: position.q?.let { q ->
                set("q", q)
            }
            position.zStr?.let { zStr ->
                set("z", zStr)
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()

    private fun formatNavigateToUriString(position: Position, i: Int?, uriQuote: UriQuote): String = Uri(
        scheme = "google.navigation",
        path = (position.getPoint(i)
            ?.toStringPair(Srs.GCJ02)
            ?.let { (latStr, lonStr) -> "$latStr,$lonStr" }
            ?: position.q
            ?: "0,0")
            .let { q ->
                "q=$q"
            },
        uriQuote = uriQuote,
    ).toString()

    private fun formatStreetViewUriString(position: Position, i: Int?, uriQuote: UriQuote): String = Uri(
        scheme = "google.streetview",
        path = (position.getPoint(i)
            ?.toStringPair(Srs.GCJ02)
            ?.let { (latStr, lonStr) -> "$latStr,$lonStr" }
            ?: "0,0"
            ).let { coords ->
                @Suppress("SpellCheckingInspection")
                "cbll=$coords"
            },
        uriQuote = uriQuote,
    ).toString()
}
