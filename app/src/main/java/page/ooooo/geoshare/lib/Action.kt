package page.ooooo.geoshare.lib

import androidx.compose.runtime.Immutable

sealed class Action {
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
