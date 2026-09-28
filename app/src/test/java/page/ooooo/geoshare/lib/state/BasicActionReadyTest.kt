package page.ooooo.geoshare.lib.state

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.outputs.NoopAction

class BasicActionReadyTest {
    private val action = NoopAction
    private val actionStateContext: ActionStateContext = mock()

    @Test
    fun transition_returnsNull() = runTest {
        val state = BasicActionReady(action, isAutomation = true)
        assertNull(state.transition(actionStateContext))
    }
}
