package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.App
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.getOrNull
import page.ooooo.geoshare.ui.components.TextIcon

/**
 * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
 */
object AppleMapsOutput : Output {

    open class CopyDisplayLinkAction : CopyAction() {
        override fun getText(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatDisplayUriString(points, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_copy_link,
                    stringResource(R.string.converter_apple_maps_name)
                )
            )
        }

        override fun getIcon() = @Composable {
            TextIcon("A")
        }
    }

    open class CopyNavigateToLinkAction : CopyAction() {
        override fun getText(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote) =
            formatNavigateToUriString(points, i, uriQuote)

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_copy_link_drive_to,
                    stringResource(R.string.converter_apple_maps_name)
                )
            )
        }
    }

    object CopyDisplayLinkAutomation : CopyDisplayLinkAction(), BasicAutomation {
        override val type = Automation.Type.COPY_APPLE_MAPS_URI
        override val packageName = ""
        override val testTag = null

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    object CopyNavigateToLinkAutomation : CopyNavigateToLinkAction(), BasicAutomation {
        override val type = Automation.Type.COPY_APPLE_MAPS_NAVIGATE_TO_URI
        override val packageName = ""
        override val testTag = null

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    override fun getPointsActions() = listOf(
        CopyDisplayLinkAction(),
        CopyNavigateToLinkAction(),
    )

    override fun getPointActions(): List<BasicAction> = listOf(
        CopyDisplayLinkAction(),
        CopyNavigateToLinkAction(),
    )

    override fun getRandomAction() = listOf(CopyDisplayLinkAction(), CopyNavigateToLinkAction()).randomOrNull()

    override fun getAutomations(apps: List<App>): List<Automation> = listOf(
        CopyDisplayLinkAutomation,
        CopyNavigateToLinkAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.COPY_APPLE_MAPS_URI -> CopyDisplayLinkAutomation
        else -> null
    }

    private fun formatDisplayUriString(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote): String = Uri(
        scheme = "https",
        host = "maps.apple.com",
        path = "/",
        queryParams = buildMap {
            points.getOrNull(i)?.toWGS84()?.run {
                latStr?.let { latStr ->
                    lonStr?.let { lonStr ->
                        set("ll", "$latStr,$lonStr")
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
        scheme = "https",
        host = "maps.apple.com",
        path = "/",
        queryParams = buildMap {
            points.getOrNull(i)?.toWGS84()?.run {
                latStr?.let { latStr ->
                    lonStr?.let { lonStr ->
                        set(@Suppress("SpellCheckingInspection") "daddr", "$latStr,$lonStr")
                    }
                } ?: name?.let { name ->
                    set(@Suppress("SpellCheckingInspection") "daddr", name)
                }
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()
}
