package page.ooooo.geoshare.data.outputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.AppDetail
import page.ooooo.geoshare.data.android.copy
import page.ooooo.geoshare.data.uri.UriQuote

object CopyStringOutput : StringOutput, Output.HasSuccessText {
    override val id = "CopyStringOutput"

    override suspend fun execute(value: String, actionContext: ActionContext) =
        actionContext.clipboard.copy(value)
            .let { true }
            .toActionResult()

    @Composable
    override fun label(appDetail: AppDetail?) =
        stringResource(R.string.conversion_succeeded_skip)

    override fun getDescription(value: String, uriQuote: UriQuote) =
        value

    override fun getMenuIcon(appDetail: AppDetail?) =
        ResourceIconDescriptor(R.drawable.content_copy_24px)

    @Composable
    override fun successText(appDetail: AppDetail?) =
        stringResource(R.string.copying_finished)
}
