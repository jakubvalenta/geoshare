package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationPermissionReceivedTest {
    @Test
    fun locationPermissionReceived_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action =
            OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter).toAction(points.last())
        val stateContext = mockStateContext()
        val state = LocationPermissionReceived(stateContext, inputUriString, points, action, isAutomation = false)
        assertNull(state.transition())
    }

    @Test
    fun locationPermissionReceived_getSmallLoadingIndicator_returnsLoadingIndicator() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action =
            OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter).toAction(points.last())
        val stateContext = mockStateContext()
        val state = LocationPermissionReceived(stateContext, inputUriString, points, action, isAutomation = false)
        assertEquals(
            LoadingIndicator.Small("Finding your location..."),
            state.getLoadingIndicator(),
        )
    }

}
