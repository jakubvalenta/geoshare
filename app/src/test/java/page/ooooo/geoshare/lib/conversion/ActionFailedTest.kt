package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.ActionResult
import page.ooooo.geoshare.lib.outputs.OpenDisplayGeoUriOutput
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class ActionFailedTest {
    private val coordinateConverter: CoordinateConverter = mock()

    @Test
    fun transition_whenExecutionIsNotCancelled_waitsAndReturnsActionFinished() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val output = OpenDisplayGeoUriOutput(PackageNames.OSMAND_PLUS, coordinateConverter)
        val actionResult = ActionResult.Failed
        val state = ActionFailed(source, points, actionResult, output)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionFinished(source, points, actionResult),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun transition_whenExecutionIsCancelled_returnsActionFinished() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val output = OpenDisplayGeoUriOutput(PackageNames.OSMAND_PLUS, coordinateConverter)
        val actionResult = ActionResult.Failed
        val state = ActionFailed(source, points, actionResult, output)
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
            ActionFinished(source, points, actionResult),
        )
    }
}
