package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.point.Point

data class CopyLinkUriOutput(val link: Link) : CopyPointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        link.formatUriString(value, uriQuote)

    override fun getIcon(appDetails: AppDetails) =
        link.icon

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_copy_link, link.name)

    @Composable
    override fun automationSuccessText(appDetails: AppDetails) =
        stringResource(R.string.conversion_automation_copy_link_succeeded)
}
