package page.ooooo.geoshare.data.outputs

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.platform.Clipboard
import page.ooooo.geoshare.data.uri.DefaultUriQuote
import page.ooooo.geoshare.data.uri.UriQuote

data class ActionContext(
    val context: Context,
    val clipboard: Clipboard,
    val resources: Resources,
    val uriQuote: UriQuote = DefaultUriQuote,
)
