package page.ooooo.geoshare.data.state

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.android.UriActivity
import page.ooooo.geoshare.data.android.UriScheme
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.outputs.ActionResult
import page.ooooo.geoshare.data.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.data.uri.FakeUriQuote
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class ActionFailedTest {
    private val coordinateConverter: CoordinateConverter = mock()
    private val uriQuote = FakeUriQuote
    private val output = OpenDisplayGeoUriOutput(
        UriActivity(PackageNames.OSMAND_PLUS, UriScheme.GEO),
        coordinateConverter,
        uriQuote,
    )
    private val actionResult = ActionResult.FAILED
    private val actionStateContext: ActionStateContext = mock()

    @Test
    fun transition_whenExecutionIsNotCancelled_waitsAndReturnsActionCompleted() = runTest {
        val state = ActionFailed(output)
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
