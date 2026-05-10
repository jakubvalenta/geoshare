package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput

class LocationPermissionReceivedTest {
    private val coordinateConverter: CoordinateConverter = mock()
    private val resources: Resources = mock {
        on { getString(R.string.conversion_succeeded_location_loading_indicator_title) } doReturn "Finding your location..."
    }
    private val stateContext: ConversionStateContext = mock {
        on { this@on.resources } doReturn resources
    }

    @Test
    fun transition_returnsNull() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter).toAction(points.last())
        val state = LocationPermissionReceived(stateContext, source, points, action, isAutomation = false)
        assertNull(state.transition())
    }

    @Test
    fun getLoadingIndicator_returnsSmallLoadingIndicator() = runTest {
        val source = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(PackageNames.TOMTOM, coordinateConverter).toAction(points.last())
        val state = LocationPermissionReceived(stateContext, source, points, action, isAutomation = false)
        assertEquals(
            LoadingIndicator.Small(
                resources.getString(R.string.conversion_succeeded_location_loading_indicator_title)
            ),
            state.getLoadingIndicator(),
        )
    }
}
