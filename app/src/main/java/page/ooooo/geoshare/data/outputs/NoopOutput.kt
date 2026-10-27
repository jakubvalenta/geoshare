package page.ooooo.geoshare.data.outputs

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetails

object NoopOutput : StringOutput {
    override val id = "NoopOutput"

    override suspend fun execute(value: String, context: Context) = ActionResult.SUCCEEDED

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
