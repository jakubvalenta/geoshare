package page.ooooo.geoshare.lib

interface State {
    suspend fun transition(): State?
}

interface PermissionState {
    val permissionTitleResId: Int

    suspend fun grant(doNotAsk: Boolean): State
    suspend fun deny(doNotAsk: Boolean): State
}
