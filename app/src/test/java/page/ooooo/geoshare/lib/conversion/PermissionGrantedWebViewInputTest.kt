package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import io.ktor.client.call.HttpClientCall
import io.ktor.client.request.HttpRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
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
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.inputs.ParseResult
import page.ooooo.geoshare.lib.inputs.WebViewInput
import page.ooooo.geoshare.lib.network.ConnectionClosedNetworkException
import page.ooooo.geoshare.lib.network.RecoverableNetworkException
import page.ooooo.geoshare.lib.network.ResponseNetworkException
import page.ooooo.geoshare.lib.network.WebViewNetworkException
import java.io.EOFException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionGrantedWebViewInputTest {
    private val log = FakeLog
    private val source = "https://maps.google.com/foo"
    private val input = object : WebViewInput {
        override val permissionTitleResId = R.string.converter_google_maps_permission_title
        override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
        override val timeout = 7.seconds
        override val unsafeExtractionJavascript = "undefined"

        override suspend fun parse(data: String, match: String) =
            result.copy(next = next.copy(match = data)) // Store data in MatchedInput, so we can test it
    }
    private val matchedInput = MatchedInput<WebViewInput>(input, source)
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val next = MatchedInput(FakeInputRepository.debugUriInput, source)
    private val result = ParseResult(points, next)
    private val oldPoints = persistentListOf(WGS84Point(3.0, 4.0, source = Source.GENERATED))
    private val oldResult = ParseResult(oldPoints)
    private val results: Results = mapOf(MatchedInput(FakeInputRepository.debugUriInput, source) to oldResult)
    private val permission = Permission.ALWAYS
    private val lastCause = ConnectionClosedNetworkException(EOFException())
    private val maxAttempts = 3
    private val resources: Resources = mock {
        on { getString(R.string.converter_google_maps_loading_indicator_title) } doReturn "Connecting to Google..."
        on { getString(R.string.conversion_failed_cancelled) } doReturn "Cancelled"
        on { getString(R.string.conversion_failed_reason_timeout) } doReturn "Timeout"
        on {
            getString(R.string.conversion_loading_indicator_description, 2, 10, "connection closed")
        } doReturn "Attempt 2 out of 10 due to: Connection closed"
        on { getString(R.string.network_exception_eof) } doReturn "Connection closed"
        on {
            getString(R.string.network_exception_response_error, HttpStatusCode.NotFound.value)
        } doReturn "Response error 404"
    }
    private val uriQuote = FakeUriQuote
    private val stateContext: ConversionStateContext = mock {
        on { this@on.log } doReturn log
        on { this@on.resources } doReturn resources
        on { this@on.uriQuote } doReturn uriQuote
    }

    @Test
    fun transition_whenPendingDataIsCompleted_returnsDataParsed() = runTest {
        val state = PermissionGrantedWebViewInput(
            stateContext, source, matchedInput, permission, results, dispatcher = testScheduler
        )
        var res: State? = null
        launch {
            res = state.transition()
        }
        state.pendingData.complete("$source-data")
        advanceUntilIdle()
        assertEquals(
            DataParsed(
                stateContext,
                source,
                matchedInput,
                permission,
                results + (matchedInput to result.copy(next = next.copy(match = "$source-data"))),
            ),
            res,
        )
    }

    @Test
    fun transition_whenPendingDataIsCompletedWithRecoverableNetworkExceptionAndLastAttemptIsNull_retries() = runTest {
        val cause = WebViewNetworkException()
        val state = PermissionGrantedWebViewInput(
            stateContext,
            source,
            matchedInput,
            permission,
            results,
            lastAttempt = null,
            maxAttempts,
            dispatcher = testScheduler,
        )
        var res: State? = null
        launch {
            res = state.transition()
        }
        state.pendingData.completeExceptionally(cause)
        advanceUntilIdle()
        assertEquals(
            PermissionGrantedWebViewInput(
                stateContext,
                source,
                matchedInput,
                permission,
                results,
                lastAttempt = Attempt(1, cause),
                maxAttempts,
            ),
            res,
        )
    }

    @Test
    fun transition_whenPendingDataIsCompletedWithRecoverableNetworkExceptionAndLastAttemptIsOne_retries() = runTest {
        val cause = WebViewNetworkException()
        val lastAttempt = Attempt<RecoverableNetworkException>(1, lastCause)
        val state = PermissionGrantedWebViewInput(
            stateContext,
            source,
            matchedInput,
            permission,
            results,
            lastAttempt,
            maxAttempts,
            dispatcher = testScheduler,
        )
        var res: State? = null
        launch {
            res = state.transition()
        }
        state.pendingData.completeExceptionally(cause)
        advanceUntilIdle()
        assertEquals(
            PermissionGrantedWebViewInput(
                stateContext,
                source,
                matchedInput,
                permission,
                results,
                lastAttempt = Attempt(2, cause),
                maxAttempts,
            ),
            res,
        )
    }

    @Test
    fun transition_whenPendingDataIsCompletedWithRecoverableNetworkExceptionAndLastAttemptIsMaxAttempts_returnsConversionFailed() =
        runTest {
            val lastAttempt = Attempt<RecoverableNetworkException>(3, lastCause)
            val state = PermissionGrantedWebViewInput(
                stateContext,
                source,
                matchedInput,
                permission,
                results,
                lastAttempt,
                maxAttempts,
                dispatcher = testScheduler,
            )
            assertEquals(
                ConversionFailed(
                    source,
                    resources.getString(R.string.network_exception_eof),
                ),
                state.transition(),
            )
        }

    @Test
    fun transition_whenPendingDataIsCompletedWithUnrecoverableNetworkException_returnsConversionFailed() = runTest {
        val requestUrl = "https://www.example.com/request"
        val request: HttpRequest = mock {
            on { url } doReturn Url(requestUrl)
        }
        val call: HttpClientCall = mock {
            on { this.request } doReturn request
        }
        val response: HttpResponse = mock {
            on { status } doReturn HttpStatusCode.NotFound
            on { this.call } doReturn call
        }
        val cause = ResponseNetworkException(response, Exception())
        val state = PermissionGrantedWebViewInput(
            stateContext,
            source,
            matchedInput,
            permission,
            results,
            lastAttempt = null,
            maxAttempts,
            dispatcher = testScheduler,
        )
        var res: State? = null
        launch {
            res = state.transition()
        }
        state.pendingData.completeExceptionally(cause)
        advanceUntilIdle()
        assertEquals(
            ConversionFailed(
                source,
                resources.getString(R.string.network_exception_response_error, HttpStatusCode.NotFound.value),
                details = "Request URL: $requestUrl",
            ),
            res,
        )
    }

    @Test
    fun transition_whenPendingDataIsNotCompletedWithinTimeout_returnsConversionFailed() = runTest {
        val state = PermissionGrantedWebViewInput(
            stateContext, source, matchedInput, permission, results, dispatcher = testScheduler
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
        assertEquals(input.timeout, workDuration)
    }

    @Test
    fun transition_whenInputParseThrowsCancellationException_returnsConversionFailed() = runTest {
        val input = object : WebViewInput {
            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
            override val timeout = 7.seconds
            override val unsafeExtractionJavascript = "undefined"

            override suspend fun parse(data: String, match: String) =
                throw CancellationException()
        }
        val matchedInput = MatchedInput<WebViewInput>(input, source)
        val state = PermissionGrantedWebViewInput(
            stateContext, source, matchedInput, permission, results, dispatcher = testScheduler
        )
        var res: State? = null
        launch {
            res = state.transition()
        }
        state.pendingData.complete("$source-data")
        advanceUntilIdle()
        assertEquals(
            ConversionFailed(source, resources.getString(R.string.conversion_failed_cancelled)),
            res,
        )
    }

    @Test
    fun transition_whenItIsCancelled_returnsConversionFailed() = runTest {
        val state = PermissionGrantedWebViewInput(
            stateContext, source, matchedInput, permission, results, dispatcher = testScheduler
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
        val state = PermissionGrantedWebViewInput(
            stateContext,
            source,
            matchedInput,
            permission,
            results,
            lastAttempt = null,
            dispatcher = testScheduler,
        )
        assertEquals(
            LoadingIndicator.Large(
                title = resources.getString(R.string.converter_google_maps_loading_indicator_title),
            ),
            state.getLoadingIndicator(),
        )
    }

    @Test
    fun getLoadingIndicator_whenLastAttemptNumberIsOne_returnsLargeLoadingIndicatorWithDescription() = runTest {
        val lastAttempt = Attempt<RecoverableNetworkException>(1, lastCause)
        val state = PermissionGrantedWebViewInput(
            stateContext,
            source,
            matchedInput,
            permission,
            results,
            lastAttempt = lastAttempt,
            dispatcher = testScheduler,
        )
        assertEquals(
            LoadingIndicator.Large(
                title = resources.getString(R.string.converter_google_maps_loading_indicator_title),
                description = resources.getString(
                    R.string.conversion_loading_indicator_description,
                    2,
                    10,
                    resources.getString(R.string.network_exception_eof),
                ),
            ),
            state.getLoadingIndicator(),
        )
    }
}
