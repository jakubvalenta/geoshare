package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.billing.Billing
import page.ooooo.geoshare.lib.inputs.Input

class ConversionStateContext(
    val inputs: List<Input<*>> = emptyList(),
    val linkRepository: LinkRepository,
    val outputRepository: OutputRepository,
    val resources: Resources,
    val userPreferencesRepository: UserPreferencesRepository,
    val engine: HttpClientEngine = CIO.create(),
    val log: Log = DefaultLog,
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
