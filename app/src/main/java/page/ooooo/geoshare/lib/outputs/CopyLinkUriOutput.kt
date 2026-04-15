package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import javax.inject.Inject

class CopyLinkUriOutput @Inject constructor(
    val link: Link,
    private val coordinateConverter: CoordinateConverter,
) : CopyPointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        UriFormatter.formatUriString(
            coordinateConverter.toSrs(value, link.srs),
            link.coordsUriTemplate,
            link.nameUriTemplate,
            uriQuote = uriQuote,
        )

    override fun getIcon(appDetails: AppDetails) =
        link.icon

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_copy_link, link.name)

    @Composable
    override fun automationSuccessText(appDetails: AppDetails) =
        stringResource(R.string.conversion_automation_copy_link_succeeded)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CopyLinkUriOutput
        return link == other.link
    }

    override fun hashCode() = link.hashCode()
}
