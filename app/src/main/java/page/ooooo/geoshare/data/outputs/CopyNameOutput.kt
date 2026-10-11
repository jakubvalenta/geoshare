package page.ooooo.geoshare.data.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetail
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.uri.UriQuote

/**
 * Copies the name of the point to the clipboard.
 *
 * Only available when the point actually has a name.
 */
object CopyNameOutput : CopyPointTextOutput {
    override val id = "CopyNameOutput"

    override fun getText(value: Point, uriQuote: UriQuote) =
        value.cleanName

    @Composable
    override fun label(appDetail: AppDetail?) =
        stringResource(R.string.conversion_succeeded_copy_name)

    @Composable
    override fun automationSuccessText(appDetail: AppDetail?) =
        stringResource(R.string.conversion_automation_copy_name_succeeded)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is CopyNameOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
