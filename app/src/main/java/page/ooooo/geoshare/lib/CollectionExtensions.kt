package page.ooooo.geoshare.lib

fun <T, U> List<T>.lastNotNullOrNull(transform: (T) -> U?): U? {
    for (i in this.size - 1 downTo 0) {
        transform(this[i])?.let { value ->
            return@lastNotNullOrNull value
        }
    }
    return null
}
