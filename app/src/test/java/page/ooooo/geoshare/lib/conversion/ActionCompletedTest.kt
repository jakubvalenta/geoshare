package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.outputs.ActionCompleted
import page.ooooo.geoshare.lib.outputs.ActionResult
import page.ooooo.geoshare.lib.outputs.ActionStateContext

class ActionCompletedTest {
    private val actionStateContext = ActionStateContext(FakeUserPreferencesRepository())

    @Test
    fun transition_returnsNull() = runTest {
        val state = ActionCompleted(ActionResult.SUCCEEDED_AND_OPENED_APP)
        assertNull(state.transition(actionStateContext))
    }
}
