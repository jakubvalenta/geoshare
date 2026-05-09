package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import io.ktor.utils.io.CancellationException
import kotlinx.collections.immutable.persistentListOf
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
import page.ooooo.geoshare.lib.inputs.BasicInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInput
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.inputs.ParseResult
import page.ooooo.geoshare.lib.network.ConnectionClosedNetworkException
import page.ooooo.geoshare.lib.network.NetworkTools
import java.io.EOFException
import java.net.MalformedURLException

class GrantedPermissionBasicUriInputTest {
    private val log = FakeLog
    private val maxAttempts = 3
    private val networkTools: NetworkTools = mock()
    private val resources: Resources = mock {
        on { getString(R.string.converter_google_maps_loading_indicator_title) } doReturn "Connecting to Google..."
        on { getString(R.string.conversion_failed_cancelled) } doReturn "Cancelled"
        on { getString(R.string.conversion_failed_reason_invalid_url) } doReturn "invalid URL"
        on { getString(R.string.conversion_failed_reason_missing_header) } doReturn "missing HTTP header"
        on {
            getString(R.string.conversion_failed_unshorten_error_with_reason, "invalid URL")
        } doReturn "Failed to resolve short link due to: invalid URL"
        on {
            getString(R.string.conversion_failed_unshorten_error_with_reason, "missing HTTP header")
        } doReturn "Failed to resolve short link due to: missing HTTP header"
        on {
            getString(R.string.conversion_failed_unshorten_error_with_reason, "response error 404")
        } doReturn "Failed to resolve short link due to: response error 404"
        on {
            getString(R.string.conversion_loading_indicator_description, 2, 10, "connection closed")
        } doReturn "Attempt 2 out of 10 due to connection closed."
        on { getString(R.string.network_exception_eof) } doReturn "connection closed"
    }
    private val uriQuote = FakeUriQuote

    @Test
    fun transition_whenInputGetDataDoesNotThrowException_returnsParsedData() = runTest {
        val source = "https://maps.google.com/foo"
        val input = object : BasicInput<String>, Input.HasPermission {
            override suspend fun getData(
                match: String,
                networkTools: NetworkTools,
                lastAttempt: NetworkTools.Attempt?,
                maxAttempts: Int,
                uriQuote: UriQuote,
                log: ILog,
                block: suspend (String) -> ParseResult,
            ) = block("${match}-data")

            override suspend fun parse(data: String, prevPoints: Points?, uriQuote: UriQuote, log: ILog) =
                ParseResult(prevPoints ?: persistentListOf(), nextMatch = data)

            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
        }
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val lastAttempt = null
        val permission = Permission.ALWAYS
        val stateContext: ConversionStateContext = mock {
            on { this@on.networkTools } doReturn networkTools
            on { this@on.log } doReturn log
            on { this@on.resources } doReturn resources
            on { this@on.uriQuote } doReturn uriQuote
        }
        val state = GrantedPermissionBasicInput(
            stateContext,
            source,
            match = source,
            input,
            input.loadingIndicatorTitleResId,
            permission,
            prevPoints,
            lastAttempt,
            maxAttempts,
        )
        assertEquals(
            ParsedData(
                stateContext,
                source,
                match = source,
                input,
                ParseResult(prevPoints, nextMatch = "${source}-data"),
                permission,
                prevPoints,
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenInputGetDataThrowsCancellationException_returnsConversionFailed() = runTest {
        val source = "https://maps.google.com/foo"
        val input = object : BasicInput<String>, Input.HasPermission {
            override suspend fun getData(
                match: String,
                networkTools: NetworkTools,
                lastAttempt: NetworkTools.Attempt?,
                maxAttempts: Int,
                uriQuote: UriQuote,
                log: ILog,
                block: suspend (String) -> ParseResult,
            ): ParseResult {
                throw CancellationException()
            }

            override suspend fun parse(data: String, prevPoints: Points?, uriQuote: UriQuote, log: ILog) =
                ParseResult(prevPoints ?: persistentListOf(), nextMatch = data)

            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
        }
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val lastAttempt = null
        val permission = Permission.ALWAYS
        val stateContext: ConversionStateContext = mock {
            on { this@on.networkTools } doReturn networkTools
            on { this@on.log } doReturn log
            on { this@on.resources } doReturn resources
            on { this@on.uriQuote } doReturn uriQuote
        }
        val state = GrantedPermissionBasicInput(
            stateContext,
            source,
            match = source,
            input,
            input.loadingIndicatorTitleResId,
            permission,
            prevPoints,
            lastAttempt,
            maxAttempts,
        )
        assertEquals(
            ConversionFailed(resources.getString(R.string.conversion_failed_cancelled), source),
            state.transition(),
        )
    }

    @Test
    fun transition_whenInputGetDataThrowsMalformedURLException_returnsConversionFailed() = runTest {
        val source = "https://maps.google.com/foo"
        val input = object : BasicInput<String>, Input.HasPermission {
            override suspend fun getData(
                match: String,
                networkTools: NetworkTools,
                lastAttempt: NetworkTools.Attempt?,
                maxAttempts: Int,
                uriQuote: UriQuote,
                log: ILog,
                block: suspend (String) -> ParseResult,
            ): ParseResult {
                throw MalformedURLException()
            }

            override suspend fun parse(data: String, prevPoints: Points?, uriQuote: UriQuote, log: ILog) =
                ParseResult(prevPoints ?: persistentListOf(), nextMatch = data)

            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
        }
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val lastAttempt = null
        val permission = Permission.ALWAYS
        val stateContext: ConversionStateContext = mock {
            on { this@on.networkTools } doReturn networkTools
            on { this@on.log } doReturn log
            on { this@on.resources } doReturn resources
            on { this@on.uriQuote } doReturn uriQuote
        }
        val state = GrantedPermissionBasicInput(
            stateContext,
            source,
            match = source,
            input,
            input.loadingIndicatorTitleResId,
            permission,
            prevPoints,
            lastAttempt,
            maxAttempts,
        )
        assertEquals(
            ConversionFailed(
                resources.getString(
                    R.string.conversion_failed_unshorten_error_with_reason,
                    resources.getString(R.string.conversion_failed_reason_invalid_url),
                ),
                source,
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenInputGetDataThrowsRecoverableNetworkExceptionAndLastAttemptIsNull_returnsGrantedPermission() =
        runTest {
            TODO()
        }

    @Test
    fun transition_whenInputGetDataThrowsRecoverableNetworkExceptionAndLastAttemptIsNotNull_returnsGrantedPermission() =
        runTest {
            TODO()
        }

    @Test
    fun transition_whenInputGetDataThrowsUnrecoverableNetworkExceptionAndLastAttemptIsNotNull_returnsConversionFailed() =
        runTest {
            TODO()
        }

    @Test
    fun getLoadingIndicator_whenLastAttemptIsNull_returnsLargeLoadingIndicatorWithoutDescription() = runTest {
        val source = "https://maps.google.com/foo"
        val input = GoogleMapsHtmlInput
        val stateContext: ConversionStateContext = mock {
            on { this@on.resources } doReturn resources
        }
        val state = GrantedPermissionBasicInput(
            stateContext,
            source,
            match = source,
            input,
            input.loadingIndicatorTitleResId,
            lastAttempt = null,
        )
        assertEquals(
            LoadingIndicator.Large(
                title = resources.getString(R.string.converter_google_maps_loading_indicator_title),
            ),
            state.getLoadingIndicator(),
        )
    }

    @Test
    fun getLoadingIndicator_whenLastAttemptNumberIsTwo_returnsLargeLoadingIndicatorWithDescription() = runTest {
        val source = "https://maps.google.com/foo"
        val input = GoogleMapsHtmlInput
        val stateContext: ConversionStateContext = mock {
            on { this@on.resources } doReturn resources
        }
        val state = GrantedPermissionBasicInput(
            stateContext,
            source,
            match = source,
            input,
            input.loadingIndicatorTitleResId,
            lastAttempt = NetworkTools.Attempt(
                2,
                ConnectionClosedNetworkException(EOFException()),
            ),
        )
        assertEquals(
            LoadingIndicator.Large(
                title = resources.getString(R.string.converter_google_maps_loading_indicator_title),
                description = resources.getString(
                    R.string.conversion_loading_indicator_description, 2, 10, R.string.network_exception_eof,
                )
            ),
            state.getLoadingIndicator(),
        )
    }
}
