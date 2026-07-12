package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.geo.Point
import javax.inject.Inject

/**
 * Copies the name of the point (e.g. the name of a place or business parsed from the link) to the clipboard.
 *
 * Only available when the point actually has a name, see [isAvailable].
 */
class CopyNameOutput @Inject constructor() : CopyPointOutput {
    override fun getText(value: Point, uriQuote: UriQuote) =
        value.cleanName

    override fun isAvailable(value: Point) =
        value.hasName()

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_copy_name)

    @Composable
    override fun automationSuccessText(appDetails: AppDetails) =
        stringResource(R.string.conversion_automation_copy_name_succeeded)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is CopyNameOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
