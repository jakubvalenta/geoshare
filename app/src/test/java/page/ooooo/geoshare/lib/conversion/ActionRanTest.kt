package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class ActionRanTest {
    @Test
    fun actionRan_successIsTrue_returnsActionSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = NoopAction
        val state = ActionRan(inputUriString, points, action, isAutomation = true, success = true)
        assertEquals(
            ActionSucceeded(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun actionRan_successIsFalse_returnsActionFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = NoopAction
        val state = ActionRan(inputUriString, points, action, isAutomation = true, success = false)
        assertEquals(
            ActionFailed(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun actionRan_successIsNull_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = NoopAction
        val state = ActionRan(inputUriString, points, action, isAutomation = true, success = null)
        assertEquals(
            ActionFinished(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
    }

}
