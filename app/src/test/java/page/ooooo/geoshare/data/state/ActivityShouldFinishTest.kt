package page.ooooo.geoshare.data.state

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock

class ActivityShouldFinishTest {
    private val actionStateContext: ActionStateContext = mock()

    @Test
    fun transition_returnsNull() = runTest {
        val state = ActivityShouldFinish
        assertNull(state.transition(actionStateContext))
    }
}
