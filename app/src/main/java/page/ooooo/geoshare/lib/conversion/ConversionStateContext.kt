package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
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
    val inputs: List<Input> = emptyList(),
    val linkRepository: LinkRepository,
    val outputRepository: OutputRepository,
    val resources: Resources,
    val userPreferencesRepository: UserPreferencesRepository,
    val log: Log = DefaultLog,
    val billing: Billing,
    val uriQuote: UriQuote = DefaultUriQuote,
    val onStateChange: (ConversionState) -> Unit = {},
) {
    var currentState: ConversionState = Initial
        set(value) {
            field = value
            onStateChange(value)
        }

    suspend fun transition() {
        var i = 0
        while (i < MAX_ITERATIONS) {
            currentState = currentState.transition(this) ?: break
            i++
        }
        if (i >= MAX_ITERATIONS) {
            throw IllegalStateException("Exceeded max state iterations")
        }
    }

    companion object {
        const val MAX_ITERATIONS = 30
    }
}
