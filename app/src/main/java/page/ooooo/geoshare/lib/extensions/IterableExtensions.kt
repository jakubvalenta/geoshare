package page.ooooo.geoshare.lib.extensions

/**
 * A version of [zipWithNext] that adds one more pair to the result: last element, null.
 *
 * Example:
 *
 * ```
 * listOf(1, 2, 3).zipWithNextLastNull(transform)
 * // Returns [transform(1, 2), transform(2, 3), transform(3, null)]
 * ```
 */
inline fun <T, R> Iterable<T>.zipWithNextLastNull(transform: (a: T, b: T?) -> R): List<R> =
    zipWithNext(transform).let { newIterable ->
        lastOrNull()?.let { lastElement ->
            newIterable + transform(lastElement, null)
        } ?: newIterable
    }
