package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import io.ktor.utils.io.CancellationException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
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
        on {
            getString(R.string.conversion_failed_parse_html_error_with_reason, "timeout")
        } doReturn "Failed to process web page due to: timeout"
        on { getString(R.string.conversion_failed_reason_timeout) } doReturn "timeout"
        on {
            getString(R.string.conversion_loading_indicator_description, 2, 10, "connection closed")
        } doReturn "Attempt 2 out of 10 due to connection closed."
    }
    private val uriQuote = FakeUriQuote

    @Test
    fun transition_whenSetDataIsCalled_returnsDataParsed() = runTest {
        val source = "https://maps.google.com/foo"
        val input = object : WebViewInput {
            override suspend fun parse(data: String, prevPoints: Points?, uriQuote: UriQuote, log: ILog) =
                ParseResult(prevPoints ?: persistentListOf(), nextMatch = data)

            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
        }
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val permission = Permission.ALWAYS
        val timeout = 7.seconds
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.resources } doReturn resources
            on { this@on.uriQuote } doReturn uriQuote
        }
        val state = PermissionGrantedWebViewInput(
            stateContext,
            source,
            match = source,
            input,
            permission,
            prevPoints,
            timeout,
            dispatcher = testScheduler,
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
                ParseResult(prevPoints, nextMatch = "${source}-data"),
                permission,
                prevPoints,
            ),
            res,
        )
    }

    @Test
    fun transition_whenSetDataIsNotCalledWithinTimeout_returnsConversionFailed() = runTest {
        val source = "https://maps.google.com/foo"
        val input = object : WebViewInput {
            override suspend fun parse(data: String, prevPoints: Points?, uriQuote: UriQuote, log: ILog) =
                ParseResult(prevPoints ?: persistentListOf(), nextMatch = data)

            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
        }
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val permission = Permission.ALWAYS
        val timeout = 7.seconds
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.resources } doReturn resources
            on { this@on.uriQuote } doReturn uriQuote
        }
        val state = PermissionGrantedWebViewInput(
            stateContext,
            source,
            match = source,
            input,
            permission,
            prevPoints,
            timeout,
            dispatcher = testScheduler,
        )
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ConversionFailed(
                    resources.getString(
                        R.string.conversion_failed_parse_html_error_with_reason,
                        resources.getString(R.string.conversion_failed_reason_timeout),
                    ),
                    source,
                ),
                state.transition(),
            )
        }
        assertEquals(timeout, workDuration)
    }

    @Test
    fun transition_whenInputParseThrowsCancellationException_returnsConversionFailed() = runTest {
        val source = "https://maps.google.com/foo"
        val input = object : WebViewInput {
            override suspend fun parse(data: String, prevPoints: Points?, uriQuote: UriQuote, log: ILog) =
                throw CancellationException()

            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
        }
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val permission = Permission.ALWAYS
        val timeout = 7.seconds
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.resources } doReturn resources
            on { this@on.uriQuote } doReturn uriQuote
        }
        val state = PermissionGrantedWebViewInput(
            stateContext,
            source,
            match = source,
            input,
            permission,
            prevPoints,
            timeout,
            dispatcher = testScheduler,
        )
        var res: State? = null
        launch {
            res = state.transition()
        }
        state.setData("${source}-data")
        advanceUntilIdle()
        assertEquals(
            ConversionFailed(resources.getString(R.string.conversion_failed_cancelled), source),
            res,
        )
    }

    @Test
    fun transition_whenItIsCancelled_returnsConversionFailed() = runTest {
        val source = "https://maps.google.com/foo"
        val input = object : WebViewInput {
            override suspend fun parse(data: String, prevPoints: Points?, uriQuote: UriQuote, log: ILog) =
                ParseResult()

            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
        }
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val permission = Permission.ALWAYS
        val timeout = 7.seconds
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.resources } doReturn resources
            on { this@on.uriQuote } doReturn uriQuote
        }
        val state = PermissionGrantedWebViewInput(
            stateContext,
            source,
            match = source,
            input,
            permission,
            prevPoints,
            timeout,
            dispatcher = StandardTestDispatcher(testScheduler),
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
            ConversionFailed(resources.getString(R.string.conversion_failed_cancelled), source),
            res,
        )
    }
}
