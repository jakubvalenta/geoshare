package page.ooooo.geoshare.data.state

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock

class ActivityFinishingRequestedTest {
    private val actionStateContext: ActionStateContext = mock()

    @Test
    fun transition_returnsNull() = runTest {
        val state = ActivityFinishingRequested
        assertNull(state.transition(actionStateContext))
    }
}
