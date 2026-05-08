package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationActionReadyTest {
    @Test
    fun locationActionReady_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(PackageNames.GOOGLE_MAPS, coordinateConverter).toAction(points.last())
        val state = LocationActionReady(
            inputUriString,
            points,
            action,
            isAutomation = false,
            WGS84Point(3.0, 4.0, source = Source.GENERATED)
        )
        assertNull(state.transition())
    }

}
