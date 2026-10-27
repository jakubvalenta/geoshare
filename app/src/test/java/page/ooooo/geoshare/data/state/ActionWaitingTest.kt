package page.ooooo.geoshare.data.state

import kotlinx.collections.immutable.persistentListOf
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
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.outputs.ActionResult
import page.ooooo.geoshare.data.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.data.uri.FakeUriQuote
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class ActionWaitingTest {
    private val coordinateConverter: CoordinateConverter = mock()
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val uriQuote = FakeUriQuote
    private val output = OpenDisplayGeoUriOutput(
        UriActivity(PackageNames.OSMAND_PLUS, UriScheme.GEO),
        coordinateConverter,
        uriQuote,
    )
    private val action = output.toAction(points.last())
    private val actionStateContext: ActionStateContext = mock()

    @Test
    fun transition_whenExecutionIsNotCancelled_waitsAndReturnsActionReady() = runTest {
        val state = ActionWaiting(action, output, isAutomation = true, 3.seconds)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionReady(action, isAutomation = true),
                state.transition(actionStateContext),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun transition_whenExecutionIsNotCancelledAndDelayIsNotPositive_doesNotWaitAndReturnsActionReady() = runTest {
        val state = ActionWaiting(action, output, isAutomation = true, (-1).seconds)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionReady(action, isAutomation = true),
                state.transition(actionStateContext),
            )
        }
        assertEquals(0.seconds, workDuration)
    }

    @Test
    fun transition_whenExecutionIsCancelled_returnsActionCompleted() = runTest {
        val state = ActionWaiting(action, output, isAutomation = true, 3.seconds)
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
            ActionCompleted(ActionResult.FAILED),
        )
    }
}
