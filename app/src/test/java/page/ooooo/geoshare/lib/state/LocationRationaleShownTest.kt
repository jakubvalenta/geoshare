package page.ooooo.geoshare.lib.state

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.android.FileActivity
import page.ooooo.geoshare.lib.android.FileType
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.ActionResult
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput

class LocationRationaleShownTest {
    private val coordinateConverter: CoordinateConverter = mock()
    private val log = FakeLog
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val output = OpenRouteOnePointGpxOutput(
        FileActivity(PackageNames.TOMTOM, FileType.GPX_ONE_POINT),
        coordinateConverter,
        log,
    )
    private val action = output.toAction(points.last())
    private val actionStateContext: ActionStateContext = mock()

    @Test
    fun grant_returnsLocationRationaleConfirmed() = runTest {
        val state = LocationRationaleShown(action, isAutomation = false)
        assertEquals(
            LocationRationaleConfirmed(action, isAutomation = false),
            state.grant(actionStateContext, false),
        )
    }

    @Test
    fun deny_returnsActionCompleted() = runTest {
        val state = LocationRationaleShown(action, isAutomation = false)
        assertEquals(
            ActionCompleted(ActionResult.FAILED),
            state.deny(actionStateContext, false),
        )
    }
}
