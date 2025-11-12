package page.ooooo.geoshare.lib.conversion

abstract class StateContext {
    companion object {
        const val MAX_ITERATIONS = 30
    }

    abstract var currentState: State

    suspend fun transition() {
        var i = 0
        while (i < MAX_ITERATIONS) {
            currentState = currentState.transition() ?: break
            i++
        }
        if (i >= MAX_ITERATIONS) {
            throw IllegalStateException("Exceeded max state iterations")
        }
    }
}
