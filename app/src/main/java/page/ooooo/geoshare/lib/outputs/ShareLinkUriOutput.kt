package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.openUriInDefaultApp
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import javax.inject.Inject

class ShareLinkUriOutput @Inject constructor(
    val link: Link,
    private val coordinateConverter: CoordinateConverter,
) : SharePointUriOutput {
    override val id = "ShareLinkUriOutput(link.uuid=${link.uuid})"

    override fun getUriString(value: Point, uriQuote: UriQuote) =
        UriFormatter.formatUriString(
            coordinateConverter.toSrs(value, link.srs),
            link.coordsUriTemplate,
            link.nameUriTemplate,
            uriQuote = uriQuote,
        )

    override suspend fun execute(value: Point, actionContext: ActionContext) =
        getUriString(value, actionContext.uriQuote)?.let { uriString ->
            actionContext.context.openUriInDefaultApp(uriString)
        }.let { success -> if (success == true) ActionResult.SUCCEEDED_AND_OPENED_APP else ActionResult.FAILED }

    @Composable
    override fun label(appLabel: String?) =
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
