package page.ooooo.geoshare.lib

import org.junit.Assert.*
import org.junit.Test

class RetryTest {
    @Test
    fun calcExponentialBackoffMillis() {
        for ((expectedMillis, attemptNumber) in mapOf(
            0 to 1,
            0 to 2,
            2 to 3,
            4 to 4,
            256 to 10,
        )) {
            assertEquals(expectedMillis, calcExponentialBackoffMillis(attemptNumber))
        }
    }

    @Test
    fun calcExponentialBackoffMillis_whenAttemptNumberIsNegative_returnsZero() {
        assertEquals(0, calcExponentialBackoffMillis(-1))
    }
}
