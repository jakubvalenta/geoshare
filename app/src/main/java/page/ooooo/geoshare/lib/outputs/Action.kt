package page.ooooo.geoshare.lib.outputs

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.Clipboard
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position

sealed interface Action {
    @Composable
    fun Label()

    fun isEnabled(position: Position, i: Int?): Boolean = true

    interface HasPackageName {
        val packageName: String
    }

    interface HasSuccessMessage {
        @Composable
        fun successText(): String
    }

    interface HasErrorMessage {
        @Composable
        fun errorText(): String
    }
}

interface BasicAction : Action {
    suspend fun runAction(
        position: Position,
        i: Int?,
        context: Context,
        clipboard: Clipboard,
        resources: Resources,
        saveGpxLauncher: ActivityResultLauncher<Intent>,
        uriQuote: UriQuote = DefaultUriQuote(),
    ): Boolean
}

interface LocationAction : Action {
    suspend fun runAction(
        position: Position,
        i: Int?,
        location: Point,
        context: Context,
        clipboard: Clipboard,
        resources: Resources,
        saveGpxLauncher: ActivityResultLauncher<Intent>,
        uriQuote: UriQuote = DefaultUriQuote(),
    ): Boolean

    @Composable
    fun permissionText(): String
}
