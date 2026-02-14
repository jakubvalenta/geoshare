package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.android.App
import page.ooooo.geoshare.lib.android.GoogleMapsAppType
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.getOrNull
import page.ooooo.geoshare.ui.components.AppIcon
import page.ooooo.geoshare.ui.components.TextIcon

/**
 * See https://developers.google.com/maps/documentation/urls/get-started
 */
object GoogleMapsOutput : Output {

    open class CopyLinkAction : CopyAction() {
        override fun getText(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatDisplayUriString(points, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_copy_link,
                    stringResource(R.string.converter_google_maps_name)
                )
            )
        }

        override fun getIcon() = @Composable {
            TextIcon("G")
        }
    }

    open class CopyNavigateToAction : CopyAction() {
        override fun getText(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatNavigateToUriString(points, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_copy_link_drive_to,
                    stringResource(R.string.converter_google_maps_name)
                )
            )
        }
    }

    open class CopyStreetViewAction : CopyAction() {
        override fun getText(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatStreetViewUriString(points, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_copy_link_street_view,
                    stringResource(R.string.converter_google_maps_name)
                )
            )
        }

        override fun isEnabled(points: ImmutableList<Point>, i: Int?) = points.getOrNull(i)?.hasCoordinates() == true
    }

    object CopyLinkChipAction : CopyLinkAction() {
        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_google_maps))
        }
    }

    data class ShareNavigateToWithAppAction(override val packageName: String) : OpenAppAction(packageName) {
        override fun getUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatNavigateToUriString(points, i, uriQuote)

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
        override fun getUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatStreetViewUriString(points, i, uriQuote)

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

        override fun isEnabled(points: ImmutableList<Point>, i: Int?) = points.getOrNull(i)?.hasCoordinates() == true
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

    data class ShareNavigateToWithAppAutomation(override val packageName: String) : OpenAppAction(packageName),
        Action.HasSuccessMessage, Action.HasErrorMessage, BasicAutomation, Automation.HasDelay {

        override val type = Automation.Type.OPEN_APP_GOOGLE_MAPS_NAVIGATE_TO
        override val testTag = null

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

    data class ShareStreetViewWithAppAutomation(override val packageName: String) : OpenAppAction(packageName),
        Action.HasSuccessMessage, Action.HasErrorMessage, BasicAutomation, Automation.HasDelay {

        override val type = Automation.Type.OPEN_APP_GOOGLE_MAPS_STREET_VIEW
        override val testTag = null

        override fun getUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatStreetViewUriString(points, i, uriQuote)

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
        override fun waitingText(counterSec: Int) = pluralStringResource(
            R.plurals.conversion_automation_open_app_waiting,
            counterSec,
            AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label ?: packageName,
            counterSec,
        )
    }

    object ShareNavigateToAction : OpenChooserAction() {
        override fun getUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatNavigateToUriString(points, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app_navigate_to,
                    stringResource(R.string.converter_google_maps_name)
                )
            )
        }
    }

    object ShareStreetViewAction : OpenChooserAction() {
        override fun getUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatStreetViewUriString(points, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_open_app_street_view,
                    stringResource(R.string.converter_google_maps_name)
                )
            )
        }
    }

    override fun getPointsActions() = listOf(
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

    override fun getAppActions(apps: List<App>): List<Pair<String, Action>> = buildList {
        apps.forEach { app ->
            (app.type as? GoogleMapsAppType)?.let {
                add(app.packageName to ShareNavigateToWithAppAction(app.packageName))
                add(app.packageName to ShareStreetViewWithAppAction(app.packageName))
            }
        }
    }

    override fun getLastPointChipActions() = listOf(CopyLinkChipAction)

    override fun getRandomAction() = CopyLinkAction()

    override fun getAutomations(apps: List<App>): List<Automation> = buildList {
        add(CopyLinkAutomation)
        add(CopyNavigateToAutomation)
        add(CopyStreetViewAutomation)
        apps.forEach { app ->
            (app.type as? GoogleMapsAppType)?.let {
                add(ShareNavigateToWithAppAutomation(app.packageName))
                add(ShareStreetViewWithAppAutomation(app.packageName))
            }
        }
    }

    override fun findAutomation(type: Automation.Type, packageName: String?): Automation? = when (type) {
        Automation.Type.COPY_GOOGLE_MAPS_URI -> CopyLinkAutomation

        Automation.Type.COPY_GOOGLE_MAPS_NAVIGATE_TO_URI -> CopyNavigateToAutomation

        Automation.Type.COPY_GOOGLE_MAPS_STREET_VIEW_URI -> CopyStreetViewAutomation

        Automation.Type.OPEN_APP_GOOGLE_MAPS_NAVIGATE_TO if packageName != null -> ShareNavigateToWithAppAutomation(
            packageName
        )

        Automation.Type.OPEN_APP_GOOGLE_MAPS_STREET_VIEW if packageName != null -> ShareStreetViewWithAppAutomation(
            packageName
        )

        else -> null
    }

    private fun formatDisplayUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote): String = Uri(
        scheme = "https",
        host = "www.google.com",
        path = "/maps",
        queryParams = buildMap {
            points.getOrNull(i)?.toGCJ02()?.run {
                latStr?.let { latStr ->
                    lonStr?.let { lonStr ->
                        set("q", "$latStr,$lonStr")
                    }
                } ?: name?.let { name ->
                    set("q", name)
                }
                zStr?.let { zStr ->
                    set("z", zStr)
                }
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()

    private fun formatNavigateToUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote): String = Uri(
        scheme = "google.navigation",
        path = (points.getOrNull(i)?.toGCJ02()?.run {
            latStr?.let { latStr ->
                lonStr?.let { lonStr ->
                    "$latStr,$lonStr"
                }
            } ?: name
        } ?: "0,0").let { "q=$it" },
        uriQuote = uriQuote,
    ).toString()

    private fun formatStreetViewUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote): String = Uri(
        scheme = "google.streetview",
        path = (points.getOrNull(i)?.toGCJ02()?.run {
            latStr?.let { latStr ->
                lonStr?.let { lonStr ->
                    "$latStr,$lonStr"
                }
            }
        } ?: "0,0").let { @Suppress("SpellCheckingInspection") "cbll=$it" },
        uriQuote = uriQuote,
    ).toString()
}
