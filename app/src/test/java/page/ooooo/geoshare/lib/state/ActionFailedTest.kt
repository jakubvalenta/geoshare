package page.ooooo.geoshare.lib.state

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.android.UriActivity
import page.ooooo.geoshare.lib.android.UriScheme
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.outputs.ActionResult
import page.ooooo.geoshare.lib.outputs.OpenDisplayGeoUriOutput
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class ActionFailedTest {
    private val coordinateConverter: CoordinateConverter = mock()
    private val output = OpenDisplayGeoUriOutput(
        UriActivity(PackageNames.OSMAND_PLUS, UriScheme.GEO),
        coordinateConverter,
    )
    private val actionResult = ActionResult.FAILED
    private val actionStateContext = ActionStateContext(FakeUserPreferencesRepository())

    @Test
    fun transition_whenExecutionIsNotCancelled_waitsAndReturnsActionCompleted() = runTest {
        val state = ActionFailed( output)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionCompleted(actionResult),
                state.transition(actionStateContext),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun transition_whenExecutionIsCancelled_returnsActionCompleted() = runTest {
        val state = ActionFailed(output)
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
