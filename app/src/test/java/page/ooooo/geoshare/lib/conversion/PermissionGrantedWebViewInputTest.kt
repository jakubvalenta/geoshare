package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import io.ktor.utils.io.CancellationException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.NextStep
import page.ooooo.geoshare.lib.inputs.ParseResult
import page.ooooo.geoshare.lib.inputs.WebViewInput
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionGrantedWebViewInputTest {
    private val log = FakeLog
    private val resources: Resources = mock {
        on { getString(R.string.converter_google_maps_loading_indicator_title) } doReturn "Connecting to Google..."
        on { getString(R.string.conversion_failed_cancelled) } doReturn "Cancelled"
        on { getString(R.string.conversion_failed_reason_timeout) } doReturn "timeout"
        on {
            getString(R.string.conversion_loading_indicator_description, 2, 10, "connection closed")
        } doReturn "Attempt 2 out of 10 due to connection closed."
    }
    private val source = "https://maps.google.com/foo"
    private val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val prevResult = ParseResult(prevPoints)
    private val nextInput = FakeInputRepository.debugUriInput
    private val uriQuote = FakeUriQuote

    @Test
    fun transition_whenSetDataIsCalled_returnsDataParsed() = runTest {
        val input = object : WebViewInput {
            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
            override val unsafeExtractionJavascript = "undefined"

            override suspend fun parse(
                data: String,
                match: String,
                prevResult: ParseResult?,
            ) = ParseResult(
                prevResult?.points ?: persistentListOf(),
                nextStep = NextStep(nextInput, data) // Store data in nextStep, so we can test it
            )
        }
        val permission = Permission.ALWAYS
        val timeout = 7.seconds
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.resources } doReturn resources
            on { this@on.uriQuote } doReturn uriQuote
        }
        val state = PermissionGrantedWebViewInput(
            stateContext, source, match = source, input, permission, prevResult, timeout, dispatcher = testScheduler
        )
        var res: State? = null
        launch {
            res = state.transition()
        }
        state.setData("${source}-data")
        advanceUntilIdle()
        assertEquals(
            DataParsed(
                stateContext,
                source,
                match = source,
                input,
                ParseResult(prevPoints, nextStep = NextStep(nextInput, "${source}-data")),
                permission,
                prevResult,
            ),
            res,
        )
    }

    @Test
    fun transition_whenSetDataIsNotCalledWithinTimeout_returnsConversionFailed() = runTest {
        val input = object : WebViewInput {
            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
            override val unsafeExtractionJavascript = "undefined"

            override suspend fun parse(
                data: String,
                match: String,
                prevResult: ParseResult?,
            ) = ParseResult(
                prevResult?.points ?: persistentListOf(),
                nextStep = NextStep(nextInput, data) // Store data in nextStep, so we can test it
            )
        }
        val permission = Permission.ALWAYS
        val timeout = 7.seconds
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.resources } doReturn resources
            on { this@on.uriQuote } doReturn uriQuote
        }
        val state = PermissionGrantedWebViewInput(
            stateContext, source, match = source, input, permission, prevResult, timeout, dispatcher = testScheduler
        )
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ConversionFailed(
                    source,
                    resources.getString(R.string.conversion_failed_reason_timeout),
                ),
                state.transition(),
            )
        }
        assertEquals(timeout, workDuration)
    }

    @Test
    fun transition_whenInputParseThrowsCancellationException_returnsConversionFailed() = runTest {
        val input = object : WebViewInput {
            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
            override val unsafeExtractionJavascript = "undefined"

            override suspend fun parse(
                data: String,
                match: String,
                prevResult: ParseResult?,
            ) = throw CancellationException()
        }
        val permission = Permission.ALWAYS
        val timeout = 7.seconds
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.resources } doReturn resources
            on { this@on.uriQuote } doReturn uriQuote
        }
        val state = PermissionGrantedWebViewInput(
            stateContext, source, match = source, input, permission, prevResult, timeout, dispatcher = testScheduler
        )
        var res: State? = null
        launch {
            res = state.transition()
        }
        state.setData("${source}-data")
        advanceUntilIdle()
        assertEquals(
            ConversionFailed(source, resources.getString(R.string.conversion_failed_cancelled)),
            res,
        )
    }

    @Test
    fun transition_whenItIsCancelled_returnsConversionFailed() = runTest {
        val input = object : WebViewInput {
            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
            override val unsafeExtractionJavascript = "undefined"

            override suspend fun parse(
                data: String,
                match: String,
                prevResult: ParseResult?,
            ) = ParseResult()
        }
        val permission = Permission.ALWAYS
        val timeout = 7.seconds
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.resources } doReturn resources
            on { this@on.uriQuote } doReturn uriQuote
        }
        val state = PermissionGrantedWebViewInput(
            stateContext, source, match = source, input, permission, prevResult, timeout, dispatcher = testScheduler
        )
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
            ConversionFailed(source, resources.getString(R.string.conversion_failed_cancelled)),
            res,
        )
    }

    @Test
    fun getLoadingIndicator_whenLastAttemptIsNull_returnsLargeLoadingIndicatorWithoutDescription() = runTest {
        val input = object : WebViewInput {
            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
            override val unsafeExtractionJavascript = "undefined"

            override suspend fun parse(
                data: String,
                match: String,
                prevResult: ParseResult?,
            ) = throw NotImplementedError()
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.resources } doReturn resources
        }
        val state = PermissionGrantedWebViewInput(
            stateContext, source, match = source, input, Permission.ALWAYS, dispatcher = testScheduler
        )
        assertEquals(
            LoadingIndicator.Large(
                title = resources.getString(R.string.converter_google_maps_loading_indicator_title),
            ),
            state.getLoadingIndicator(),
        )
    }
}
