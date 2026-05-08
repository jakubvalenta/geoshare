package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationReceivedTest {
    @Test
    fun locationReceived_locationIsNull_returnsLocationFindingFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action =
            OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter).toAction(points.last())
        val state = LocationReceived(inputUriString, points, action, isAutomation = false, location = null)
        assertEquals(
            LocationFindingFailed(inputUriString, points, action, isAutomation = false),
            state.transition(),
        )
    }

    @Test
    fun locationReceived_locationIsNotNull_returnsLocationActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action =
            OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter).toAction(points.last())
        val location = WGS84Point(3.0, 4.0, source = Source.GENERATED)
        val state = LocationReceived(inputUriString, points, action, isAutomation = false, location)
        assertEquals(
            LocationActionReady(inputUriString, points, action, isAutomation = false, location),
            state.transition(),
        )
    }

}
