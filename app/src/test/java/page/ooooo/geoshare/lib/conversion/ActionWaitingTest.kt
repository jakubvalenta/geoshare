package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.NoopAction
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class ActionWaitingTest {
    private val stateContext: ConversionStateContext = mock()

    @Test
    fun transition_whenExecutionIsNotCancelled_waitsAndReturnsActionReady() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = NoopAction
        val state = ActionWaiting(stateContext, source, points, action, isAutomation = true, 3.seconds)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionReady(source, points, action, isAutomation = true),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun transition_whenExecutionIsNotCancelledAndDelayIsNotPositive_doesNotWaitAndReturnsActionReady() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = NoopAction
        val state = ActionWaiting(stateContext, source, points, action, isAutomation = true, (-1).seconds)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionReady(source, points, action, isAutomation = true),
                state.transition(),
            )
        }
        assertEquals(0.seconds, workDuration)
    }

    @Test
    fun transition_whenExecutionIsCancelled_returnsActionFinished() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = NoopAction
        val state = ActionWaiting(stateContext, source, points, action, isAutomation = true, 3.seconds)
        var res: State? = null
        val job = launch {
            res = state.transition()
        }
        testScheduler.runCurrent()
        testScheduler.advanceTimeBy(1.seconds)
        try {
            job.cancelAndJoin()
        } catch (_: CancellationException) {
            // Do nothing
        }
        assertEquals(
            res,
            ActionFinished(source, points, action, isAutomation = true),
        )
    }
}
