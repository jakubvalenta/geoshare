package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class FileUriRequestedTest {
    @Test
    fun fileUriRequested_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = SavePointsGpxOutput(coordinateConverter).toAction(points)
        val state = FileUriRequested(inputUriString, points, action, isAutomation = false)
        assertNull(state.transition())
    }

}
