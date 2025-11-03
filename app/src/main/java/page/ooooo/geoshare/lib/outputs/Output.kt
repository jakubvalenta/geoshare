package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import page.ooooo.geoshare.lib.ConversionRunContext
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

interface Output {
    sealed class Action {
        // TODO Move out of Output

        abstract suspend fun run(intentTools: IntentTools, runContext: ConversionRunContext): Boolean

        @Immutable
        data class Copy(val text: String) : Action() {
            override suspend fun run(intentTools: IntentTools, runContext: ConversionRunContext) =
                intentTools.copyToClipboard(runContext.context, runContext.clipboard, text).let { true }
        }

        @Immutable
        data class OpenApp(val packageName: String, val uriString: String) : Action() {
            override suspend fun run(intentTools: IntentTools, runContext: ConversionRunContext) =
                intentTools.openApp(runContext.context, packageName, uriString)
        }

        @Immutable
        data class OpenChooser(val uriString: String) : Action() {
            override suspend fun run(intentTools: IntentTools, runContext: ConversionRunContext) =
                intentTools.openChooser(runContext.context, uriString)
        }

        class SaveGpx() : Action() {
            override suspend fun run(intentTools: IntentTools, runContext: ConversionRunContext) =
                intentTools.launchSaveGpx(runContext.context, runContext.saveGpxLauncher)
        }
    }

    data class LabeledAction<T : Action>(val action: T, val label: @Composable () -> String)

    fun getText(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String?

    fun getText(point: Point, uriQuote: UriQuote = DefaultUriQuote()): String?

    fun getActions(position: Position, uriQuote: UriQuote = DefaultUriQuote()): List<LabeledAction<Action>>

    fun getActions(point: Point, uriQuote: UriQuote = DefaultUriQuote()): List<LabeledAction<Action>>

    fun getChips(position: Position, uriQuote: UriQuote = DefaultUriQuote()): List<LabeledAction<Action>>
}
