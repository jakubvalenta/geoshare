package page.ooooo.geoshare.data.state

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.outputs.ActionResult
import page.ooooo.geoshare.data.outputs.FileAction

class FileActionReadyTest {
    private val context: Context = mock()

    @Test
    fun execute_executesActionAndReturnsActionRan() = runTest {
        val uri: Uri = mock()
        val actionResult = ActionResult.SUCCEEDED
        val action: FileAction.WithPoint = mock {
            on { execute(eq(uri), any()) } doReturn actionResult
        }
        val state = FileActionReady(action, isAutomation = true, uri = uri)
        assertEquals(
            ActionRan(action, actionResult, isAutomation = true),
            state.execute(context),
        )
    }
}
