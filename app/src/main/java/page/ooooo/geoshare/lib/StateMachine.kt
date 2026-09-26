package page.ooooo.geoshare.lib

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.ComparableTimeMark
import kotlin.time.TimeSource

interface StateMachine<S : State<C>, C> {
    val currentState: StateFlow<S?>
    val stateLog: StateFlow<List<StateLogItem<S>>>

    fun transition(
        stateContext: C,
        coroutineScope: CoroutineScope,
        resetLog: Boolean = false,
        newState: (suspend () -> S),
    )

    fun reset(stateContext: C, coroutineScope: CoroutineScope)

    fun cancel()
}

class DefaultStateMachine<S : State<C>, C>(
    private val initialState: S,
    exceptionState: (tr: Throwable) -> S,
    private val log: Log = DefaultLog,
) : StateMachine<S, C> {
    private var counter: Int = 0
    private val timeSource = TimeSource.Monotonic

    private var _currentState: MutableStateFlow<S> = MutableStateFlow(initialState)
    override val currentState: StateFlow<S> = _currentState.asStateFlow()

    private var _stateLog: MutableStateFlow<List<StateLogItem<S>>> = MutableStateFlow(emptyList())
    override val stateLog: StateFlow<List<StateLogItem<S>>> = _stateLog.asStateFlow()

    private var transitionJob: Job? = null
    private val transitionExceptionHandler = CoroutineExceptionHandler { _, tr ->
        log.e(TAG, "Exception when transitioning state", tr)
        setState(exceptionState(tr))
    }

    /**
     * Sets [newState] as the current state and transitions it recursively using [transitionRecursively].
     */
    override fun transition(
        stateContext: C,
        coroutineScope: CoroutineScope,
        resetLog: Boolean,
        newState: (suspend () -> S),
    ) {
        transitionJob?.cancel()
        transitionJob = coroutineScope.launch(transitionExceptionHandler) {
            val newState = newState()
            log.d(TAG, "Set state to $newState")
            setState(newState, resetLog)
            transitionRecursively(stateContext)
        }
    }

    /**
     * Transitions current state and sets the resulting state as the new current state. Then it continues transitioning
     * the current state as long as it keeps returning a non-null value.
     *
     * Throws [IllegalStateException] if the chain of transitions reaches [MAX_ITERATIONS].
     */
    private suspend fun transitionRecursively(stateContext: C) {
        var i = 0
        while (i < MAX_ITERATIONS) {
            val newState = _currentState.value.transition(stateContext) ?: break
            log.d(TAG, "Transitioned state to $newState")
            setState(newState)
            i++
        }
        if (i >= MAX_ITERATIONS) {
            throw IllegalStateException("Exceeded max transition iterations")
        }
    }

    private fun setState(newState: S, resetLog: Boolean = false) {
        _currentState.value = newState
        val newLogItem = StateLogItem(counter++, newState, timeSource.markNow())
        if (resetLog) {
            _stateLog.value = listOf(newLogItem)
        } else {
            _stateLog.update { it + newLogItem }
        }
    }

    override fun reset(stateContext: C, coroutineScope: CoroutineScope) {
        transition(stateContext, coroutineScope, resetLog = true) { initialState }
    }

    override fun cancel() {
        transitionJob?.cancel()
    }

    companion object {
        const val MAX_ITERATIONS = 30
        const val TAG = "ConversionStateContext"
    }
}

/**
 * An implementation of [StateMachine] that allows setting the state using a public method [setState].
 *
 * For testing purposes only.
 */
class FakeStateMachine<S : State<C>, C>(
    initialState: S,
) : StateMachine<S, C> {
    private var counter: Int = 0

    private var _currentState: MutableStateFlow<S> = MutableStateFlow(initialState)
    override val currentState: StateFlow<S?> = _currentState.asStateFlow()

    private var _stateLog: MutableStateFlow<List<StateLogItem<S>>> = MutableStateFlow(emptyList())
    override val stateLog: StateFlow<List<StateLogItem<S>>> = _stateLog.asStateFlow()

    override fun transition(
        stateContext: C,
        coroutineScope: CoroutineScope,
        resetLog: Boolean,
        newState: suspend () -> S,
    ) {
        throw NotImplementedError()
    }

    override fun reset(stateContext: C, coroutineScope: CoroutineScope) {
        throw NotImplementedError()
    }

    override fun cancel() {
        throw NotImplementedError()
    }

    fun setState(newState: S, start: ComparableTimeMark, resetLog: Boolean = false) {
        _currentState.value = newState
        val newLogItem = StateLogItem(counter++, newState, start)
        if (resetLog) {
            _stateLog.value = listOf(newLogItem)
        } else {
            _stateLog.update { it + newLogItem }
        }
    }
}
