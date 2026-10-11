package page.ooooo.geoshare.data.state

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.outputs.SavePointsGpxOutput

class FileUriRequestedTest {
    private val coordinateConverter: CoordinateConverter = mock()
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val actionStateContext: ActionStateContext = mock()

    @Test
    fun transition_returnsNull() = runTest {
        val action = SavePointsGpxOutput(coordinateConverter).toAction(points)
        val state = FileUriRequested(action, isAutomation = false)
        assertNull(state.transition(actionStateContext))
    }
}
