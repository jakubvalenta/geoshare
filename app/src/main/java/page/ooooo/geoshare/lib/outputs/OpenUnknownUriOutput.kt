package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.UriActivity
import page.ooooo.geoshare.ui.components.DrawableIconDescriptor

class OpenUnknownUriOutput(
    override val activity: UriActivity,
) : StringOutput, Output.HasActivity<UriActivity> {
    override val id = "OpenUnknownUriOutput(activity=$activity)"

    override suspend fun execute(value: String, actionContext: ActionContext) =
        activity
            .launch(actionContext.context, value)
            .toActionResult(openedApp = true)

    @Composable
    override fun label(appDetail: AppDetail?) = appDetail?.label.orEmpty()

    override fun getMenuIcon(appDetail: AppDetail?) =
        appDetail?.let { DrawableIconDescriptor(it.icon) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OpenUnknownUriOutput
        return activity == other.activity
    }

    override fun hashCode() = activity.hashCode()
}
