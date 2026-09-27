package page.ooooo.geoshare.lib.state

interface State<C> {
    @Suppress("SameReturnValue")
    suspend fun transition(stateContext: C): State<C>? = null

    interface HasPermission<C> {
        suspend fun grant(stateContext: C, doNotAsk: Boolean): State<C>
        suspend fun deny(stateContext: C, doNotAsk: Boolean): State<C>
    }
}

// TODO Docstring
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
