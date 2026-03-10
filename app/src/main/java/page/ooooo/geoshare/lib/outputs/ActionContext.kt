package page.ooooo.geoshare.lib.outputs

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.platform.Clipboard
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AndroidTools

data class ActionContext(
    val context: Context,
    val clipboard: Clipboard,
    val resources: Resources,
    val androidTools: AndroidTools = AndroidTools,
    val uriQuote: UriQuote = DefaultUriQuote,
)
