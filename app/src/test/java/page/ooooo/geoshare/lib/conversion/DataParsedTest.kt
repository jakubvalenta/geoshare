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
import page.ooooo.geoshare.lib.inputs.NextStep
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
        val state = DataParsed(stateContext, source, match = source, GoogleMapsUriInput, result, permission = null)
        assertEquals(
            ConversionSucceeded(stateContext, source, points),
            state.transition(),
        )
    }

    @Test
    fun transition_whenLastPointHasNoCoordinatesAndNextStepIsSet_returnsInputFound() = runTest {
        val source = "https://maps.apple.com/foo"
        val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val nextStep = NextStep(GoogleMapsHtmlInput, "https://maps.apple.com/foo")
        val result = ParseResult(points, nextStep)
        val permission = Permission.ALWAYS
        val state = DataParsed(stateContext, source, match = source, GoogleMapsUriInput, result, permission)
        assertEquals(
            InputFound(stateContext, source, nextStep.match, nextStep.input, permission, prevResult = result),
            state.transition(),
        )
    }

    @Test
    fun transition_whenLastPointHasNameOnly_returnsConversionSucceeded() = runTest {
        val source = "https://maps.apple.com/foo"
        val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val result = ParseResult(points)
        val state = DataParsed(stateContext, source, match = source, GoogleMapsUriInput, result, permission = null)
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
        val state = DataParsed(stateContext, source, match = source, GoogleMapsUriInput, result, permission = null)
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
        val state = DataParsed(stateContext, source, match = source, GoogleMapsUriInput, result, permission = null)
        assertEquals(
            ConversionFailed(
                resources.getString(R.string.conversion_failed_reason_no_points),
                source,
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPointsAreEmptyAndPrevResultIsEmpty_returnsConversionFailed() = runTest {
        val source = "https://maps.apple.com/foo"
        val points = persistentListOf<WGS84Point>()
        val result = ParseResult(points)
        val prevPoints = persistentListOf<WGS84Point>()
        val prevResult = ParseResult(prevPoints)
        val state = DataParsed(
            stateContext, source, match = source, GoogleMapsUriInput, result, permission = null, prevResult
        )
        assertEquals(
            ConversionFailed(
                resources.getString(R.string.conversion_failed_reason_no_points),
                source,
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPointsAreEmptyAndPrevResultHasCoordinates_returnsConversionSucceeded() = runTest {
        val source = "https://maps.apple.com/foo"
        val points = persistentListOf<WGS84Point>()
        val result = ParseResult(points)
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val prevResult = ParseResult(prevPoints)
        val state = DataParsed(
            stateContext, source, match = source, GoogleMapsUriInput, result, permission = null, prevResult
        )
        assertEquals(
            ConversionSucceeded(stateContext, source, prevPoints),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPointsAreEmptyAndPrevResultHasNameOnly_returnsConversionSucceeded() = runTest {
        val source = "https://maps.apple.com/foo"
        val points = persistentListOf<WGS84Point>()
        val result = ParseResult(points)
        val prevPoints = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val prevResult = ParseResult(prevPoints)
        val state = DataParsed(
            stateContext, source, match = source, GoogleMapsUriInput, result, permission = null, prevResult
        )
        assertEquals(
            ConversionSucceeded(stateContext, source, prevPoints),
            state.transition(),
        )
    }
}
