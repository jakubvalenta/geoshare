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
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput

class LocationRationaleShownTest {
    private val coordinateConverter: CoordinateConverter = mock()

    @Test
    fun grant_returnsLocationRationaleConfirmed() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter).toAction(points.last())
        val state = LocationRationaleShown(source, points, action, isAutomation = false)
        assertEquals(
            LocationRationaleConfirmed(source, points, action, isAutomation = false),
            state.grant(false),
        )
    }

    @Test
    fun deny_returnsActionFinished() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter).toAction(points.last())
        val state = LocationRationaleShown(source, points, action, isAutomation = false)
        assertEquals(
            ActionFinished(source, points, action, isAutomation = false),
            state.deny(false),
        )
    }
}
