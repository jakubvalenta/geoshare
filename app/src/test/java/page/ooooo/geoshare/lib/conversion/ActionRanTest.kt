package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.NoopAction

class ActionRanTest {
    @Test
    fun transition_whenSuccessIsTrue_returnsActionSucceeded() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = NoopAction
        val state = ActionRan(source, points, action, isAutomation = true, success = true)
        assertEquals(
            ActionSucceeded(source, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun transition_whenSuccessIsFalse_returnsActionFailed() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = NoopAction
        val state = ActionRan(source, points, action, isAutomation = true, success = false)
        assertEquals(
            ActionFailed(source, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun transition_whenSuccessIsNull_returnsActionFinished() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = NoopAction
        val state = ActionRan(source, points, action, isAutomation = true, success = null)
        assertEquals(
            ActionFinished(source, points, action, isAutomation = true),
            state.transition(),
        )
    }
}
