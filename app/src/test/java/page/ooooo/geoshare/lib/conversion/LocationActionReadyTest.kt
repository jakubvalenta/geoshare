package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput

class LocationActionReadyTest {
    private val coordinateConverter: CoordinateConverter = mock()
    private val source = "https://maps.apple.com/foo"
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val action =
        OpenRouteOnePointGpxOutput(PackageNames.GOOGLE_MAPS, coordinateConverter).toAction(points.last())

    @Test
    fun transition_returnsNull() = runTest {
        val location = WGS84Point(3.0, 4.0, source = Source.GENERATED)
        val state = LocationActionReady(source, points, action, isAutomation = false, location)
        assertNull(state.transition())
    }
}
