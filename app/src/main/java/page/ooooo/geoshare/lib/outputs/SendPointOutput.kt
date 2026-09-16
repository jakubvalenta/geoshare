package page.ooooo.geoshare.lib.outputs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.TextActivity
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Srs
import page.ooooo.geoshare.ui.components.DrawableIconDescriptor
import page.ooooo.geoshare.ui.components.ImageVectorIconDescriptor
import javax.inject.Inject

/**
 * Creates a Google Maps display URI and sends it via [activity], which is often a messaging app.
 */
class SendPointOutput @Inject constructor(
    val activity: TextActivity,
    private val coordinateConverter: CoordinateConverter,
) :
    PointOutput.WithoutLocation,
    Output.HasErrorText,
    Output.HasAutomationDelay,
    Output.HasAutomationErrorText {

    override val id = "SendPointOutput(activity=$activity)"

    fun getUriString(value: Point, uriQuote: UriQuote): String? =
        UriFormatter.formatUriString(
            point = coordinateConverter.toSrs(value, Srs.GCJ02_MAINLAND_CHINA),
            // Use https://maps.google.com/?q= instead of https://www.google.com/maps/search/?api=1&q=, because
            // Telegram doesn't support the API link
            coordsUriTemplate = "https://maps.google.com/?q={lat}%2C{lon}",
            nameUriTemplate = "https://maps.google.com/?q={q}",
            uriQuote = uriQuote,
        )

    override suspend fun execute(value: Point, actionContext: ActionContext) =
        getUriString(value, actionContext.uriQuote)
            ?.let { text -> activity.launch(actionContext.context, text) }
            .toActionResult(openedApp = true)

    @Composable
    override fun label(appLabel: String?) =
        stringResource(R.string.output_send)

    override fun getMenuIcon(appDetails: AppDetails) =
        ImageVectorIconDescriptor(Icons.AutoMirrored.Default.Send)

    override fun getIcon(appDetails: AppDetails) =
        appDetails[activity.packageName]?.let { DrawableIconDescriptor(it.icon) }

    @Composable
    override fun errorText(appLabel: String?) =
        stringResource(R.string.conversion_succeeded_open_app_failed, appLabel.orEmpty())

    @Composable
    override fun automationLabel(appLabel: String?) =
        stringResource(R.string.output_send_via, appLabel.orEmpty())

    @Composable
    override fun automationErrorText(appLabel: String?) =
        stringResource(R.string.conversion_automation_open_app_failed, appLabel.orEmpty())

    @Composable
    override fun automationWaitingText(counterSec: Int, appLabel: String?) =
        pluralStringResource(
            R.plurals.conversion_automation_open_app_waiting,
            counterSec,
            appLabel.orEmpty(),
            counterSec,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SendPointOutput
        return activity == other.activity
    }

    override fun hashCode() = activity.hashCode()
}
