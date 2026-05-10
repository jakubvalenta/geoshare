package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class LocationFindingFailedTest {
    private val coordinateConverter: CoordinateConverter = mock()

    @Test
    fun locationFindingFailed_executionIsNotCancelled_waitsAndReturnsActionFinished() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter).toAction(points.last())
        val state = LocationFindingFailed(source, points, action, isAutomation = false)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionFinished(source, points, action, isAutomation = false),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun locationFindingFailed_executionIsCancelled_returnsActionFinished() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter).toAction(points.last())
        val state = LocationFindingFailed(source, points, action, isAutomation = false)
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
            ActionFinished(source, points, action, isAutomation = false),
        )
    }
}
