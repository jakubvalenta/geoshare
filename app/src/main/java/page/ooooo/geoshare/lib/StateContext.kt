package page.ooooo.geoshare.lib

abstract class StateContext {
    private val maxIterations = 10

    abstract var currentState: State

    suspend fun transition() {
        var i = 0
        while (i < maxIterations) {
            currentState = currentState.transition() ?: break
            i++
        }
        if (i >= maxIterations) {
            throw Exception("Exceeded max state iterations")
        }
    }
}
