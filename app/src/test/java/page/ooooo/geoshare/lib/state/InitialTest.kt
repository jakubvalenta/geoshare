package page.ooooo.geoshare.lib.state

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock

class InitialTest {
    private val stateContext: ConversionStateContext = mock()

    @Test
    fun initial_returnsNull() = runTest {
        val state = ConversionState.Initial
        assertNull(state.transition(stateContext))
    }
}
