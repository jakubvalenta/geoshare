package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.SavePointsGpxOutput

class FileUriRequestedTest {
    private val coordinateConverter: CoordinateConverter = mock()
    private val source = "https://maps.apple.com/foo"
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))

    @Test
    fun transition_returnsNull() = runTest {
        val action = SavePointsGpxOutput(coordinateConverter).toAction(points)
        val state = FileUriRequested(source, points, action, isAutomation = false)
        assertNull(state.transition())
    }
}
