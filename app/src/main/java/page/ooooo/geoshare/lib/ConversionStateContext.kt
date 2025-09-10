package page.ooooo.geoshare.lib

import android.net.Uri
import androidx.core.net.toUri
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.lib.converters.UrlConverter

data class ConversionStateContext(
    val urlConverters: List<UrlConverter>,
    val intentTools: IntentTools,
    val networkTools: NetworkTools,
    val userPreferencesRepository: UserPreferencesRepository,
    val log: ILog = DefaultLog(),
    val onStateChange: (State) -> Unit = {},
    val uriQuote: UriQuote = DefaultUriQuote(),
    val parseUri: (uriString: String) -> Uri = { it.toUri() },
) : StateContext() {
    override var currentState: State = Initial()
        set(value) {
            field = value
            onStateChange(value)
        }
}
