package page.ooooo.geoshare.lib.inputs

sealed interface FetchResult<T> {
    data class Success<T>(val data: T) : FetchResult<T>
    data class Failure<T>(val exception: Exception) : FetchResult<T>
}
