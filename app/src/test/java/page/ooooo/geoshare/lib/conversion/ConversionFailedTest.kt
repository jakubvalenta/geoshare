package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock

class ConversionFailedTest {
    private val source = "https://maps.google.com/foo"
    private val stateContext: ConversionStateContext = mock()

    @Test
    fun transition_returnsNull() = runTest {
        val state = ConversionFailed(source, "Test message")
        assertNull(state.transition(stateContext))
    }
}
