package page.ooooo.geoshare.lib

import kotlin.math.pow
import kotlin.math.roundToLong

private const val EXPONENTIAL_DELAY_BASE = 2.0
private const val EXPONENTIAL_DELAY_BASE_DELAY_MILLIS = 1_000L

data class Attempt<T : Throwable>(val number: Int, val cause: T)

fun calcExponentialBackoffMillis(attemptNumber: Int = 1): Long =
    if (attemptNumber < 2) {
        0
    } else {
        (EXPONENTIAL_DELAY_BASE.pow(attemptNumber - 2) * EXPONENTIAL_DELAY_BASE_DELAY_MILLIS)
            .roundToLong()
    }
