package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.billing.Billing
import page.ooooo.geoshare.lib.inputs.Input
import kotlin.time.TimeSource

interface ConversionStateContext {
    val billing: Billing
    val inputs: List<Input>
    val linkRepository: LinkRepository
    val log: Log
    val outputRepository: OutputRepository
    val resources: Resources
    val uriQuote: UriQuote
    val userPreferencesRepository: UserPreferencesRepository

    val currentState: StateFlow<ConversionState>
    val stateLog: StateFlow<List<ConversionStateLogItem>>

    suspend fun transition(newState: ConversionState, resetLog: Boolean = false)

    fun setExceptionState(tr: Throwable, newState: ConversionState)
}

class DefaultConversionStateContext(
    override val billing: Billing,
    override val inputs: List<Input> = emptyList(),
    override val linkRepository: LinkRepository,
    override val log: Log = DefaultLog,
    override val outputRepository: OutputRepository,
    override val resources: Resources,
    override val uriQuote: UriQuote = DefaultUriQuote,
    override val userPreferencesRepository: UserPreferencesRepository,
) : ConversionStateContext {
    private var counter: Int = 0
    private val timeSource = TimeSource.Monotonic

    private var _currentState: MutableStateFlow<ConversionState> = MutableStateFlow(Initial)
    override val currentState: StateFlow<ConversionState> = _currentState.asStateFlow()

    private var _stateLog: MutableStateFlow<List<ConversionStateLogItem>> = MutableStateFlow(emptyList())
    override val stateLog: StateFlow<List<ConversionStateLogItem>> = _stateLog.asStateFlow()

    /**
     * Sets [newState] as [currentState] and transition it. Then continues transitioning the current state as long as it
     * keeps returning a state.
     *
     * Throws [IllegalStateException] if the chain of transitions reaches [MAX_ITERATIONS].
     */
    override suspend fun transition(newState: ConversionState, resetLog: Boolean) {
        log.d(TAG, "Set state to $newState")
        setState(newState, resetLog)
        var i = 0
        while (i < MAX_ITERATIONS) {
            val newState = _currentState.value.transition(this) ?: break
            log.d(TAG, "Transitioned state to $newState")
            setState(newState)
            i++
        }
        if (i >= MAX_ITERATIONS) {
            throw IllegalStateException("Exceeded max transition iterations")
        }
    }

    override fun setExceptionState(tr: Throwable, newState: ConversionState) {
        log.e(TAG, "Exception when transitioning state", tr)
        setState(newState)
    }

    private fun setState(newState: ConversionState, resetLog: Boolean = false) {
        _currentState.value = newState
        val newLogItem = ConversionStateLogItem(counter++, newState, timeSource.markNow())
        if (resetLog) {
            _stateLog.value = listOf(newLogItem)
        } else {
            _stateLog.update { it + newLogItem }
        }
    }

    companion object {
        const val MAX_ITERATIONS = 30
        const val TAG = "ConversionStateContext"
    }
}
