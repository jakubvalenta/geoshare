package page.ooooo.geoshare.lib.conversion

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable

sealed interface LoadingIndicator {
    data class Small(
        @param:StringRes val messageResId: Int,
    ) : LoadingIndicator

    data class Large(
        @param:StringRes val titleResId: Int,
        val description: @Composable () -> String?,
    ) : LoadingIndicator
}
