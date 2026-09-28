package page.ooooo.geoshare.lib.state

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.outputs.ActionResult

class ActionCompletedTest {
    private val actionStateContext: ActionStateContext = mock()

    @Test
    fun transition_returnsNull() = runTest {
        val state = ActionCompleted(ActionResult.SUCCEEDED_AND_OPENED_APP)
        assertNull(state.transition(actionStateContext))
    }
}
