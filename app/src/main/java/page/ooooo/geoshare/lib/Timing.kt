package page.ooooo.geoshare.lib

@Suppress("unused")
fun <R> withMeasureTimeMillis(done: (millis: Long) -> Unit, block: () -> R): R {
    val start = System.currentTimeMillis()
    val res = block()
    done(System.currentTimeMillis() - start)
    return res
}
