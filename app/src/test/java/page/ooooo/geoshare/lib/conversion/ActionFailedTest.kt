package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class ActionFailedTest {
    @Test
    fun actionFailed_executionIsNotCancelled_waitsAndReturnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenDisplayGeoUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter).toAction(points.last())
        val state = ActionFailed(inputUriString, points, action, isAutomation = true)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionFinished(inputUriString, points, action, isAutomation = true),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun actionFailed_executionIsCancelled_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenDisplayGeoUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter).toAction(points.last())
        val state = ActionFailed(inputUriString, points, action, isAutomation = true)
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
            ActionFinished(inputUriString, points, action, isAutomation = true),
        )
    }

}
