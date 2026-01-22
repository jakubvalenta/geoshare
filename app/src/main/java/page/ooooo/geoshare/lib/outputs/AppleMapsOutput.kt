package page.ooooo.geoshare.lib.outputs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.inputs.AppleMapsInput
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

/**
 * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
 */
object AppleMapsOutput : Output {

    open class CopyLinkAction : CopyAction() {
        override fun getText(position: Position, i: Int?, uriQuote: UriQuote) =
            formatUriString(position, i, uriQuote)

        @Composable
        override fun Label() {
            Text(stringResource(R.string.conversion_succeeded_copy_link, AppleMapsInput.NAME))
        }
    }

    object CopyLinkAutomation : CopyLinkAction(), BasicAutomation {
        override val type = Automation.Type.COPY_APPLE_MAPS_URI
        override val packageName = ""
        override val testTag = null

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_link_succeeded)
    }

    override fun getPositionActions() = listOf(CopyLinkAction())

    override fun getPointActions(): List<BasicAction> = listOf(CopyLinkAction())

    override fun getRandomAction() = CopyLinkAction()

    override fun getAutomations(apps: List<AndroidTools.App>) = listOf(CopyLinkAutomation)

    override fun findAutomation(type: Automation.Type, packageName: String?) = when (type) {
        Automation.Type.COPY_APPLE_MAPS_URI -> CopyLinkAutomation
        else -> null
    }

    private fun formatUriString(position: Position, i: Int?, uriQuote: UriQuote): String = Uri(
        scheme = "https",
        host = "maps.apple.com",
        path = "/",
        queryParams = buildMap {
            position.getPoint(i)
                ?.toStringPair(Srs.WGS84)?.let { (latStr, lonStr) ->
                    set("ll", "$latStr,$lonStr")
                } ?: position.q?.let { q ->
                set("q", q)
            }
            position.zStr?.let { zStr ->
                set("z", zStr)
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()
}
