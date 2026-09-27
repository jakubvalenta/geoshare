package page.ooooo.geoshare.lib.state

interface State<C> {
    @Suppress("SameReturnValue")
    suspend fun transition(stateContext: C): State<C>? = null

    interface HasPermission<C> {
        suspend fun grant(stateContext: C, doNotAsk: Boolean): State<C>
        suspend fun deny(stateContext: C, doNotAsk: Boolean): State<C>
    }
}

/**
 * Transitions the state and then takes the resulting state and transitions it too and so on until a transition returns
 * null.
 *
 * Calls [onStateChange] with the new state after each transition.
 *
 * Throws [IllegalStateException] if the chain of transitions reaches [maxIterations].
 */
suspend fun <C> State<C>.transitionRecursively(
    stateContext: C,
    maxIterations: Int = 30,
    onStateChange: (newState: State<C>) -> Unit,
) {
    var currentState = this
    var i = 0
    while (i < maxIterations) {
        currentState = currentState.transition(stateContext) ?: break
        onStateChange(currentState)
        i++
    }
    if (i >= maxIterations) {
        throw IllegalStateException("Exceeded max transition iterations")
    }
}
