package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.billing.Billing
import page.ooooo.geoshare.lib.inputs.NewInput
import page.ooooo.geoshare.lib.network.NetworkTools

class ConversionStateContext(
    val inputs: List<NewInput> = emptyList(),
    val networkTools: NetworkTools = NetworkTools(),
    val linkRepository: LinkRepository,
    val outputRepository: OutputRepository,
    val resources: Resources,
    val userPreferencesRepository: UserPreferencesRepository,
    val log: ILog = DefaultLog,
    val billing: Billing,
    val uriQuote: UriQuote = DefaultUriQuote,
    val onStateChange: (State) -> Unit = {},
) : StateContext() {
    override var currentState: State = Initial()
        set(value) {
            field = value
            onStateChange(value)
        }
}
