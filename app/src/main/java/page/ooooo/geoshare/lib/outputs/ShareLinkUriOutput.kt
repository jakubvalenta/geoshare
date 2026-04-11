package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.point.Point
import javax.inject.Inject

class ShareLinkUriOutput @Inject constructor(
    val link: Link,
    private val uriFormatter: UriFormatter,
) : SharePointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        uriFormatter.formatUriString(
            value,
            link.coordsUriTemplate,
            link.nameUriTemplate,
            link.srs,
            uriQuote = uriQuote,
        )

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ShareLinkUriOutput
        return link == other.link
    }

    override fun hashCode() = link.hashCode()
}
