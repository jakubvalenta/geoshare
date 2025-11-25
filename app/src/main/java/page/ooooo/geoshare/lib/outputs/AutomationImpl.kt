package page.ooooo.geoshare.lib.outputs

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.position.Position

object NoopAutomation : BasicAutomation {
    override val type = Automation.Type.NOOP
    override val packageName = ""
    override val testTag = null

    override suspend fun runAction(
        position: Position,
        i: Int?,
        context: Context,
        clipboard: Clipboard,
        resources: Resources,
        saveGpxLauncher: ActivityResultLauncher<Intent>,
        uriQuote: UriQuote,
    ): Boolean = true

    @Composable
    override fun Label() {
        Text(stringResource(R.string.user_preferences_automation_nothing))
    }
}
