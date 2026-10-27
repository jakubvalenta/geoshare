package page.ooooo.geoshare.data.state

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.outputs.ActionResult
import page.ooooo.geoshare.data.outputs.BasicAction

class BasicActionReadyTest {
    private val context: Context = mock()

    @Test
    fun execute_executesActionAndReturnsActionRan() = runTest {
        val actionResult = ActionResult.SUCCEEDED
        val action: BasicAction.WithPoint = mock {
            on { execute(any()) } doReturn actionResult
        }
        val state = BasicActionReady(action, isAutomation = true)
        assertEquals(
            ActionRan(action, actionResult, isAutomation = true),
            state.execute(context),
        )
    }
}
