package page.ooooo.geoshare.lib

import androidx.compose.runtime.Immutable

@Immutable
data class Message(val text: String, val isError: Boolean = false)
