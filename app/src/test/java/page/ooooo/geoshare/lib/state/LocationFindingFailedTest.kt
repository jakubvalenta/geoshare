package page.ooooo.geoshare.lib.state

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.outputs.ActionResult
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class LocationFindingFailedTest {
    private val actionResult = ActionResult.FAILED
    private val actionStateContext = ActionStateContext(FakeUserPreferencesRepository())

    @Test
    fun locationFindingFailed_executionIsNotCancelled_waitsAndReturnsActionCompleted() = runTest {
        val state = LocationFindingFailed(actionResult)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionCompleted(actionResult),
                state.transition(actionStateContext),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun locationFindingFailed_executionIsCancelled_returnsActionCompleted() = runTest {
        val state = LocationFindingFailed(actionResult)
        var res: ActionState? = null
        val job = launch {
            res = state.transition(actionStateContext)
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
            ActionCompleted(actionResult),
        )
    }
}
