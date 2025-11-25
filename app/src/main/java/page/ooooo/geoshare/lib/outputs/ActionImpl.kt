package page.ooooo.geoshare.lib.outputs

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.position.Position

abstract class CopyAction : BasicAction, Action.HasSuccessMessage {
    abstract fun getText(position: Position, i: Int? = null, uriQuote: UriQuote = DefaultUriQuote()): String

    override suspend fun runAction(
        position: Position,
        i: Int?,
        context: Context,
        clipboard: Clipboard,
        resources: Resources,
        saveGpxLauncher: ActivityResultLauncher<Intent>,
        uriQuote: UriQuote,
    ): Boolean {
        AndroidTools.copyToClipboard(clipboard, getText(position, i, uriQuote))
        return true
    }

    @Composable
    override fun successText() = stringResource(R.string.copying_finished)
}

abstract class OpenAppAction(override val packageName: String) :
    BasicAction,
    Action.HasErrorMessage,
    Action.HasPackageName {

    abstract fun getUriString(position: Position, i: Int?, uriQuote: UriQuote): String

    override suspend fun runAction(
        position: Position,
        i: Int?,
        context: Context,
        clipboard: Clipboard,
        resources: Resources,
        saveGpxLauncher: ActivityResultLauncher<Intent>,
        uriQuote: UriQuote,
    ) =
        AndroidTools.openApp(context, packageName, getUriString(position, i, uriQuote))

    @Composable
    override fun errorText() =
        stringResource(
            R.string.conversion_succeeded_open_app_failed,
            AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)?.label
                ?: packageName,
        )

    private var appDetailsCache: AndroidTools.AppDetails? = null

    @Composable
    protected fun queryAppDetails(): AndroidTools.AppDetails? =
        appDetailsCache ?: AndroidTools.queryAppDetails(LocalContext.current.packageManager, packageName)
            ?.also { appDetailsCache = it }
}

abstract class OpenChooserAction : BasicAction, Action.HasErrorMessage {
    abstract fun getUriString(position: Position, i: Int?, uriQuote: UriQuote): String

    override suspend fun runAction(
        position: Position,
        i: Int?,
        context: Context,
        clipboard: Clipboard,
        resources: Resources,
        saveGpxLauncher: ActivityResultLauncher<Intent>,
        uriQuote: UriQuote,
    ) =
        AndroidTools.openChooser(context, getUriString(position, i, uriQuote))

    @Composable
    override fun errorText(): String =
        stringResource(R.string.conversion_succeeded_apps_not_found)
}
