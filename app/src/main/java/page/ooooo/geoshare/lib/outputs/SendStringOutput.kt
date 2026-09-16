package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.TextActivity
import page.ooooo.geoshare.ui.components.DrawableIconDescriptor

class SendStringOutput(
    val activity: TextActivity,
) : StringOutput {
    override val id = "SendStringOutput(activity=$activity)"

    override suspend fun execute(value: String, actionContext: ActionContext) =
        activity
            .launch(actionContext.context, value)
            .toActionResult(openedApp = true)

    @Composable
    override fun label(appLabel: String?) = appLabel.orEmpty()

    override fun getMenuIcon(appDetails: AppDetails) =
        appDetails[activity.packageName]?.let { DrawableIconDescriptor(it.icon) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SendStringOutput
        return activity == other.activity
    }

    override fun hashCode() = activity.hashCode()
}
