package page.ooooo.geoshare.lib.outputs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Srs
import page.ooooo.geoshare.ui.components.DrawableIconDescriptor
import page.ooooo.geoshare.ui.components.ImageVectorIconDescriptor
import javax.inject.Inject

/**
 * Creates a Google Maps display URI and sends it via [packageName], which is often a messaging app.
 */
class SendViaAppOutput @Inject constructor(
    override val packageName: String,
    private val coordinateConverter: CoordinateConverter,
) : OpenPointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        UriFormatter.formatUriString(
            point = coordinateConverter.toSrs(value, Srs.GCJ02_MAINLAND_CHINA),
            // Use https://maps.google.com/?q= instead of https://www.google.com/maps/search/?api=1&q=, because
            // Telegram doesn't support the API link
            coordsUriTemplate = "https://maps.google.com/?q={lat}%2C{lon}",
            nameUriTemplate = "https://maps.google.com/?q={q}",
            uriQuote = uriQuote,
        )

    override suspend fun execute(value: Point, actionContext: ActionContext): Boolean =
        getText(value, actionContext.uriQuote)?.let { text ->
            actionContext.androidTools.sendViaApp(actionContext.context, packageName, text)
        } ?: false

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.output_send_via_app)

    override fun getMenuIcon(appDetails: AppDetails) =
        ImageVectorIconDescriptor(Icons.AutoMirrored.Default.Send)

    override fun getIcon(appDetails: AppDetails) =
        appDetails[packageName]?.let { DrawableIconDescriptor(it.icon) }

    @Composable
    override fun automationLabel(appDetails: AppDetails) =
        stringResource(
            R.string.conversion_automation_send_via_app,
            appDetails[packageName]?.label ?: packageName,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SendViaAppOutput
        return packageName == other.packageName
    }

    override fun hashCode() = packageName.hashCode()
}
