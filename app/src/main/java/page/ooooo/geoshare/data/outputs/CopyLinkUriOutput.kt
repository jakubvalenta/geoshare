package page.ooooo.geoshare.data.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetail
import page.ooooo.geoshare.data.database.Link
import page.ooooo.geoshare.data.formatters.UriFormatter
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject

class CopyLinkUriOutput @Inject constructor(
    val link: Link,
    private val coordinateConverter: CoordinateConverter,
) : CopyPointTextOutput {
    override val id = "CopyLinkUriOutput"

    override fun getText(value: Point, uriQuote: UriQuote) =
        UriFormatter.formatUriString(
            coordinateConverter.toSrs(value, link.srs),
            link.coordsUriTemplate,
            link.nameUriTemplate,
            uriQuote = uriQuote,
        )

    @Composable
    override fun label(appDetail: AppDetail?) =
        stringResource(R.string.conversion_succeeded_copy_link, link.name)

    override fun getIcon(appDetail: AppDetail?) =
        link.icon

    @Composable
    override fun automationSuccessText(appDetail: AppDetail?) =
        stringResource(R.string.conversion_automation_copy_link_succeeded)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CopyLinkUriOutput
        return link == other.link
    }

    override fun hashCode() = link.hashCode()
}
