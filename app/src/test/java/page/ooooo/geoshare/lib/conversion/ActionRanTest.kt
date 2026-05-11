package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.ActionResult
import page.ooooo.geoshare.lib.outputs.NoopAction

class ActionRanTest {
    @Test
    fun transition_whenActionResultIsSucceededAndOutputDoesNotHaveAnySuccessText_returnsActionFinished() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = NoopAction
        val actionResult = ActionResult.Succeeded
        val state = ActionRan(source, points, action, actionResult, isAutomation = true)
        assertEquals(
            ActionFinished(source, points, actionResult),
            state.transition(),
        )
    }

    @Test
    fun transition_whenActionResultIsSucceededAndFinishAndOutputDoesNotHaveAnySuccessText_returnsActionFinished() =
        runTest {
            val source = "https://maps.google.com/foo"
            val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
            val action = NoopAction
            val actionResult = ActionResult.SucceededAndFinish
            val state = ActionRan(source, points, action, actionResult, isAutomation = true)
            assertEquals(
                ActionFinished(source, points, actionResult),
                state.transition(),
            )
        }

    @Test
    fun transition_whenActionResultIsFailedAndOutputDoesNotHaveAnyErrorText_returnsActionFinished() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = NoopAction
        val actionResult = ActionResult.Failed
        val state = ActionRan(source, points, action, actionResult, isAutomation = true)
        assertEquals(
            ActionFinished(source, points, actionResult),
            state.transition(),
        )
    }
}
