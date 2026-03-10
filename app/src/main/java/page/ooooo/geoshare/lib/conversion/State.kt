package page.ooooo.geoshare.lib.conversion

interface State {
    suspend fun transition(): State?
}
