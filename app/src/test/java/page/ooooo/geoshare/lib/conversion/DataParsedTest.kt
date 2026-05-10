package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsUriInput
import page.ooooo.geoshare.lib.inputs.ParseResult

class DataParsedTest {
    private val log = FakeLog
    private val resources: Resources = mock {
        on { getString(R.string.conversion_failed_connection_permission_denied) } doReturn "This link is not supported without connecting to the map service"
        on { getString(R.string.conversion_failed_reason_no_points) } doReturn "no points found"
    }
    private val stateContext: ConversionStateContext = mock {
        on { this@on.log } doReturn log
        on { this@on.resources } doReturn resources
    }

    @Test
    fun transition_whenLastPointHasCoordinates_returnsConversionSucceeded() = runTest {
        val source = "https://maps.apple.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val result = ParseResult(points)
        val state = DataParsed(stateContext, source, match = source, GoogleMapsUriInput, result)
        assertEquals(
            ConversionSucceeded(stateContext, source, points),
            state.transition(),
        )
    }

    @Test
    fun transition_whenLastPointHasNoCoordinatesButHasNextInputAndNextMatch_returnsInputFound() = runTest {
        val source = "https://maps.apple.com/foo"
        val points = persistentListOf(WGS84Point(source = Source.GENERATED))
        val nextInput = GoogleMapsHtmlInput
        val nextMatch = "https://maps.apple.com/foo"
        val result = ParseResult(points, nextInput, nextMatch)
        val permission = Permission.ALWAYS
        val prevPoints = persistentListOf(WGS84Point(name = "Previous point", source = Source.GENERATED))
        val state = DataParsed(
            stateContext, source, match = source, GoogleMapsUriInput, result, permission, prevPoints
        )
        assertEquals(
            InputFound(stateContext, source, nextMatch, nextInput, permission, points),
            state.transition(),
        )
    }

    @Test
    fun transition_whenLastPointHasNoCoordinatesButHasNextInputAndNoNextMatch_returnsInputFound() = runTest {
        val source = "https://maps.apple.com/foo"
        val points = persistentListOf(WGS84Point(source = Source.GENERATED))
        val nextInput = GoogleMapsHtmlInput
        val result = ParseResult(points, nextInput)
        val permission = Permission.ALWAYS
        val prevPoints = persistentListOf(WGS84Point(name = "Previous point", source = Source.GENERATED))
        val state = DataParsed(
            stateContext, source, match = source, GoogleMapsUriInput, result, permission, prevPoints
        )
        assertEquals(
            InputFound(stateContext, source, match = source, nextInput, permission, points),
            state.transition(),
        )
    }

    @Test
    fun transition_whenLastPointHasNoCoordinatesButHasName_returnsConversionSucceeded() = runTest {
        val source = "https://maps.apple.com/foo"
        val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val result = ParseResult(points)
        val state = DataParsed(stateContext, source, match = source, GoogleMapsUriInput, result)
        assertEquals(
            ConversionSucceeded(stateContext, source, points),
            state.transition(),
        )
    }

    @Test
    fun transition_whenLastPointIsEmpty_returnsConversionFailed() = runTest {
        val source = "https://maps.apple.com/foo"
        val points = persistentListOf(WGS84Point(source = Source.GENERATED))
        val result = ParseResult(points)
        val state = DataParsed(stateContext, source, match = source, GoogleMapsUriInput, result)
        assertEquals(
            ConversionFailed(
                resources.getString(R.string.conversion_failed_reason_no_points),
                source,
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPointsAreEmpty_returnsConversionFailed() = runTest {
        val source = "https://maps.apple.com/foo"
        val points = persistentListOf<WGS84Point>()
        val result = ParseResult(points)
        val state = DataParsed(stateContext, source, match = source, GoogleMapsUriInput, result)
        assertEquals(
            ConversionFailed(
                resources.getString(R.string.conversion_failed_reason_no_points),
                source,
            ),
            state.transition(),
        )
    }
}
