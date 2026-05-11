package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.ActionResult
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput

class LocationReceivedTest {
    private val coordinateConverter: CoordinateConverter = mock()

    @Test
    fun transition_whenLocationIsNull_returnsLocationFindingFailed() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter).toAction(points.last())
        val state = LocationReceived(source, points, action, isAutomation = false, location = null)
        assertEquals(
            LocationFindingFailed(source, points, ActionResult.Failed),
            state.transition(),
        )
    }

    @Test
    fun transition_whenLocationIsNotNull_returnsLocationActionReady() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter).toAction(points.last())
        val location = WGS84Point(3.0, 4.0, source = Source.GENERATED)
        val state = LocationReceived(source, points, action, isAutomation = false, location)
        assertEquals(
            LocationActionReady(source, points, action, isAutomation = false, location),
            state.transition(),
        )
    }
}
