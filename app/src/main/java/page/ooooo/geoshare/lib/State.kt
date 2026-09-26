package page.ooooo.geoshare.lib

interface State<C> {
    suspend fun transition(stateContext: C): State<C>? = null

    interface HasPermission<C> {
        suspend fun grant(stateContext: C, doNotAsk: Boolean): State<C>
        suspend fun deny(stateContext: C, doNotAsk: Boolean): State<C>
    }
}
