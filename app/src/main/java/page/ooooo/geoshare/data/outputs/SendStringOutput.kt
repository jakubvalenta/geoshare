package page.ooooo.geoshare.data.outputs

import androidx.compose.runtime.Composable
import page.ooooo.geoshare.data.android.AppDetail
import page.ooooo.geoshare.data.android.TextActivity

class SendStringOutput(
    override val activity: TextActivity,
) : StringOutput, Output.HasActivity<TextActivity> {
    override val id = "SendStringOutput(activity=$activity)"

    override suspend fun execute(value: String, actionContext: ActionContext) =
        activity
            .launch(actionContext.context, value)
            .toActionResult(openedApp = true)

    @Composable
    override fun label(appDetail: AppDetail?) =
        appDetail?.label.orEmpty()

    override fun getMenuIcon(appDetail: AppDetail?) =
        appDetail?.let { DrawableIconDescriptor(it.icon) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SendStringOutput
        return activity == other.activity
    }

    override fun hashCode() = activity.hashCode()
}
