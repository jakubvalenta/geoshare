package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.ActionResult
import page.ooooo.geoshare.lib.outputs.NoopAction
import page.ooooo.geoshare.lib.outputs.SavePointsGpxOutput

class ActionRanTest {
    private val coordinateConverter: CoordinateConverter = mock()
    private val source = "https://maps.google.com/foo"
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))

    @Test
    fun transition_whenAutomationIsFalseAndResultIsSucceeded_returnsActionSucceededOrFinished() = runTest {
        for (actionResult in setOf(ActionResult.Succeeded, ActionResult.SucceededAndFinish)) {
            val output = SavePointsGpxOutput(coordinateConverter)
            val action = output.toAction(points)
            assertEquals(
                ActionSucceeded(source, points, actionResult, output),
                ActionRan(source, points, action, actionResult, isAutomation = false).transition(),
            )
            assertEquals(
                ActionFinished(source, points, actionResult),
                ActionRan(source, points, NoopAction, actionResult, isAutomation = false).transition(),
            )
        }
    }

    @Test
    fun transition_whenAutomationIsFalseAndResultIsFailed_returnsActionFailedOrFinished() = runTest {
        val output = SavePointsGpxOutput(coordinateConverter)
        val action = output.toAction(points)
        val actionResult = ActionResult.Failed
        assertEquals(
            ActionFailed(source, points, actionResult, output),
            ActionRan(source, points, action, actionResult, isAutomation = false).transition(),
        )
        assertEquals(
            ActionFinished(source, points, actionResult),
            ActionRan(source, points, NoopAction, actionResult, isAutomation = false).transition(),
        )
    }

    @Test
    fun transition_whenAutomationIsTrueAndResultIsSucceeded_returnsActionAutomationSucceededOrFinished() = runTest {
        for (actionResult in setOf(ActionResult.Succeeded, ActionResult.SucceededAndFinish)) {
            val output = SavePointsGpxOutput(coordinateConverter)
            val action = output.toAction(points)
            assertEquals(
                ActionAutomationSucceeded(source, points, actionResult, output),
                ActionRan(source, points, action, actionResult, isAutomation = true).transition(),
            )
            assertEquals(
                ActionFinished(source, points, actionResult),
                ActionRan(source, points, NoopAction, actionResult, isAutomation = true).transition(),
            )
        }
    }

    @Test
    fun transition_whenAutomationIsTrueAndResultIsFailed_returnsActionAutomationFailedOrFinished() = runTest {
        val output = SavePointsGpxOutput(coordinateConverter)
        val action = output.toAction(points)
        val actionResult = ActionResult.Failed
        assertEquals(
            ActionAutomationFailed(source, points, actionResult, output),
            ActionRan(source, points, action, actionResult, isAutomation = true).transition(),
        )
        assertEquals(
            ActionFinished(source, points, actionResult),
            ActionRan(source, points, NoopAction, actionResult, isAutomation = true).transition(),
        )
    }
}
