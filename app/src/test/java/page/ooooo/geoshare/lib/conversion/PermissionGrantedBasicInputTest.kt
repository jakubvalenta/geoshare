package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.CancellationException
import kotlinx.collections.immutable.persistentListOf
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
import page.ooooo.geoshare.lib.inputs.BasicInput
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.inputs.NextStep
import page.ooooo.geoshare.lib.inputs.ParseResult
import page.ooooo.geoshare.lib.network.ConnectionClosedNetworkException
import page.ooooo.geoshare.lib.network.RecoverableNetworkException
import page.ooooo.geoshare.lib.network.ResponseNetworkException
import page.ooooo.geoshare.lib.network.SocketTimeoutNetworkException
import java.io.EOFException
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class PermissionGrantedBasicInputTest {
    private val log = FakeLog
    private val source = "https://maps.google.com/foo"
    private val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val prevResult = ParseResult(prevPoints)
    private val nextInput = FakeInputRepository.debugUriInput
    private val permission = Permission.ALWAYS
    private val maxAttempts = 3
    private val resources: Resources = mock {
        on { getString(R.string.converter_google_maps_loading_indicator_title) } doReturn "Connecting to Google..."
        on { getString(R.string.conversion_failed_cancelled) } doReturn "Cancelled"
        on { getString(R.string.conversion_failed_reason_invalid_url) } doReturn "invalid URL"
        on { getString(R.string.conversion_failed_reason_missing_header) } doReturn "missing HTTP header"
        on {
            getString(R.string.conversion_loading_indicator_description, 2, 10, "connection closed")
        } doReturn "Attempt 2 out of 10 due to connection closed."
        on { getString(R.string.network_exception_eof) } doReturn "connection closed"
        on {
            getString(R.string.network_exception_response_error, HttpStatusCode.NotFound.value)
        } doReturn "response error 404"
    }
    private val uriQuote = FakeUriQuote

    @Test
    fun transition_whenInputFetchDoesNotThrowException_returnsDataParsed() = runTest {
        val input = object : BasicInput<String>, Input.HasPermission {
            override suspend fun fetch(
                match: String,
                block: suspend (String) -> ParseResult,
            ): ParseResult = block("${match}-data")

            override suspend fun parse(
                data: String,
                match: String,
                prevResult: ParseResult?,
            ) = ParseResult(
                prevResult?.points ?: persistentListOf(),
                nextStep = NextStep(nextInput, data) // Store data in nextStep, so we can test it
            )

            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
        }
        val lastAttempt = null
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.resources } doReturn resources
            on { this@on.uriQuote } doReturn uriQuote
        }
        val state = PermissionGrantedBasicInput(
            stateContext,
            source,
            match = source,
            input,
            permission,
            prevResult,
            lastAttempt,
            maxAttempts,
            dispatcher = testScheduler,
        )
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
            state.transition(),
        )
    }

    @Test
    fun transition_whenInputFetchThrowsCancellationException_returnsConversionFailed() = runTest {
        val input = object : BasicInput<String>, Input.HasPermission {
            override suspend fun fetch(
                match: String,
                block: suspend (String) -> ParseResult,
            ): ParseResult = throw CancellationException()

            override suspend fun parse(
                data: String,
                match: String,
                prevResult: ParseResult?,
            ) = ParseResult(
                prevResult?.points ?: persistentListOf(),
                nextStep = NextStep(nextInput, data) // Store data in nextStep, so we can test it
            )

            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
        }
        val lastAttempt = null
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.resources } doReturn resources
            on { this@on.uriQuote } doReturn uriQuote
        }
        val state = PermissionGrantedBasicInput(
            stateContext,
            source,
            match = source,
            input,
            permission,
            prevResult,
            lastAttempt,
            maxAttempts,
            dispatcher = testScheduler,
        )
        assertEquals(
            ConversionFailed(source, resources.getString(R.string.conversion_failed_cancelled)),
            state.transition(),
        )
    }

    @Test
    fun transition_whenInputFetchThrowsMalformedURLException_returnsConversionFailed() = runTest {
        val input = object : BasicInput<String>, Input.HasPermission {
            override suspend fun fetch(
                match: String,
                block: suspend (String) -> ParseResult,
            ): ParseResult = throw MalformedURLException()

            override suspend fun parse(
                data: String,
                match: String,
                prevResult: ParseResult?,
            ) = ParseResult(
                prevResult?.points ?: persistentListOf(),
                nextStep = NextStep(nextInput, data) // Store data in nextStep, so we can test it
            )

            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
        }
        val lastAttempt = null
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.resources } doReturn resources
            on { this@on.uriQuote } doReturn uriQuote
        }
        val state = PermissionGrantedBasicInput(
            stateContext,
            source,
            match = source,
            input,
            permission,
            prevResult,
            lastAttempt,
            maxAttempts,
            dispatcher = testScheduler,
        )
        assertEquals(
            ConversionFailed(
                source,
                resources.getString(R.string.conversion_failed_reason_invalid_url),
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenInputFetchThrowsRecoverableNetworkExceptionAndLastAttemptIsNull_returnsPermissionGrantedBasicInput() =
        runTest {
            val cause = SocketTimeoutNetworkException(SocketTimeoutException())
            val input = object : BasicInput<String>, Input.HasPermission {
                override suspend fun fetch(
                    match: String,
                    block: suspend (String) -> ParseResult,
                ): ParseResult = throw cause

                override suspend fun parse(
                    data: String,
                    match: String,
                    prevResult: ParseResult?,
                ) = ParseResult(
                    prevResult?.points ?: persistentListOf(),
                    nextStep = NextStep(nextInput, data) // Store data in nextStep, so we can test it
                )

                override val permissionTitleResId = R.string.converter_google_maps_permission_title
                override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
            }
            val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
            val prevResult = ParseResult(prevPoints)
            val lastAttempt = null
            val permission = Permission.ALWAYS
            val stateContext: ConversionStateContext = mock {
                on { this@on.log } doReturn log
                on { this@on.resources } doReturn resources
                on { this@on.uriQuote } doReturn uriQuote
            }
            val state = PermissionGrantedBasicInput(
                stateContext,
                source,
                match = source,
                input,
                permission,
                prevResult,
                lastAttempt,
                maxAttempts,
                dispatcher = testScheduler,
            )
            val workDuration = testScheduler.timeSource.measureTime {
                assertEquals(
                    PermissionGrantedBasicInput(
                        stateContext,
                        source,
                        match = source,
                        input,
                        permission,
                        prevResult,
                        lastAttempt = Attempt(1, cause),
                        maxAttempts,
                    ),
                    state.transition(),
                )
            }
            assertEquals(0.seconds, workDuration)
        }

    @Test
    fun transition_whenInputFetchThrowsRecoverableNetworkExceptionAndLastAttemptIsOne_waitsAndReturnsPermissionGrantedBasicInput() =
        runTest {
            val cause = SocketTimeoutNetworkException(SocketTimeoutException())
            val input = object : BasicInput<String>, Input.HasPermission {
                override suspend fun fetch(
                    match: String,
                    block: suspend (String) -> ParseResult,
                ): ParseResult = throw cause

                override suspend fun parse(
                    data: String,
                    match: String,
                    prevResult: ParseResult?,
                ) = ParseResult(
                    prevResult?.points ?: persistentListOf(),
                    nextStep = NextStep(nextInput, data) // Store data in nextStep, so we can test it
                )

                override val permissionTitleResId = R.string.converter_google_maps_permission_title
                override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
            }
            val lastAttempt = Attempt<RecoverableNetworkException>(1, ConnectionClosedNetworkException(EOFException()))
            val stateContext: ConversionStateContext = mock {
                on { this@on.log } doReturn log
                on { this@on.resources } doReturn resources
                on { this@on.uriQuote } doReturn uriQuote
            }
            val state = PermissionGrantedBasicInput(
                stateContext,
                source,
                match = source,
                input,
                permission,
                prevResult,
                lastAttempt,
                maxAttempts,
                dispatcher = testScheduler,
            )
            val workDuration = testScheduler.timeSource.measureTime {
                assertEquals(
                    PermissionGrantedBasicInput(
                        stateContext,
                        source,
                        match = source,
                        input,
                        permission,
                        prevResult,
                        lastAttempt = Attempt(2, cause),
                        maxAttempts,
                    ),
                    state.transition(),
                )
            }
            assertEquals(1.seconds, workDuration)
        }

    @Test
    fun transition_whenInputFetchThrowsRecoverableNetworkExceptionAndLastAttemptIsMaxAttempts_returnsConversionFailed() =
        runTest {
            val cause = SocketTimeoutNetworkException(SocketTimeoutException())
            val input = object : BasicInput<String>, Input.HasPermission {
                override suspend fun fetch(
                    match: String,
                    block: suspend (String) -> ParseResult,
                ): ParseResult = throw cause

                override suspend fun parse(
                    data: String,
                    match: String,
                    prevResult: ParseResult?,
                ) = ParseResult(
                    prevResult?.points ?: persistentListOf(),
                    nextStep = NextStep(nextInput, data) // Store data in nextStep, so we can test it
                )

                override val permissionTitleResId = R.string.converter_google_maps_permission_title
                override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
            }
            val lastAttempt = Attempt<RecoverableNetworkException>(3, ConnectionClosedNetworkException(EOFException()))
            val stateContext: ConversionStateContext = mock {
                on { this@on.log } doReturn log
                on { this@on.resources } doReturn resources
                on { this@on.uriQuote } doReturn uriQuote
            }
            val state = PermissionGrantedBasicInput(
                stateContext,
                source,
                match = source,
                input,
                permission,
                prevResult,
                lastAttempt,
                maxAttempts,
                dispatcher = testScheduler,
            )
            val workDuration = testScheduler.timeSource.measureTime {
                assertEquals(
                    ConversionFailed(
                        source,
                        resources.getString(R.string.network_exception_eof),
                    ),
                    state.transition(),
                )
            }
            assertEquals(0.seconds, workDuration)
        }

    @Test
    fun transition_whenInputFetchThrowsUnrecoverableNetworkException_returnsConversionFailed() = runTest {
        val response: HttpResponse = mock {
            on { status } doReturn HttpStatusCode.NotFound
        }
        val cause = ResponseNetworkException(response, Exception())
        val input = object : BasicInput<String>, Input.HasPermission {
            override suspend fun fetch(
                match: String,
                block: suspend (String) -> ParseResult,
            ): ParseResult = throw cause

            override suspend fun parse(
                data: String,
                match: String,
                prevResult: ParseResult?,
            ) = ParseResult(
                prevResult?.points ?: persistentListOf(),
                nextStep = NextStep(nextInput, data) // Store data in nextStep, so we can test it
            )

            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
        }
        val lastAttempt = null
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.resources } doReturn resources
            on { this@on.uriQuote } doReturn uriQuote
        }
        val state = PermissionGrantedBasicInput(
            stateContext,
            source,
            match = source,
            input,
            permission,
            prevResult,
            lastAttempt,
            maxAttempts,
            dispatcher = testScheduler,
        )
        assertEquals(
            ConversionFailed(
                source,
                resources.getString(R.string.network_exception_response_error, HttpStatusCode.NotFound.value),
            ),
            state.transition(),
        )
    }

    @Test
    fun getLoadingIndicator_whenLastAttemptIsNull_returnsLargeLoadingIndicatorWithoutDescription() = runTest {
        val input = FakeInputRepository.googleMapsShortLinkInput
        val stateContext: ConversionStateContext = mock {
            on { this@on.resources } doReturn resources
        }
        val state = PermissionGrantedBasicInput(
            stateContext,
            source,
            match = source,
            input,
            Permission.ALWAYS,
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
        val input = FakeInputRepository.googleMapsShortLinkInput
        val lastAttempt = Attempt<RecoverableNetworkException>(1, ConnectionClosedNetworkException(EOFException()))
        val stateContext: ConversionStateContext = mock {
            on { this@on.resources } doReturn resources
        }
        val state = PermissionGrantedBasicInput(
            stateContext,
            source,
            match = source,
            input,
            Permission.ALWAYS,
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
