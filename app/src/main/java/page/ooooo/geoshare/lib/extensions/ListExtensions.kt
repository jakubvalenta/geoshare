package page.ooooo.geoshare.lib.extensions

fun <T> List<T>.forEachReversed(block: (T) -> Unit) {
    for (i in (this.size - 1) downTo 0) {
        block(this[i])
    }
}
