package page.ooooo.geoshare.lib

import android.net.Uri
import android.os.Build
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.lib.converters.UrlConverter

data class ConversionStateContext(
    val urlConverters: List<UrlConverter>,
    val intentTools: IntentTools,
    val networkTools: NetworkTools,
    val userPreferencesRepository: UserPreferencesRepository,
    val log: ILog = DefaultLog(),
    val onStateChange: (State) -> Unit = {},
    val getBuildVersionSdkInt: () -> Int = { Build.VERSION.SDK_INT },
    val uriQuote: UriQuote = DefaultUriQuote(),
    val parseUri: (uriString: String) -> Uri = { Uri.parse(it) },
) : StateContext() {
    override var currentState: State = Initial()
        set(value) {
            field = value
            onStateChange(value)
        }
}
