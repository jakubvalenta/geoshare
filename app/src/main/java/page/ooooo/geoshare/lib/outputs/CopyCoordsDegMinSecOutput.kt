package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formats.CoordsFormat
import page.ooooo.geoshare.lib.point.Point

object CopyCoordsDegMinSecOutput : CopyPointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        CoordsFormat.formatDegMinSecCoords(value)

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_copy_coordinates)

    override fun getAutomationDescription() = @Composable {
        getText(Point.example)
    }

    @Composable
    override fun automationSuccessText(appDetails: AppDetails) =
        stringResource(R.string.conversion_automation_copy_succeeded)
}
