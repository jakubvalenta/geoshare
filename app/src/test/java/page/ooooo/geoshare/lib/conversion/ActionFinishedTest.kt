package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class ActionFinishedTest {
    @Test
    fun actionFinished_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenDisplayGeoUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter).toAction(points.last())
        val state = ActionFinished(inputUriString, points, action, isAutomation = true)
        assertNull(state.transition())
    }

}
