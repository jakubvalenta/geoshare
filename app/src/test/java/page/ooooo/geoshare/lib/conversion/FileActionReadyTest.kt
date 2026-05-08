package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class FileActionReadyTest {
    @Test
    fun fileActionReady_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = SavePointsGpxOutput(coordinateConverter).toAction(points)
        val state = FileActionReady(inputUriString, points, action, isAutomation = true, mock())
        assertNull(state.transition())
    }

}
