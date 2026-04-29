package page.ooooo.geoshare.lib.outputs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.ui.components.ImageVectorIconDescriptor
import javax.inject.Inject

/**
 * When executed, this output saves a [Point] in the address field of a contact.
 */
class SavePointToContactOutput @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
) :
    PointOutput.WithoutLocation,
    Output.HasErrorText,
    Output.HasAutomationErrorText {

    override suspend fun execute(value: Point, actionContext: ActionContext) =
        AndroidTools.insertOrEditContactAddress(
            actionContext.context,
            CoordinateFormatter.formatDecCoords(
                coordinateConverter.toWGS84(value)
            ),
        )

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.output_save_to_contact)

    override fun getMenuIcon(appDetails: AppDetails) =
        ImageVectorIconDescriptor(Icons.Default.AccountBox)

    @Composable
    override fun errorText(appDetails: AppDetails) =
        stringResource(R.string.output_save_to_contact_failed)

    @Composable
    override fun automationErrorText(appDetails: AppDetails) =
        stringResource(R.string.output_save_to_contact_automation_failed)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is SavePointToContactOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
