package page.ooooo.geoshare.lib.conversion

class ActionWaitingTest {
    @Test
    fun actionWaiting_executionIsNotCancelled_waitsAndReturnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = SavePointsGpxOutput(coordinateConverter).toAction(points)
        val stateContext = mockStateContext()
        val state = ActionWaiting(stateContext, inputUriString, points, action, isAutomation = true, 3.seconds)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionReady(inputUriString, points, action, isAutomation = true),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun actionWaiting_delayIsNotPositive_doesNotWaitAndReturnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = SavePointsGpxOutput(coordinateConverter).toAction(points)
        val stateContext = mockStateContext()
        val state = ActionWaiting(stateContext, inputUriString, points, action, isAutomation = true, (-1).seconds)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionReady(inputUriString, points, action, isAutomation = true),
                state.transition(),
            )
        }
        assertEquals(0.seconds, workDuration)
    }

    @Test
    fun actionWaiting_executionIsCancelled_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = SavePointsGpxOutput(coordinateConverter).toAction(points)
        val stateContext = mockStateContext()
        val state = ActionWaiting(stateContext, inputUriString, points, action, isAutomation = true, 3.seconds)
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
