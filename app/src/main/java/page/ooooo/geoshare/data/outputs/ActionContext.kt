package page.ooooo.geoshare.data.outputs

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.platform.Clipboard

data class ActionContext(
    val context: Context,
    val clipboard: Clipboard,
    val resources: Resources,
)
