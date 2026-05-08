package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationRationaleShownTest {
    @Test
    fun locationRationaleShown_grant_returnsLocationRationaleConfirmed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action =
            OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter).toAction(points.last())
        val state = LocationRationaleShown(inputUriString, points, action, isAutomation = false)
        assertEquals(
            LocationRationaleConfirmed(inputUriString, points, action, isAutomation = false),
            state.grant(false),
        )
    }

    @Test
    fun locationRationaleShown_deny_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action =
            OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter).toAction(points.last())
        val state = LocationRationaleShown(inputUriString, points, action, isAutomation = false)
        assertEquals(
            ActionFinished(inputUriString, points, action, isAutomation = false),
            state.deny(false),
        )
    }
}
