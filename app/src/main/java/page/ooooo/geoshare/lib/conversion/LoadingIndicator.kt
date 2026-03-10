package page.ooooo.geoshare.lib.conversion

sealed interface LoadingIndicator {
    data class Small(val message: String) : LoadingIndicator
    data class Large(val title: String, val description: String? = null) : LoadingIndicator
}
