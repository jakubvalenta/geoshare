package page.ooooo.geoshare.lib

import org.junit.Assert.*
import org.junit.Test

class RetryTest {
    @Test
    fun calcExponentialBackoffMillis() {
        mapOf(
            0L to 1,
            1_000L to 2,
            2_000L to 3,
            4_000L to 4,
            256_000L to 10,
        ).run {
            assertEquals(
                keys.toList(),
                values.map { calcExponentialBackoffMillis(it) },
            )
        }
    }

    @Test
    fun calcExponentialBackoffMillis_whenAttemptNumberIsZero_returnsZero() {
        assertEquals(0, calcExponentialBackoffMillis(0))
    }

    @Test
    fun calcExponentialBackoffMillis_whenAttemptNumberIsNegative_returnsZero() {
        assertEquals(0, calcExponentialBackoffMillis(-1))
    }
}
