package page.ooooo.geoshare.lib.state

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
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val output = SavePointsGpxOutput(coordinateConverter)
    private val action = output.toAction(points)
    private val actionStateContext: ActionStateContext = mock()

    @Test
    fun transition_whenAutomationIsFalseAndResultIsSucceededAndOutputHasSuccessText_returnsActionSucceeded() = runTest {
        for (actionResult in setOf(ActionResult.SUCCEEDED, ActionResult.SUCCEEDED_AND_OPENED_APP)) {
            val action = output.toAction(points)
            assertEquals(
                ActionSucceeded(actionResult, output),
                ActionRan(action, actionResult, isAutomation = false).transition(actionStateContext),
            )
        }
    }

    @Test
    fun transition_whenAutomationIsFalseAndResultIsSucceededAndOutputDoesNotHaveSuccessText_returnsActionCompleted() =
        runTest {
            for (actionResult in setOf(ActionResult.SUCCEEDED, ActionResult.SUCCEEDED_AND_OPENED_APP)) {
                assertEquals(
                    ActionCompleted(actionResult),
                    ActionRan(NoopAction, actionResult, isAutomation = false).transition(actionStateContext),
                )
            }
        }

    @Test
    fun transition_whenAutomationIsFalseAndResultIsFailedAndOutputHasErrorText_returnsActionFailed() = runTest {
        val actionResult = ActionResult.FAILED
        assertEquals(
            ActionFailed( output),
            ActionRan(action, actionResult, isAutomation = false).transition(actionStateContext),
        )
    }

    @Test
    fun transition_whenAutomationIsFalseAndResultIsFailedAndOutputDoesNotHaveErrorText_returnsActionCompleted() =
        runTest {
            val actionResult = ActionResult.FAILED
            assertEquals(
                ActionCompleted(actionResult),
                ActionRan(NoopAction, actionResult, isAutomation = false).transition(actionStateContext),
            )
        }

    @Test
    fun transition_whenAutomationIsTrueAndResultIsSucceededAndOutputHasAutomationSuccessText_returnsActionAutomationSucceeded() =
        runTest {
            for (actionResult in setOf(ActionResult.SUCCEEDED, ActionResult.SUCCEEDED_AND_OPENED_APP)) {
                assertEquals(
                    ActionAutomationSucceeded(actionResult, output),
                    ActionRan(action, actionResult, isAutomation = true).transition(actionStateContext),
                )
            }
        }

    @Test
    fun transition_whenAutomationIsTrueAndResultIsSucceededAndOutputDoesNotHaveAutomationSuccessText_returnsActionCompleted() =
        runTest {
            for (actionResult in setOf(ActionResult.SUCCEEDED, ActionResult.SUCCEEDED_AND_OPENED_APP)) {
                assertEquals(
                    ActionCompleted(actionResult),
                    ActionRan(NoopAction, actionResult, isAutomation = true).transition(actionStateContext),
                )
            }
        }

    @Test
    fun transition_whenAutomationIsTrueAndResultIsFailedAndOutputHasAutomationErrorText_returnsActionAutomationFailed() =
        runTest {
            val actionResult = ActionResult.FAILED
            assertEquals(
                ActionCompleted(actionResult),
                ActionRan(NoopAction, actionResult, isAutomation = true).transition(actionStateContext),
            )
        }

    @Test
    fun transition_whenAutomationIsTrueAndResultIsFailedAndOutputDoesNotHaveAutomationErrorText_returnsActionCompleted() =
        runTest {
            val actionResult = ActionResult.FAILED
            assertEquals(
                ActionCompleted(actionResult),
                ActionRan(NoopAction, actionResult, isAutomation = true).transition(actionStateContext),
            )
        }
}
