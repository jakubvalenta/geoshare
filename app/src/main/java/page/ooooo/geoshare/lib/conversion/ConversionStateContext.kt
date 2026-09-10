package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
) {
    private var _currentState: MutableStateFlow<ConversionState> = MutableStateFlow(Initial)
    val currentState: StateFlow<ConversionState> = _currentState.asStateFlow()

    /**
     * Sets [newState] as [currentState] and transition it. Then continues transitioning the current state as long as it
     * keeps returning a state.
     *
     * Throws [IllegalStateException] if the chain of transitions reaches [MAX_ITERATIONS].
     */
    suspend fun transition(newState: ConversionState) {
        log.d(TAG, "Set state to $newState")
        _currentState.value = newState
        var i = 0
        while (i < MAX_ITERATIONS) {
            val newState = _currentState.value.transition(this) ?: break
            log.d(TAG, "Transitioned to $newState")
            _currentState.value = newState
            i++
        }
        if (i >= MAX_ITERATIONS) {
            throw IllegalStateException("Exceeded max state iterations")
        }
    }

    fun reset() {
        if (_currentState.value != Initial) {
            _currentState.value = Initial
        }
    }

    fun setExceptionState(tr: Throwable, newState: ConversionState) {
        log.e(TAG, "Exception when transitioning state", tr)
        _currentState.value = newState
    }

    companion object {
        const val MAX_ITERATIONS = 30
        const val TAG = "ConversionStateContext"
    }
}
