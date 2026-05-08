package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class ActionReadyTest {
    @Test
    fun actionReady_actionIsCopyAutomation_returnsBasicActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = CopyCoordsDecOutput(coordinateConverter).toAction(points.last())
        val state = ActionReady(inputUriString, points, action, isAutomation = false)
        assertEquals(
            BasicActionReady(inputUriString, points, action, isAutomation = false),
            state.transition(),
        )
    }

    @Test
    fun actionReady_actionIsCopyAction_returnsBasicActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = CopyCoordsDecOutput(coordinateConverter).toAction(points.last())
        val state = ActionReady(inputUriString, points, action, isAutomation = true)
        assertEquals(
            BasicActionReady(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun actionReady_actionIsSaveGpxPoints_returnsFileUriRequested() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = SavePointsGpxOutput(coordinateConverter).toAction(points)
        val state = ActionReady(inputUriString, points, action, isAutomation = true)
        assertEquals(
            FileUriRequested(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun actionReady_actionIsShareGpxRouteAutomation_returnsLocationRationaleRequested() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(PackageNames.GOOGLE_MAPS, coordinateConverter).toAction(points.last())
        val state = ActionReady(inputUriString, points, action, isAutomation = true)
        assertEquals(
            LocationRationaleRequested(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun actionReady_actionIsShareGpxRouteAction_returnsLocationRationaleRequested() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(PackageNames.GOOGLE_MAPS, coordinateConverter).toAction(points.last())
        val state = ActionReady(inputUriString, points, action, isAutomation = false)
        assertEquals(
            LocationRationaleRequested(inputUriString, points, action, isAutomation = false),
            state.transition(),
        )
    }

}
