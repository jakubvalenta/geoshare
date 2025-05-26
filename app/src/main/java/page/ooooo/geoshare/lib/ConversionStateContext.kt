package page.ooooo.geoshare.lib

import android.os.Build
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.lib.converters.UrlConverter

data class ConversionStateContext(
    val urlConverters: List<UrlConverter>,
    val intentTools: IntentTools,
    val networkTools: NetworkTools,
    val userPreferencesRepository: UserPreferencesRepository,
    val xiaomiTools: XiaomiTools,
    val clipboardTools: ClipboardTools = ClipboardTools(),
    val log: ILog = DefaultLog(),
    val onMessage: (message: Message) -> Unit,
    val getBuildVersionSdkInt: () -> Int = { Build.VERSION.SDK_INT },
) : StateContext() {
    override var currentState: State = Initial()
}
