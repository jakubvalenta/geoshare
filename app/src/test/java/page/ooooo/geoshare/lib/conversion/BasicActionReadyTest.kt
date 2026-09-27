package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.outputs.ActionStateContext
import page.ooooo.geoshare.lib.outputs.BasicActionReady
import page.ooooo.geoshare.lib.outputs.NoopAction

class BasicActionReadyTest {
    private val action = NoopAction
    private val actionStateContext = ActionStateContext(FakeUserPreferencesRepository())

    @Test
    fun transition_returnsNull() = runTest {
        val state = BasicActionReady(action, isAutomation = true)
        assertNull(state.transition(actionStateContext))
    }
}
