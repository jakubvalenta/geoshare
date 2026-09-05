package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock

class InitialTest {
    private val stateContext: ConversionStateContext = mock()

    @Test
    fun initial_returnsNull() = runTest {
        val state = Initial
        assertNull(state.transition(stateContext))
    }
}
