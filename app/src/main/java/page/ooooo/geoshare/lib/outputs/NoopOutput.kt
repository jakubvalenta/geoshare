package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.geo.Point

class NoopOutput : PointOutput.WithoutLocation {
    override suspend fun execute(value: Point, actionContext: ActionContext) = true

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.user_preferences_automation_nothing)

    override fun getMenuIcon(appDetails: AppDetails) = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is NoopOutput
    }

    override fun hashCode() = javaClass.hashCode()
}
