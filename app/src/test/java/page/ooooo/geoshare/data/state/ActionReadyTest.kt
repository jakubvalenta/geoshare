package page.ooooo.geoshare.data.state

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.android.FileActivity
import page.ooooo.geoshare.data.android.FileType
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.log.FakeLog
import page.ooooo.geoshare.data.outputs.CopyCoordsDecOutput
import page.ooooo.geoshare.data.outputs.OpenRouteOnePointGpxOutput
import page.ooooo.geoshare.data.outputs.SavePointsGpxOutput

class ActionReadyTest {
    private val coordinateConverter: CoordinateConverter = mock()
    private val log = FakeLog
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val actionStateContext: ActionStateContext = mock()

    @Test
    fun transition_whenActionIsCopyCoordsDec_returnsBasicActionReady() = runTest {
        val action = CopyCoordsDecOutput(coordinateConverter).toAction(points.last())
        val state = ActionReady(action, isAutomation = true)
        assertEquals(
            BasicActionReady(action, isAutomation = true),
            state.transition(actionStateContext),
        )
    }

    @Test
    fun transition_whenActionIsSavePointsGpx_returnsFileUriRequested() = runTest {
        val action = SavePointsGpxOutput(coordinateConverter).toAction(points)
        val state = ActionReady(action, isAutomation = true)
        assertEquals(
            FileUriRequested(action, isAutomation = true),
            state.transition(actionStateContext),
        )
    }

    @Test
    fun transition_whenActionIsOpenRouteOnePointGpx_returnsLocationRationaleRequested() = runTest {
        val output = OpenRouteOnePointGpxOutput(
            FileActivity(PackageNames.TOMTOM, FileType.GPX_ONE_POINT),
            coordinateConverter,
            log,
        )
        val action = output.toAction(points.last())
        val state = ActionReady(action, isAutomation = true)
        assertEquals(
            LocationRationaleRequested(action, isAutomation = true),
            state.transition(actionStateContext),
        )
    }
}
