package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.inputs.NoopInput
import page.ooooo.geoshare.lib.inputs.ParseResult

class DataParsedTest {
    private val log = FakeLog
    private val resources: Resources = mock {
        on { getString(R.string.conversion_failed_connection_permission_denied) } doReturn "This link is not supported without connecting to the map service"
        on { getString(R.string.conversion_failed_reason_no_points) } doReturn "no points found"
        on { getString(R.string.conversion_failed_unsupported_source_place_list) } doReturn "Place lists are not supported"
    }
    private val source = "https://maps.google.com/foo"
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val input = FakeInputRepository.googleMapsShortLinkInput
    private val matchedInput = MatchedInput(input, source)
    private val stateContext: ConversionStateContext = mock {
        on { this@on.log } doReturn log
        on { this@on.resources } doReturn resources
    }

    @Test
    fun transition_whenLastPointHasCoordinates_returnsConversionSucceeded() = runTest {
        val results: Results = mapOf(
            matchedInput to ParseResult(points),
        )
        val state = DataParsed(stateContext, source, matchedInput, permission = null, results)
        assertEquals(
            ConversionSucceeded(stateContext, source, points),
            state.transition(),
        )
    }

    @Test
    fun transition_whenLastPointHasCoordinatesAndZoomAndNameAndThereAreOldResults_returnsConversionSucceeded() =
        runTest {
            val points = persistentListOf(WGS84Point(1.0, 2.0, z = 3.14, name = "foo", source = Source.GENERATED))
            val results: Results = mapOf(
                MatchedInput(
                    FakeInputRepository.debugUriInput,
                    "1"
                ) to ParseResult(persistentListOf(WGS84Point(name = "bar", source = Source.URI))),
                MatchedInput(
                    FakeInputRepository.debugUriInput,
                    "2"
                ) to ParseResult(persistentListOf(WGS84Point(z = 3.14, source = Source.HTML))),
                MatchedInput(FakeInputRepository.debugUriInput, "3") to ParseResult(
                    persistentListOf(
                        WGS84Point(
                            5.0,
                            6.0,
                            source = Source.JAVASCRIPT
                        )
                    )
                ),
                MatchedInput(FakeInputRepository.debugUriInput, "4") to ParseResult(),
                matchedInput to ParseResult(points),
            )
            val state = DataParsed(stateContext, source, matchedInput, permission = null, results)
            assertEquals(
                ConversionSucceeded(stateContext, source, points),
                state.transition(),
            )
        }

    @Test
    fun transition_whenLastPointHasCoordinatesAndNoZoomAndNoNameAndThereAreOldResults_returnsConversionSucceededWithMergedResult() =
        runTest {
            val results: Results = mapOf(
                MatchedInput(
                    FakeInputRepository.debugUriInput,
                    "1"
                ) to ParseResult(persistentListOf(WGS84Point(name = "bar", source = Source.URI))),
                MatchedInput(
                    FakeInputRepository.debugUriInput,
                    "2"
                ) to ParseResult(persistentListOf(WGS84Point(z = 3.14, source = Source.HTML))),
                MatchedInput(FakeInputRepository.debugUriInput, "3") to ParseResult(
                    persistentListOf(
                        WGS84Point(
                            5.0,
                            6.0,
                            source = Source.JAVASCRIPT
                        )
                    )
                ),
                MatchedInput(FakeInputRepository.debugUriInput, "4") to ParseResult(),
                matchedInput to ParseResult(points),
            )
            val state = DataParsed(stateContext, source, matchedInput, permission = null, results)
            assertEquals(
                ConversionSucceeded(
                    stateContext,
                    source,
                    persistentListOf(
                        WGS84Point(1.0, 2.0, z = 3.14, name = "bar", source = Source.GENERATED)
                    ),
                ),
                state.transition(),
            )
        }

    @Test
    fun transition_whenLastPointHasNoCoordinatesAndNextStepIsSet_returnsInputFound() = runTest {
        val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val next = MatchedInput(FakeInputRepository.googleMapsUriInput, source)
        val results: Results = mapOf(matchedInput to ParseResult(points, next))
        val permission = Permission.ALWAYS
        val state = DataParsed(stateContext, source, matchedInput, permission, results)
        assertEquals(
            InputMatched(stateContext, source, next, permission, results),
            state.transition(),
        )
    }

    @Test
    fun transition_whenLastPointHasNameOnly_returnsConversionSucceeded() = runTest {
        val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val results: Results = mapOf(matchedInput to ParseResult(points))
        val state = DataParsed(stateContext, source, matchedInput, permission = null, results)
        assertEquals(
            ConversionSucceeded(stateContext, source, points),
            state.transition(),
        )
    }

    @Test
    fun transition_whenLastPointIsEmpty_returnsConversionFailed() = runTest {
        val points = persistentListOf(WGS84Point(source = Source.GENERATED))
        val results: Results = mapOf(matchedInput to ParseResult(points))
        val state = DataParsed(stateContext, source, matchedInput, permission = null, results)
        assertEquals(
            ConversionFailed(
                source,
                resources.getString(R.string.conversion_failed_reason_no_points),
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPointsAreEmpty_returnsConversionFailed() = runTest {
        val points = persistentListOf<WGS84Point>()
        val results: Results = mapOf(matchedInput to ParseResult(points))
        val state = DataParsed(stateContext, source, matchedInput, permission = null, results)
        assertEquals(
            ConversionFailed(
                source,
                resources.getString(R.string.conversion_failed_reason_no_points),
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPointsAreEmptyAndOldResultIsEmpty_returnsConversionFailed() = runTest {
        val points = persistentListOf<WGS84Point>()
        val oldPoints = persistentListOf<WGS84Point>()
        val results: Results = mapOf(
            MatchedInput(FakeInputRepository.debugUriInput, "1") to ParseResult(oldPoints),
            matchedInput to ParseResult(points),
        )
        val state = DataParsed(stateContext, source, matchedInput, permission = null, results)
        assertEquals(
            ConversionFailed(
                source,
                resources.getString(R.string.conversion_failed_reason_no_points),
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPointsAreEmptyAndOldResultIsEmptyAndPermissionIsNever_returnsConversionFailed() = runTest {
        val points = persistentListOf<WGS84Point>()
        val oldPoints = persistentListOf<WGS84Point>()
        val results: Results = mapOf(
            MatchedInput(FakeInputRepository.debugUriInput, "1") to ParseResult(oldPoints),
            matchedInput to ParseResult(points),
        )
        val state = DataParsed(stateContext, source, matchedInput, permission = Permission.NEVER, results)
        assertEquals(
            ConversionFailed(
                source,
                resources.getString(R.string.conversion_failed_connection_permission_denied),
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPointsAreEmptyAndOldResultHasCoordinates_returnsConversionFailed() = runTest {
        val points = persistentListOf<WGS84Point>()
        val oldPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val results: Results = mapOf(
            MatchedInput(FakeInputRepository.debugUriInput, "1") to ParseResult(oldPoints),
            matchedInput to ParseResult(points),
        )
        val state = DataParsed(stateContext, source, matchedInput, permission = null, results)
        assertEquals(
            ConversionFailed(
                source,
                resources.getString(R.string.conversion_failed_reason_no_points),
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPointsAreEmptyAndOldResultHasNameOnly_returnsConversionSucceeded() = runTest {
        val points = persistentListOf<WGS84Point>()
        val oldPoints = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val results: Results = mapOf(
            MatchedInput(FakeInputRepository.debugUriInput, "1") to ParseResult(oldPoints),
            matchedInput to ParseResult(points),
        )
        val state = DataParsed(stateContext, source, matchedInput, permission = null, results)
        assertEquals(
            ConversionSucceeded(stateContext, source, oldPoints),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPointsAreEmptyAndOldResultHasNameOnlyAndNext_returnsConversionSucceededWithoutNext() =
        runTest {
            val points = persistentListOf<WGS84Point>()
            val oldPoints = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val oldNext = matchedInput
            val results: Results = mapOf(
                MatchedInput(FakeInputRepository.debugUriInput, "1") to ParseResult(oldPoints, oldNext),
                matchedInput to ParseResult(points),
            )
            val state = DataParsed(stateContext, source, matchedInput, permission = null, results)
            assertEquals(
                ConversionSucceeded(stateContext, source, oldPoints),
                state.transition(),
            )
        }

    @Test
    fun transition_whenPointsAreEmptyAndOldResultHasZoomOnly_returnsConversionFailed() = runTest {
        val points = persistentListOf<WGS84Point>()
        val oldPoints = persistentListOf(WGS84Point(z = 3.14, source = Source.GENERATED))
        val results: Results = mapOf(
            MatchedInput(FakeInputRepository.debugUriInput, "1") to ParseResult(oldPoints),
            matchedInput to ParseResult(points),
        )
        val state = DataParsed(stateContext, source, matchedInput, permission = null, results)
        assertEquals(
            ConversionFailed(
                source,
                resources.getString(R.string.conversion_failed_reason_no_points),
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenLastPointIsEmptyAndInputHasCustomErrorMessage_returnsConversionFailedWithCustomErrorMessage() =
        runTest {
            val input = object : NoopInput {
                override fun getErrorMessage(resources: Resources) =
                    resources.getString(R.string.conversion_failed_unsupported_source_place_list)
            }
            val matchedInput = MatchedInput(input, source)
            val points = persistentListOf(WGS84Point(source = Source.GENERATED))
            val results: Results = mapOf(matchedInput to ParseResult(points))
            val state = DataParsed(stateContext, source, matchedInput, permission = null, results)
            assertEquals(
                ConversionFailed(
                    source,
                    resources.getString(R.string.conversion_failed_unsupported_source_place_list),
                ),
                state.transition(),
            )
        }
}
