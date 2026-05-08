package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class BasicActionReadyTest {
    @Test
    fun basicActionReady_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = SharePointsGpxOutput(coordinateConverter).toAction(points)
        val state = BasicActionReady(inputUriString, points, action, isAutomation = true)
        assertNull(state.transition())
    }

}
