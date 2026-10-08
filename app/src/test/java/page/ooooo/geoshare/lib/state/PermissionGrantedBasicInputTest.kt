package page.ooooo.geoshare.lib.state

import android.content.res.Resources
import io.ktor.client.call.HttpClientCall
import io.ktor.client.request.HttpRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.utils.io.CancellationException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.BasicInput
import page.ooooo.geoshare.lib.inputs.BasicOfflineInput
import page.ooooo.geoshare.lib.inputs.BasicOnlineInput
import page.ooooo.geoshare.lib.inputs.InputGroup
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.inputs.ParseResult
import page.ooooo.geoshare.lib.network.ConnectionClosedNetworkException
import page.ooooo.geoshare.lib.network.FetchTools
import page.ooooo.geoshare.lib.network.RecoverableNetworkException
import page.ooooo.geoshare.lib.network.ResponseNetworkException
import page.ooooo.geoshare.lib.network.SocketTimeoutNetworkException
import page.ooooo.geoshare.lib.uri.FakeUriQuote
import java.io.EOFException
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class PermissionGrantedBasicInputTest {
    private val log = FakeLog
    private val source = "https://maps.google.com/foo"
    private val input = object : BasicOfflineInput {
        override fun getName(resources: Resources) = "Test Input"
        override val group = InputGroup.DEBUG

        override fun parse(match: String, resources: Resources) =
            result
    }
    private val matchedInput = MatchedInput<BasicInput>(input, source)
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val next = MatchedInput(FakeInputRepository.debugUriInput, source)
    private val result = ParseResult.Success(points, next)
    private val oldPoints = persistentListOf(WGS84Point(3.0, 4.0, source = Source.GENERATED))
    private val oldResult = ParseResult.Success(oldPoints)
    private val results: Results = mapOf(MatchedInput(FakeInputRepository.debugUriInput, source) to oldResult)
    private val permission = Permission.ALWAYS
    private val lastCause = ConnectionClosedNetworkException(EOFException())
    private val maxAttempts = 3
    private val resources: Resources = mock {
        on { getString(R.string.conversion_failed_unsupported_source_place_list) } doReturn "Place lists are not supported"
        on { getString(R.string.conversion_failed_cancelled) } doReturn "Cancelled"
        on { getString(R.string.conversion_failed_reason_invalid_url) } doReturn "Invalid URL"
        on { getString(R.string.conversion_failed_reason_missing_header) } doReturn "missing HTTP header"
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
    fun transition_whenInputParseSucceedsAndInputIsOfflineResultIsSuccess_returnsDataParsed() = runTest {
        val state = PermissionGrantedBasicInput(
            source,
            matchedInput,
            permission,
            results,
            lastAttempt = null,
            maxAttempts,
            dispatcher = testScheduler,
        )
        assertEquals(
            DataParsed(
                source,
                matchedInput,
                permission,
                results + (matchedInput to result),
            ),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenInputParseSucceedsAndResultIsOnlineSuccess_returnsDataParsed() = runTest {
        val input = object : BasicOnlineInput {
            override fun getName(resources: Resources) = "Test Input"
            override val group = InputGroup.DEBUG

            override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
                result
        }
        val matchedInput = MatchedInput<BasicInput>(input, source)
        val state = PermissionGrantedBasicInput(
            source,
            matchedInput,
            permission,
            results,
            lastAttempt = null,
            maxAttempts,
            dispatcher = testScheduler,
        )
        assertEquals(
            DataParsed(
                source,
                matchedInput,
                permission,
                results + (matchedInput to result),
            ),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenInputParseSucceedsAndResultIsWarning_returnsConversionFailed() =
        runTest {
            val input = object : BasicOfflineInput {
                override fun getName(resources: Resources) = "Test Input"
                override val group = InputGroup.DEBUG

                override fun parse(match: String, resources: Resources) =
                    ParseResult.Warning(
                        resources.getString(R.string.conversion_failed_unsupported_source_place_list)
                    )
            }
            val matchedInput = MatchedInput<BasicInput>(input, source)
            val state = PermissionGrantedBasicInput(
                source,
                matchedInput,
                permission,
                results,
                lastAttempt = null,
                maxAttempts,
                dispatcher = testScheduler,
            )
            assertEquals(
                ConversionFailed(
                    source,
                    resources.getString(R.string.conversion_failed_unsupported_source_place_list),
                    warning = true,
                ),
                state.transition(stateContext),
            )
        }

    @Test
    fun transition_whenInputParseThrowsCancellationException_returnsConversionFailed() = runTest {
        val input = object : BasicOnlineInput {
            override fun getName(resources: Resources) = "Test Input"
            override val group = InputGroup.DEBUG

            override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
                throw CancellationException()
        }
        val matchedInput = MatchedInput<BasicInput>(input, source)
        val state = PermissionGrantedBasicInput(
            source,
            matchedInput,
            permission,
            results,
            lastAttempt = null,
            maxAttempts,
            dispatcher = testScheduler,
        )
        assertEquals(
            ConversionFailed(source, resources.getString(R.string.conversion_failed_cancelled)),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenInputParseThrowsMalformedURLException_returnsConversionFailed() = runTest {
        val input = object : BasicOfflineInput {
            override fun getName(resources: Resources) = "Test Input"
            override val group = InputGroup.DEBUG

            override fun parse(match: String, resources: Resources) =
                throw MalformedURLException()
        }
        val matchedInput = MatchedInput<BasicInput>(input, source)
        val state = PermissionGrantedBasicInput(
            source,
            matchedInput,
            permission,
            results,
            lastAttempt = null,
            maxAttempts,
            dispatcher = testScheduler,
        )
        assertEquals(
            ConversionFailed(
                source,
                resources.getString(R.string.conversion_failed_reason_invalid_url),
            ),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenInputParseThrowsRecoverableNetworkExceptionAndLastAttemptIsNull_retries() = runTest {
        val cause = SocketTimeoutNetworkException(SocketTimeoutException())
        val input = object : BasicOnlineInput {
            override fun getName(resources: Resources) = "Test Input"
            override val group = InputGroup.DEBUG

            override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
                throw cause
        }
        val matchedInput = MatchedInput<BasicInput>(input, source)
        val state = PermissionGrantedBasicInput(
            source,
            matchedInput,
            permission,
            results,
            lastAttempt = null,
            maxAttempts,
            dispatcher = testScheduler,
        )
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                PermissionGrantedBasicInput(
                    source,
                    matchedInput,
                    permission,
                    results,
                    lastAttempt = Attempt(1, cause),
                    maxAttempts,
                ),
                state.transition(stateContext),
            )
        }
        assertEquals(0.seconds, workDuration)
    }

    @Test
    fun transition_whenInputParseThrowsRecoverableNetworkExceptionAndLastAttemptIsOne_waitsAndRetries() = runTest {
        val cause = SocketTimeoutNetworkException(SocketTimeoutException())
        val input = object : BasicOnlineInput {
            override fun getName(resources: Resources) = "Test Input"
            override val group = InputGroup.DEBUG

            override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
                throw cause
        }
        val matchedInput = MatchedInput<BasicInput>(input, source)
        val lastAttempt = Attempt<RecoverableNetworkException>(1, lastCause)
        val state = PermissionGrantedBasicInput(
            source,
            matchedInput,
            permission,
            results,
            lastAttempt,
            maxAttempts,
            dispatcher = testScheduler,
        )
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                PermissionGrantedBasicInput(
                    source,
                    matchedInput,
                    permission,
                    results,
                    lastAttempt = Attempt(2, cause),
                    maxAttempts,
                ),
                state.transition(stateContext),
            )
        }
        assertEquals(1.seconds, workDuration)
    }

    @Test
    fun transition_whenInputParseThrowsRecoverableNetworkExceptionAndLastAttemptIsMaxAttempts_returnsConversionFailed() =
        runTest {
            val input = object : BasicOfflineInput {
                override fun getName(resources: Resources) = "Test Input"
                override val group = InputGroup.DEBUG

                override fun parse(match: String, resources: Resources) =
                    throw NotImplementedError()
            }
            val matchedInput = MatchedInput<BasicInput>(input, source)
            val lastAttempt = Attempt<RecoverableNetworkException>(3, lastCause)
            val state = PermissionGrantedBasicInput(
                source,
                matchedInput,
                permission,
                results,
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
                    state.transition(stateContext),
                )
            }
            assertEquals(0.seconds, workDuration)
        }

    @Test
    fun transition_whenInputParseThrowsUnrecoverableNetworkException_returnsConversionFailed() = runTest {
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
        val input = object : BasicOnlineInput {
            override fun getName(resources: Resources) = "Test Input"
            override val group = InputGroup.DEBUG

            override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
                throw cause
        }
        val matchedInput = MatchedInput<BasicInput>(input, source)
        val lastAttempt = null
        val state = PermissionGrantedBasicInput(
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
                resources.getString(R.string.network_exception_response_error, HttpStatusCode.NotFound.value),
                stackTrace = "Request URL: $requestUrl",
            ),
            state.transition(stateContext),
        )
    }

    @Test
    fun getDetails_whenLastAttemptIsNull_returnsNull() = runTest {
        val state = PermissionGrantedBasicInput(
            source,
            matchedInput,
            permission,
            results,
            lastAttempt = null,
            dispatcher = testScheduler,
        )
        assertNull(state.getDetails(resources))
    }

    @Test
    fun getDetails_whenLastAttemptNumberIsOne_returnsDetails() = runTest {
        val lastAttempt = Attempt<RecoverableNetworkException>(1, lastCause)
        val state = PermissionGrantedBasicInput(
            source,
            matchedInput,
            permission,
            results,
            lastAttempt = lastAttempt,
            dispatcher = testScheduler,
        )
        assertEquals(
            resources.getString(
                R.string.conversion_loading_indicator_description,
                2,
                10,
                resources.getString(R.string.network_exception_eof),
            ),
            state.getDetails(resources),
        )
    }
}
