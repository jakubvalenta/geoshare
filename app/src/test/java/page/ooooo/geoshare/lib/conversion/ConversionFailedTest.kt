package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test

class ConversionFailedTest {
    private val source = "https://maps.google.com/foo"

    @Test
    fun returnsNull() = runTest {
        val state = ConversionFailed("Test message", source)
        assertNull(state.transition())
    }
}
