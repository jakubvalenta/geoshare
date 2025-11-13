package page.ooooo.geoshare.lib.conversion

import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.NetworkTools
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.inputs.Input

data class ConversionStateContext(
    val inputs: List<Input> = emptyList(),
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
