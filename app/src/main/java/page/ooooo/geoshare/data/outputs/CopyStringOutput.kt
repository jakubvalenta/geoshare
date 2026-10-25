package page.ooooo.geoshare.data.outputs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.content.getSystemService
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetails

object CopyStringOutput : StringOutput, Output.HasSuccessText {
    override val id = "CopyStringOutput"

    override suspend fun execute(value: String, context: Context) =
        context.getSystemService<ClipboardManager>()
            ?.setPrimaryClip(ClipData.newPlainText(null, value))
            ?.let { true }
            .toActionResult()

    @Composable
    override fun label(appDetails: AppDetails) =
        stringResource(R.string.conversion_succeeded_skip)

    override fun getDescription(value: String) =
        value

    override fun getMenuIcon(appDetails: AppDetails) =
        ResourceIconDescriptor(R.drawable.content_copy_24px)

    @Composable
    override fun successText(appDetails: AppDetails) =
        stringResource(R.string.copying_finished)
}
