package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.point.Point

data class ShareLinkUriOutput(val link: Link) : SharePointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        link.formatUriString(value, uriQuote)

    override suspend fun execute(value: Point, actionContext: ActionContext) =
        getText(value, actionContext.uriQuote)?.let { uriString ->
            AndroidTools.openWebUri(actionContext.context, uriString)
        } ?: false

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.output_open_link, link.name)

    override fun getMenuIcon(appDetails: AppDetails) =
        link.menuIcon

    override fun getIcon(appDetails: AppDetails) =
        link.icon
}
