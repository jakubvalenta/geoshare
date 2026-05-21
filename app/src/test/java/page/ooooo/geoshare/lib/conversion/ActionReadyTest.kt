package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.CopyCoordsDecOutput
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput
import page.ooooo.geoshare.lib.outputs.SavePointsGpxOutput

class ActionReadyTest {
    private val coordinateConverter: CoordinateConverter = mock()
    private val source = "https://maps.google.com/foo"
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))

    @Test
    fun transition_whenActionIsCopyCoordsDec_returnsBasicActionReady() = runTest {
        val action = CopyCoordsDecOutput(coordinateConverter).toAction(points.last())
        val state = ActionReady(source, points, action, isAutomation = true)
        assertEquals(
            BasicActionReady(source, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun transition_whenActionIsSavePointsGpx_returnsFileUriRequested() = runTest {
        val action = SavePointsGpxOutput(coordinateConverter).toAction(points)
        val state = ActionReady(source, points, action, isAutomation = true)
        assertEquals(
            FileUriRequested(source, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun transition_whenActionIsOpenRouteOnePointGpx_returnsLocationRationaleRequested() = runTest {
        val action = OpenRouteOnePointGpxOutput(PackageNames.GOOGLE_MAPS, coordinateConverter).toAction(points.last())
        val state = ActionReady(source, points, action, isAutomation = true)
        assertEquals(
            LocationRationaleRequested(source, points, action, isAutomation = true),
            state.transition(),
        )
    }
}
