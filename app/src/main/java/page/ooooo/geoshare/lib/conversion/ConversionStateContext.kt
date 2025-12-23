package page.ooooo.geoshare.lib.conversion

import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.features.Features
import page.ooooo.geoshare.lib.inputs.Input

data class ConversionStateContext(
    val inputs: List<Input> = emptyList(),
    val networkTools: NetworkTools = NetworkTools(),
    val userPreferencesRepository: UserPreferencesRepository,
    val log: ILog = DefaultLog,
    val features: Features,
    val uriQuote: UriQuote = DefaultUriQuote(),
    val onStateChange: (State) -> Unit = {},
) : StateContext() {
    override var currentState: State = Initial()
        set(value) {
            field = value
            onStateChange(value)
        }
}
