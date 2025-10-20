package page.ooooo.geoshare.lib

import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.lib.converters.UrlConverter

data class ConversionStateContext(
    val urlConverters: List<UrlConverter> = emptyList(),
    val intentTools: IntentTools = IntentTools(),
    val networkTools: NetworkTools = NetworkTools(),
    val userPreferencesRepository: UserPreferencesRepository,
    val log: ILog = DefaultLog(),
    val uriQuote: UriQuote = DefaultUriQuote(),
    val onStateChange: (State) -> Unit = {},
) : StateContext() {
    override var currentState: State = Initial()
        set(value) {
            field = value
            onStateChange(value)
        }
}
