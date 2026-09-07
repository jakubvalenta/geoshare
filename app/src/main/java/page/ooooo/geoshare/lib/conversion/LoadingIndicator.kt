package page.ooooo.geoshare.lib.conversion

// TODO Replace LoadingIndicator.Small with ConversionState.Something
sealed interface LoadingIndicator {
    data class Small(val message: String) : LoadingIndicator
}
