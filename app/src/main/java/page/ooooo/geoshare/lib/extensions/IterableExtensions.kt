package page.ooooo.geoshare.lib.extensions

fun <T> Iterable<T>.chunkedPairs(): List<Pair<T, T>> =
    chunked(2)
        .mapNotNull { chunk ->
            if (chunk.size == 2) {
                chunk.first() to chunk.last()
            } else {
                null
            }
        }

fun <T> Iterable<T>.removeRepeated(equals: (a: T?, b: T) -> Boolean = { a, b -> a == b }): List<T> =
    zipWithNextFirstNull { prev, curr -> prev to curr }
        .mapNotNull { (prev, curr) -> curr.takeIf { !equals(prev, curr) } }

/**
 * A version of [zipWithNext] that prepends one more pair to the result: null, first element.
 *
 * Example:
 *
 * ```
 * listOf(1, 2, 3).zipWithNextLastNull(transform)
 * // Returns [transform(null, 1), transform(1, 2), transform(2, 3)]
 * ```
 */
inline fun <T, R> Iterable<T>.zipWithNextFirstNull(transform: (a: T?, b: T) -> R): List<R> =
    listOfNotNull(firstOrNull()?.let { transform(null, it) }) + zipWithNext(transform)

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
    zipWithNext(transform) + listOfNotNull(lastOrNull()?.let { transform(it, null) })
