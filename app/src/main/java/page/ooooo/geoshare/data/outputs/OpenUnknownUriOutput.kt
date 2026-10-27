package page.ooooo.geoshare.data.outputs

import android.content.Context
import androidx.compose.runtime.Composable
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.android.UriActivity

class OpenUnknownUriOutput(
    override val activity: UriActivity,
) : StringOutput, Output.HasActivity<UriActivity> {
    override val id = "OpenUnknownUriOutput(activity=$activity)"

    override suspend fun execute(value: String, context: Context) =
        activity
            .launch(context, value)
            .toActionResult(openedApp = true)

    @Composable
    override fun label(appDetails: AppDetails) =
        appDetails[activity.packageName]?.label.orEmpty()

    override fun getMenuIcon(appDetails: AppDetails) =
        appDetails[activity.packageName]?.let { DrawableIconDescriptor(it.icon) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OpenUnknownUriOutput
        return activity == other.activity
    }

    override fun hashCode() = activity.hashCode()
}
