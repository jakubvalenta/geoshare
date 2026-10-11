package page.ooooo.geoshare.data.state

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.log.FakeLog
import page.ooooo.geoshare.data.android.FileActivity
import page.ooooo.geoshare.data.android.FileType
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.outputs.ActionResult
import page.ooooo.geoshare.data.outputs.OpenRouteOnePointGpxOutput

class LocationReceivedTest {
    private val coordinateConverter: CoordinateConverter = mock()
    private val log = FakeLog
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val output = OpenRouteOnePointGpxOutput(
        FileActivity(PackageNames.TOMTOM, FileType.GPX_ONE_POINT),
        coordinateConverter,
        log,
    )
    private val action = output.toAction(points.last())
    private val actionStateContext: ActionStateContext = mock()

    @Test
    fun transition_whenLocationIsNull_returnsLocationFindingFailed() = runTest {
        val state = LocationReceived(action, isAutomation = false, location = null)
        assertEquals(
            LocationFindingFailed( ActionResult.FAILED),
            state.transition(actionStateContext),
        )
    }

    @Test
    fun transition_whenLocationIsNotNull_returnsLocationActionReady() = runTest {
        val location = WGS84Point(3.0, 4.0, source = Source.GENERATED)
        val state = LocationReceived(action, isAutomation = false, location)
        assertEquals(
            LocationActionReady(action, isAutomation = false, location),
            state.transition(actionStateContext),
        )
    }
}
