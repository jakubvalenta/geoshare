package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test

class ConversionFailedTest {
    @Test
    fun returnsNull() = runTest {
        val source = "https://maps.google.com/foo"
        val state = ConversionFailed("Test message", source)
        assertNull(state.transition())
    }
}
