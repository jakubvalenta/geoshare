package page.ooooo.geoshare.data.state

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.outputs.ActionResult
import page.ooooo.geoshare.data.outputs.LocationAction

class LocationActionReadyTest {
    private val context: Context = mock()

    @Test
    fun execute_executesActionAndReturnsActionRan() = runTest {
        val location = WGS84Point(3.0, 4.0, source = Source.GENERATED)
        val actionResult = ActionResult.SUCCEEDED
        val action: LocationAction.WithPoint = mock {
            on { execute(eq(location), any()) } doReturn actionResult
        }
        val state = LocationActionReady(action, isAutomation = false, location)
        assertEquals(
            ActionRan(action, actionResult, isAutomation = false),
            state.execute(context),
        )
    }
}
