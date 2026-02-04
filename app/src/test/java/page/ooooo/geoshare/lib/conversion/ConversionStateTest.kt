package page.ooooo.geoshare.lib.conversion

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.AutomationDelayPreference
import page.ooooo.geoshare.data.local.preferences.AutomationPreference
import page.ooooo.geoshare.data.local.preferences.BillingCachedProductIdPreference
import page.ooooo.geoshare.data.local.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.NetworkTools
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.Billing
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.inputs.GeoUriInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsInput
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.inputs.InputDocumentation
import page.ooooo.geoshare.lib.inputs.ParseHtmlResult
import page.ooooo.geoshare.lib.inputs.ParseUriResult
import page.ooooo.geoshare.lib.outputs.CoordinatesOutput
import page.ooooo.geoshare.lib.outputs.GeoUriOutput
import page.ooooo.geoshare.lib.outputs.GpxOutput
import page.ooooo.geoshare.lib.outputs.NoopAutomation
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.WGS84Point
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

@OptIn(ExperimentalCoroutinesApi::class)
class ConversionStateTest {

    private open class MockNetworkTools : NetworkTools() {
        override suspend fun requestLocationHeader(
            url: URL,
            retry: Retry?,
            dispatcher: CoroutineDispatcher,
        ): String? = onRequestLocationHeader(url)

        open fun onRequestLocationHeader(url: URL): String? {
            throw NotImplementedError()
        }

        override suspend fun getRedirectUrlString(
            url: URL,
            retry: Retry?,
            dispatcher: CoroutineDispatcher,
        ): String = onGetRedirectUrlString(url)

        open fun onGetRedirectUrlString(url: URL): String {
            throw NotImplementedError()
        }

        override suspend fun <T> getSource(
            url: URL,
            retry: Retry?,
            dispatcher: CoroutineDispatcher,
            block: suspend (source: ByteReadChannel) -> T,
        ): T = block(onGetSource(url).byteInputStream().toByteReadChannel())

        open fun onGetSource(url: URL): String {
            throw NotImplementedError()
        }
    }

    private val uriQuote = FakeUriQuote()
    private val fakeUserPreferencesRepository: UserPreferencesRepository = FakeUserPreferencesRepository()

    private fun mockStateContext(
        inputs: List<Input> = listOf(GeoUriInput, GoogleMapsInput),
        networkTools: NetworkTools = MockNetworkTools(),
        userPreferencesRepository: UserPreferencesRepository = fakeUserPreferencesRepository,
        billing: Billing = mock {},
        log: ILog = FakeLog,
        uriQuote: UriQuote = this@ConversionStateTest.uriQuote,
    ) = ConversionStateContext(
        inputs = inputs,
        networkTools = networkTools,
        userPreferencesRepository = userPreferencesRepository,
        billing = billing,
        log = log,
        uriQuote = uriQuote,
    )

    @Test
    fun initial_returnsNull() = runTest {
        val state = Initial()
        assertNull(state.transition())
    }

    @Test
    fun receivedUriString_inputUriStringIsEmpty_returnsConversionFailed() = runTest {
        val inputUriString = ""
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, "")
        assertEquals(
            ConversionFailed(R.string.conversion_failed_missing_url, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringIsGeoUri_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "geo:1,2?q="
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ReceivedUri(stateContext, inputUriString, GeoUriInput, uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringHasUriInTheMiddle_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "FOO https://maps.google.com/foo BAR"
        val matchedInputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(matchedInputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ReceivedUri(stateContext, inputUriString, GoogleMapsInput, uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringIsValidUrl_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ReceivedUri(stateContext, inputUriString, GoogleMapsInput, uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringIsNotValidUrl_returnsConversionFailed() = runTest {
        val inputUriString = "https://[invalid:ipv6]/"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringDoesNotHaveScheme_returnsConversionFailed() = runTest {
        val inputUriString = "maps.google.com/"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringHasRelativeScheme_returnsConversionFailed() = runTest {
        val inputUriString = "//maps.google.com/"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringDoesNotHaveHttpsScheme_returnsConversionFailed() = runTest {
        val inputUriString = "ftp://maps.google.com/"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringDoesNotMatchAnyInput_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.example.com/foo"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_inputSupportsShortUriAndItDoesNotMatchTheUri_returnsUnshortenedUrlAndPassesPermission() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = ReceivedUri(stateContext, inputUriString, GoogleMapsInput, uri, Permission.NEVER)
        assertEquals(
            UnshortenedUrl(stateContext, inputUriString, GoogleMapsInput, uri, Permission.NEVER),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_inputDoesNotSupportShortUri_returnsUnshortenedUrlAndPassesPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val mockInput = object : Input {
            override val uriPattern = Regex(".")
            override val documentation =
                InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())

            override suspend fun parseUri(uri: Uri) = ParseUriResult.Succeeded(persistentListOf())
        }
        val stateContext = mockStateContext(inputs = listOf(mockInput))
        val state = ReceivedUri(stateContext, inputUriString, mockInput, uri, Permission.NEVER)
        assertEquals(
            UnshortenedUrl(stateContext, inputUriString, mockInput, uri, Permission.NEVER),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_inputSupportsShortUriAndItMatchesTheUriAndPermissionIsAlways_returnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(eq(ConnectionPermissionPreference)) } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = ReceivedUri(
                stateContext, inputUriString, GoogleMapsInput, uri, Permission.ALWAYS
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput, uri),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_inputSupportsShortUriAndItMatchesTheUriAndPermissionIsAsk_returnsRequestedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(eq(ConnectionPermissionPreference)) } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = ReceivedUri(stateContext, inputUriString, GoogleMapsInput, uri, Permission.ASK)
            assertEquals(
                RequestedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput, uri),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_inputSupportsShortUriAndItMatchesTheUriAndPermissionIsNever_returnsDeniedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(eq(ConnectionPermissionPreference)) } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = ReceivedUri(stateContext, inputUriString, GoogleMapsInput, uri, Permission.NEVER)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsInput),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_inputSupportsShortUriAndItMatchesTheUriPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(eq(ConnectionPermissionPreference)) } doReturn Permission.ALWAYS
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = ReceivedUri(stateContext, inputUriString, GoogleMapsInput, uri, null)
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput, uri),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_inputSupportsShortUriAndItMatchesTheUriAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(eq(ConnectionPermissionPreference)) } doReturn Permission.ASK
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = ReceivedUri(stateContext, inputUriString, GoogleMapsInput, uri, null)
            assertEquals(
                RequestedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput, uri),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_inputSupportsShortUriAndItMatchesTheUriAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(eq(ConnectionPermissionPreference)) } doReturn Permission.NEVER
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = ReceivedUri(stateContext, inputUriString, GoogleMapsInput, uri, null)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsInput),
                state.transition(),
            )
        }

    @Test
    fun requestedUnshortenPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = RequestedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput, uri)
        assertNull(state.transition())
    }

    @Test
    fun requestedUnshortenPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedUnshortenPermission(
                stateContext, inputUriString, GoogleMapsInput, uri
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput, uri),
                state.grant(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(ConnectionPermissionPreference),
                any<Permission>(),
            )
        }

    @Test
    fun requestedUnshortenPermission_grantWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedUnshortenPermission(
                stateContext, inputUriString, GoogleMapsInput, uri
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput, uri),
                state.grant(true),
            )
            verify(mockUserPreferencesRepository).setValue(
                ConnectionPermissionPreference,
                Permission.ALWAYS,
            )
        }

    @Test
    fun requestedUnshortenPermission_denyWithDoNotAskFalse_doesNotSavePreferenceAndReturnsDeniedConnectionPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedUnshortenPermission(
                stateContext, inputUriString, GoogleMapsInput, uri
            )
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsInput),
                state.deny(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(ConnectionPermissionPreference),
                any<Permission>(),
            )
        }

    @Test
    fun requestedUnshortenPermission_denyWithDoNotAskTrue_savesPreferenceAndDeniedConnectionPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = RequestedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput, uri)
        assertEquals(
            DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsInput),
            state.deny(true),
        )
        verify(mockUserPreferencesRepository).setValue(
            ConnectionPermissionPreference,
            Permission.NEVER,
        )
    }

    @Test
    fun grantedUnshortenPermission_inputUriStringIsInvalidURL_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://[invalid:ipv6]/"
            val uri = Uri.parse(inputUriString, uriQuote)
            val redirectUriString = "https://maps.google.com/redirect"
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onRequestLocationHeader(url: URL): String = redirectUriString
                },
            )
            val state = GrantedUnshortenPermission(
                stateContext, inputUriString, GoogleMapsInput, uri
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_unshorten_error, inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderThrowsCancellationException_returnsConversionFailedWithCancelledErrorMessage() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onRequestLocationHeader(url: URL): String? =
                        if (url.toString() == inputUriString) {
                            throw CancellationException()
                        } else {
                            super.onRequestLocationHeader(url)
                        }
                },
            )
            val state = GrantedUnshortenPermission(
                stateContext, inputUriString, GoogleMapsInput, uri
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_cancelled, inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderThrowsSocketTimeoutException_returnsGrantedUnshortenPermissionWithRetry() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val tr = NetworkTools.RecoverableException(
                R.string.network_exception_socket_timeout, SocketTimeoutException()
            )
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onRequestLocationHeader(url: URL): String? =
                        if (url.toString() == inputUriString) {
                            throw tr
                        } else {
                            super.onRequestLocationHeader(url)
                        }
                },
            )
            val state = GrantedUnshortenPermission(
                stateContext, inputUriString, GoogleMapsInput, uri
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext, inputUriString, GoogleMapsInput, uri, NetworkTools.Retry(1, tr),
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderThrowsSocketTimeoutExceptionAndRetryIsOne_returnsGrantedUnshortenPermissionWithRetryTwo() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val tr = NetworkTools.RecoverableException(
                R.string.network_exception_socket_timeout, SocketTimeoutException()
            )
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onRequestLocationHeader(url: URL): String? =
                        if (url.toString() == inputUriString) {
                            throw tr
                        } else {
                            super.onRequestLocationHeader(url)
                        }
                },
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                retry = NetworkTools.Retry(1, tr),
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext, inputUriString, GoogleMapsInput, uri, NetworkTools.Retry(2, tr),
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderThrowsUnexpectedResponseCodeException_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onRequestLocationHeader(url: URL): String? =
                        if (url.toString() == inputUriString) {
                            throw UnrecoverableException(
                                R.string.network_exception_server_response_error,
                                Exception(),
                            )
                        } else {
                            super.onRequestLocationHeader(url)
                        }
                },
            )
            val state = GrantedUnshortenPermission(
                stateContext, inputUriString, GoogleMapsInput, uri
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_unshorten_error, inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderThrowsUnexpectedResponseCodeExceptionWithSocketTimeoutExceptionCause_returnsConversionFailedWithConnectionErrorMessage() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onRequestLocationHeader(url: URL): String? =
                        if (url.toString() == inputUriString) {
                            throw UnrecoverableException(
                                R.string.network_exception_server_response_error,
                                SocketTimeoutException(),
                            )
                        } else {
                            super.onRequestLocationHeader(url)
                        }
                },
            )
            val state = GrantedUnshortenPermission(
                stateContext, inputUriString, GoogleMapsInput, uri
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_unshorten_connection_error, inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderReturnsNull_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onRequestLocationHeader(url: URL): String? =
                        if (url.toString() == inputUriString) {
                            null
                        } else {
                            super.onRequestLocationHeader(url)
                        }
                },
            )
            val state = GrantedUnshortenPermission(
                stateContext, inputUriString, GoogleMapsInput, uri
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_unshorten_error, inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_inputUriStringHasNoScheme_callsRequestLocationHeaderWithUrlWithHttpsScheme() =
        runTest {
            val inputUriString = "maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val redirectUriString = "https://maps.google.com/redirect"
            val redirectUri = Uri.parse(redirectUriString, uriQuote)
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onRequestLocationHeader(url: URL): String? =
                        if (url.toString() == "https://$inputUriString") {
                            redirectUriString
                        } else {
                            super.onRequestLocationHeader(url)
                        }
                },
            )
            val state = GrantedUnshortenPermission(
                stateContext, inputUriString, GoogleMapsInput, uri
            )
            assertEquals(
                UnshortenedUrl(
                    stateContext, inputUriString, GoogleMapsInput, redirectUri, Permission.ALWAYS
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderReturnsAbsoluteUrl_returnsUnshortenedUrl() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val redirectUriString = "https://maps.google.com/redirect"
        val redirectUri = Uri.parse(redirectUriString, uriQuote)
        val stateContext = mockStateContext(
            networkTools = object : MockNetworkTools() {
                override fun onRequestLocationHeader(url: URL): String? = if (url.toString() == inputUriString) {
                    redirectUriString
                } else {
                    super.onRequestLocationHeader(url)
                }
            },
        )
        val state = GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput, uri)
        assertEquals(
            UnshortenedUrl(
                stateContext, inputUriString, GoogleMapsInput, redirectUri, Permission.ALWAYS
            ),
            state.transition(),
        )
    }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderReturnsRelativeUrl_returnsUnshortenedUrl() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val redirectUriString = "redirect"
        val redirectUri = Uri.parse("$inputUriString/$redirectUriString", uriQuote)
        val stateContext = mockStateContext(
            networkTools = object : MockNetworkTools() {
                override fun onRequestLocationHeader(url: URL): String? = if (url.toString() == inputUriString) {
                    redirectUriString
                } else {
                    super.onRequestLocationHeader(url)
                }
            },
        )
        val state = GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput, uri)
        assertEquals(
            UnshortenedUrl(
                stateContext, inputUriString, GoogleMapsInput, redirectUri, Permission.ALWAYS
            ),
            state.transition(),
        )
    }

    @Test
    fun grantedUnshortenPermission_inputHasShortUriMethodGet_usesGetRedirectUrlString() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val redirectUriString = "https://maps.google.com/redirect"
        val redirectUri = Uri.parse(redirectUriString, uriQuote)
        val mockInput = object : Input.HasShortUri {
            override val uriPattern = Regex(".")
            override val documentation =
                InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())
            override val shortUriPattern = Regex(".")
            override val shortUriMethod = Input.ShortUriMethod.GET
            override val permissionTitleResId = -1
            override val loadingIndicatorTitleResId = -1
            override suspend fun parseUri(uri: Uri) = ParseUriResult.Succeeded(persistentListOf())
        }
        val stateContext = mockStateContext(
            inputs = listOf(mockInput),
            networkTools = object : MockNetworkTools() {
                override fun onGetRedirectUrlString(url: URL): String = if (url.toString() == inputUriString) {
                    redirectUriString
                } else {
                    super.onGetSource(url)
                }
            },
        )
        val state = GrantedUnshortenPermission(stateContext, inputUriString, mockInput, uri)
        assertEquals(
            UnshortenedUrl(stateContext, inputUriString, mockInput, redirectUri, Permission.ALWAYS),
            state.transition(),
        )
    }

    @Test
    fun deniedConnectionPermission_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val stateContext = mockStateContext()
        val state = DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsInput)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_connection_permission_denied, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parseUriReturnsSucceeded_returnsConversionSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val mockGoogleMapsInput: GoogleMapsInput = mock {
            on { parseUri(any()) } doReturn ParseUriResult.Succeeded(points)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(ConnectionPermissionPreference) } doThrow NotImplementedError()
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            inputs = listOf(mockGoogleMapsInput),
        )
        val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsInput, uri, null)
        assertEquals(
            ConversionSucceeded(stateContext, inputUriString, points),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parseUriReturnsFailed_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val mockGoogleMapsInput: GoogleMapsInput = mock {
            on { parseUri(any()) } doReturn ParseUriResult.Failed()
        }
        val stateContext = mockStateContext(inputs = listOf(mockGoogleMapsInput))
        val state = UnshortenedUrl(
            stateContext, inputUriString, GoogleMapsInput, uri, Permission.ALWAYS
        )
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_url_error, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parseUriReturnsSucceededAndSupportsHtmlParsingButInputDoesNotSupportHtmlParsing_returnsDeniedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf<WGS84Point>()
            val htmlUriString = "$inputUriString/html"
            val mockInput = object : Input {
                override val uriPattern = Regex(".")
                override val documentation =
                    InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())

                override suspend fun parseUri(uri: Uri) =
                    ParseUriResult.SucceededAndSupportsHtmlParsing(points, htmlUriString)
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = UnshortenedUrl(
                stateContext, inputUriString, mockInput, uri, Permission.ALWAYS
            )
            assertEquals(
                DeniedParseHtmlPermission(stateContext, inputUriString, points),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsSucceededAndSupportsHtmlParsingAndPermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val mockGoogleMapsInput: GoogleMapsInput = mock {
                on { parseUri(any()) } doReturn
                    ParseUriResult.SucceededAndSupportsHtmlParsing(points, htmlUriString)
            }
            val stateContext = mockStateContext(inputs = listOf(mockGoogleMapsInput))
            val state = UnshortenedUrl(
                stateContext, inputUriString, mockGoogleMapsInput, uri, Permission.ALWAYS
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    mockGoogleMapsInput,
                    uri,
                    points,
                    htmlUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsSucceededAndSupportsHtmlParsingAndPermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val mockGoogleMapsInput: GoogleMapsInput = mock {
                on { parseUri(any()) } doReturn
                    ParseUriResult.SucceededAndSupportsHtmlParsing(points, htmlUriString)
            }
            val stateContext = mockStateContext(inputs = listOf(mockGoogleMapsInput))
            val state = UnshortenedUrl(
                stateContext, inputUriString, mockGoogleMapsInput, uri, Permission.ASK
            )
            assertEquals(
                RequestedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    mockGoogleMapsInput,
                    uri,
                    points,
                    htmlUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsSucceededAndSupportsHtmlParsingAndPermissionIsNever_returnsDeniedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val mockGoogleMapsInput: GoogleMapsInput = mock {
                on { parseUri(any()) } doReturn
                    ParseUriResult.SucceededAndSupportsHtmlParsing(points, htmlUriString)
            }
            val stateContext = mockStateContext(inputs = listOf(mockGoogleMapsInput))
            val state = UnshortenedUrl(
                stateContext, inputUriString, mockGoogleMapsInput, uri, Permission.NEVER
            )
            assertEquals(
                DeniedParseHtmlPermission(stateContext, inputUriString, points),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsSucceededAndSupportsHtmlParsingAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val mockGoogleMapsInput: GoogleMapsInput = mock {
                on { parseUri(any()) } doReturn
                    ParseUriResult.SucceededAndSupportsHtmlParsing(points, htmlUriString)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(ConnectionPermissionPreference) } doReturn Permission.ALWAYS
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                inputs = listOf(mockGoogleMapsInput),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsInput, uri, null)
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    mockGoogleMapsInput,
                    uri,
                    points,
                    htmlUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsSucceededAndSupportsHtmlParsingAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val mockGoogleMapsInput: GoogleMapsInput = mock {
                on { parseUri(any()) } doReturn
                    ParseUriResult.SucceededAndSupportsHtmlParsing(points, htmlUriString)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(ConnectionPermissionPreference) } doReturn Permission.ASK
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                inputs = listOf(mockGoogleMapsInput),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsInput, uri, null)
            assertEquals(
                RequestedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    mockGoogleMapsInput,
                    uri,
                    points,
                    htmlUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriSucceededAndSupportsHtmlParsingAndUrlAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val mockGoogleMapsInput: GoogleMapsInput = mock {
                on { parseUri(any()) } doReturn
                    ParseUriResult.SucceededAndSupportsHtmlParsing(points, htmlUriString)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(ConnectionPermissionPreference) } doReturn Permission.NEVER
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                inputs = listOf(mockGoogleMapsInput),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsInput, uri, null)
            assertEquals(
                DeniedParseHtmlPermission(stateContext, inputUriString, points),
                state.transition(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val points = persistentListOf(WGS84Point(name = "bar"))
        val htmlUriString = "$inputUriString/html"
        val stateContext = mockStateContext()
        val state = RequestedParseHtmlPermission(
            stateContext,
            inputUriString,
            GoogleMapsInput,
            uri,
            points,
            htmlUriString,
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedParseHtmlPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsInput,
                    uri,
                    pointsFromUri,
                    htmlUriString,
                ),
                state.grant(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(ConnectionPermissionPreference),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_grantWithDoNotAskTrue_savesPreferenceAndReturnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsInput,
                    uri,
                    pointsFromUri,
                    htmlUriString,
                ),
                state.grant(true),
            )
            verify(mockUserPreferencesRepository).setValue(
                ConnectionPermissionPreference,
                Permission.ALWAYS,
            )
        }

    @Test
    fun requestedParseHtmlPermission_denyWithDoNotAskFalse_doesNotSavePreferenceAndReturnsDeniedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                DeniedParseHtmlPermission(stateContext, inputUriString, pointsFromUri),
                state.deny(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(ConnectionPermissionPreference),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_denyWithDoNotAskTrue_savesPreferenceAndReturnsDeniedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                DeniedParseHtmlPermission(stateContext, inputUriString, pointsFromUri),
                state.deny(true),
            )
            verify(mockUserPreferencesRepository).setValue(
                ConnectionPermissionPreference,
                Permission.NEVER,
            )
        }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsFailed_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
        val htmlUriString = "$inputUriString/html"
        val html = "<html></html>"
        val mockInput = object : Input.HasHtml {
            override val uriPattern = Regex(".")
            override val documentation =
                InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())
            override val permissionTitleResId = -1
            override val loadingIndicatorTitleResId = -1
            override suspend fun parseUri(uri: Uri): ParseUriResult {
                throw NotImplementedError()
            }

            override suspend fun parseHtml(
                htmlUrlString: String,
                channel: ByteReadChannel,
                pointsFromUri: ImmutableList<Point>,
                log: ILog,
            ) = ParseHtmlResult.Failed()
        }
        val stateContext = mockStateContext(
            inputs = listOf(mockInput),
            networkTools = object : MockNetworkTools() {
                override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                    html
                } else {
                    super.onGetSource(url)
                }
            },
        )
        val state = GrantedParseHtmlPermission(
            stateContext,
            inputUriString,
            mockInput,
            uri,
            pointsFromUri,
            htmlUriString,
        )
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlPermission_inputUriStringIsInvalidURL_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://[invalid:ipv6]/"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val html = "<html></html>"
            val stateContext = mockStateContext(networkTools = object : MockNetworkTools() {
                override fun onGetSource(url: URL): String = if (url.toString() == inputUriString) {
                    html
                } else {
                    super.onGetSource(url)
                }
            })
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getSourceThrowsCancellationException_returnsConversionFailedWithCancelledMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                        throw CancellationException()
                    } else {
                        super.onGetSource(url)
                    }
                },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_cancelled, inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getSourceThrowsSocketTimeoutException_returnsGrantedParseHtmlPermissionWithRetry() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val tr = NetworkTools.RecoverableException(
                R.string.network_exception_socket_timeout, SocketTimeoutException(),
            )
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                        throw tr
                    } else {
                        super.onGetSource(url)
                    }
                },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsInput,
                    uri,
                    pointsFromUri,
                    htmlUriString,
                    NetworkTools.Retry(1, tr),
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getSourceThrowsSocketTimeoutExceptionAndRetryIsOne_returnsGrantedParseHtmlPermissionWithRetryTwo() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val tr = NetworkTools.RecoverableException(
                R.string.network_exception_socket_timeout, SocketTimeoutException(),
            )
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                        throw tr
                    } else {
                        super.onGetSource(url)
                    }
                },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                pointsFromUri,
                htmlUriString,
                retry = NetworkTools.Retry(1, tr),
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsInput,
                    uri,
                    pointsFromUri,
                    htmlUriString,
                    NetworkTools.Retry(2, tr),
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getSourceThrowsUnexpectedResponseCodeException_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                        throw UnrecoverableException(
                            R.string.network_exception_server_response_error,
                            Exception(),
                        )
                    } else {
                        super.onGetSource(url)
                    }
                },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getSourceThrowsUnexpectedResponseCodeExceptionWithSocketTimeoutExceptionCause_returnsConversionFailedWithConnectionErrorMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                        throw UnrecoverableException(
                            R.string.network_exception_server_response_error,
                            SocketTimeoutException(),
                        )
                    } else {
                        super.onGetSource(url)
                    }
                },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_connection_error, inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_htmlUriStringHasNoScheme_callsGetTextWithUrlWithHttpsScheme() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val html = "<html></html>"
        val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
        val htmlUriString = "maps.apple.com/foo"
        val pointsFromHtml = persistentListOf(WGS84Point(1.0, 2.0, name = "fromHtml"))
        val mockInput = object : Input.HasHtml {
            override val uriPattern = Regex(".")
            override val documentation =
                InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())
            override val permissionTitleResId = -1
            override val loadingIndicatorTitleResId = -1
            override suspend fun parseUri(uri: Uri): ParseUriResult {
                throw NotImplementedError()
            }

            override suspend fun parseHtml(
                htmlUrlString: String,
                channel: ByteReadChannel,
                pointsFromUri: ImmutableList<Point>,
                log: ILog,
            ) = ParseHtmlResult.Succeeded(pointsFromHtml)
        }
        val stateContext = mockStateContext(
            inputs = listOf(mockInput),
            networkTools = object : MockNetworkTools() {
                override fun onGetSource(url: URL): String = if (url.toString() == "https://$htmlUriString") {
                    html
                } else {
                    super.onGetSource(url)
                }
            },
        )
        val state = GrantedParseHtmlPermission(
            stateContext,
            inputUriString,
            mockInput,
            uri,
            pointsFromUri,
            htmlUriString,
        )
        assertEquals(
            ConversionSucceeded(stateContext, inputUriString, pointsFromHtml),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsSucceeded_returnsSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val html = "<html></html>"
        val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
        val htmlUriString = "https://api.apple.com/foo.json"
        val pointsFromHtml = persistentListOf(WGS84Point(1.0, 2.0, name = "fromHtml"))
        val mockInput = object : Input.HasHtml {
            override val uriPattern = Regex(".")
            override val documentation =
                InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())
            override val permissionTitleResId = -1
            override val loadingIndicatorTitleResId = -1
            override suspend fun parseUri(uri: Uri): ParseUriResult {
                throw NotImplementedError()
            }

            override suspend fun parseHtml(
                htmlUrlString: String,
                channel: ByteReadChannel,
                pointsFromUri: ImmutableList<Point>,
                log: ILog,
            ) = ParseHtmlResult.Succeeded(pointsFromHtml)
        }
        val stateContext = mockStateContext(
            inputs = listOf(mockInput),
            networkTools = object : MockNetworkTools() {
                override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                    html
                } else {
                    super.onGetSource(url)
                }
            },
        )
        val state = GrantedParseHtmlPermission(
            stateContext,
            inputUriString,
            mockInput,
            uri,
            pointsFromUri,
            htmlUriString,
        )
        assertEquals(
            ConversionSucceeded(stateContext, inputUriString, pointsFromHtml),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsRequiresRedirectWithAbsoluteUrl_returnsReceivedUriWithTheUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val redirectUriString = "$inputUriString/redirect"
            val redirectUri = Uri.parse(redirectUriString, uriQuote)
            val mockInput = object : Input.HasHtml {
                override val uriPattern = Regex(".")
                override val documentation =
                    InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
                override suspend fun parseUri(uri: Uri): ParseUriResult {
                    throw NotImplementedError()
                }

                override suspend fun parseHtml(
                    htmlUrlString: String,
                    channel: ByteReadChannel,
                    pointsFromUri: ImmutableList<Point>,
                    log: ILog,
                ) = ParseHtmlResult.RequiresRedirect(redirectUriString)
            }
            val stateContext = mockStateContext(
                inputs = listOf(mockInput),
                networkTools = object : MockNetworkTools() {
                    override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                        listOf(
                            "first",
                            "second",
                            "third",
                            redirectUriString,
                            "fifth",
                        ).joinToString("\n") { it.repeat(2048) }
                    } else {
                        super.onGetSource(url)
                    }
                },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                mockInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                ReceivedUri(stateContext, inputUriString, mockInput, redirectUri, Permission.ALWAYS),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsRequiresRedirectWithRelativeUrl_returnsReceivedUriWithTheUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val redirectUriString = "redirect"
            val redirectUri = Uri.parse("$inputUriString/$redirectUriString", uriQuote)
            val html = "<html></html>"
            val mockInput = object : Input.HasHtml {
                override val uriPattern = Regex(".")
                override val documentation =
                    InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
                override suspend fun parseUri(uri: Uri): ParseUriResult {
                    throw NotImplementedError()
                }

                override suspend fun parseHtml(
                    htmlUrlString: String,
                    channel: ByteReadChannel,
                    pointsFromUri: ImmutableList<Point>,
                    log: ILog,
                ) = ParseHtmlResult.RequiresRedirect(redirectUriString)
            }
            val stateContext = mockStateContext(
                inputs = listOf(mockInput),
                networkTools = object : MockNetworkTools() {
                    override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                        html
                    } else {
                        super.onGetSource(url)
                    }
                },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                mockInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                ReceivedUri(stateContext, inputUriString, mockInput, redirectUri, Permission.ALWAYS),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsRequiresWebParsingAndInputSupportsWebParsing_returnsGrantedParseWebPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val webUrlString = "$inputUriString/web"
            val html = "<html></html>"
            val mockInput = object : Input.HasHtml, Input.HasWeb {
                override val uriPattern = Regex(".")
                override val documentation =
                    InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
                override suspend fun parseUri(uri: Uri): ParseUriResult {
                    throw NotImplementedError()
                }

                override suspend fun parseHtml(
                    htmlUrlString: String,
                    channel: ByteReadChannel,
                    pointsFromUri: ImmutableList<Point>,
                    log: ILog,
                ) = ParseHtmlResult.RequiresWebParsing(webUrlString)
            }
            val stateContext = mockStateContext(
                inputs = listOf(mockInput),
                networkTools = object : MockNetworkTools() {
                    override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                        html
                    } else {
                        super.onGetSource(url)
                    }
                },
            )
            val state = GrantedParseHtmlPermission(
                stateContext, inputUriString, mockInput, uri, pointsFromUri, htmlUriString
            )
            assertEquals(
                GrantedParseWebPermission(stateContext, inputUriString, mockInput, uri, pointsFromUri, webUrlString),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsRequiresWebParsingAndInputDoesNotSupportsWebParsing_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val htmlUriString = "$inputUriString/html"
            val webUrlString = "$inputUriString/web"
            val html = "<html></html>"
            val mockInput = object : Input.HasHtml {
                override val uriPattern = Regex(".")
                override val documentation =
                    InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
                override suspend fun parseUri(uri: Uri): ParseUriResult {
                    throw NotImplementedError()
                }

                override suspend fun parseHtml(
                    htmlUrlString: String,
                    channel: ByteReadChannel,
                    pointsFromUri: ImmutableList<Point>,
                    log: ILog,
                ) = ParseHtmlResult.RequiresWebParsing(webUrlString)
            }
            val stateContext = mockStateContext(
                inputs = listOf(mockInput),
                networkTools = object : MockNetworkTools() {
                    override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                        html
                    } else {
                        super.onGetSource(url)
                    }
                },
            )
            val state = GrantedParseHtmlPermission(
                stateContext, inputUriString, mockInput, uri, pointsFromUri, htmlUriString
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString),
                state.transition(),
            )
        }

    @Test
    fun deniedParseHtmlPermission_lastPointHasCoords_returnsConversionSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val stateContext = mockStateContext()
        val state = DeniedParseHtmlPermission(stateContext, inputUriString, points)
        assertEquals(
            ConversionSucceeded(stateContext, inputUriString, points),
            state.transition(),
        )
    }

    @Test
    fun deniedParseHtmlPermission_lastPointHasName_returnsConversionSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val points = persistentListOf(WGS84Point(name = "bar"))
        val stateContext = mockStateContext()
        val state = DeniedParseHtmlPermission(stateContext, inputUriString, points)
        assertEquals(
            ConversionSucceeded(stateContext, inputUriString, points),
            state.transition(),
        )
    }

    @Test
    fun deniedParseHtmlPermission_pointsAreEmpty_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val points = persistentListOf<WGS84Point>()
        val stateContext = mockStateContext()
        val state = DeniedParseHtmlPermission(stateContext, inputUriString, points)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun requestedParseWebPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val points = persistentListOf(WGS84Point(name = "bar"))
        val webUrlString = "$inputUriString/web"
        val stateContext = mockStateContext()
        val state = RequestedParseWebPermission(
            stateContext,
            inputUriString,
            GoogleMapsInput,
            uri,
            points,
            webUrlString,
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedParseWebPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedParseWebPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val webUrlString = "$inputUriString/web"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseWebPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                pointsFromUri,
                webUrlString,
            )
            assertEquals(
                GrantedParseWebPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsInput,
                    uri,
                    pointsFromUri,
                    webUrlString,
                ),
                state.grant(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(ConnectionPermissionPreference),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseWebPermission_grantWithDoNotAskTrue_savesPreferenceAndReturnsGrantedParseWebPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val webUrlString = "$inputUriString/web"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseWebPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                pointsFromUri,
                webUrlString,
            )
            assertEquals(
                GrantedParseWebPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsInput,
                    uri,
                    pointsFromUri,
                    webUrlString,
                ),
                state.grant(true),
            )
            verify(mockUserPreferencesRepository).setValue(
                ConnectionPermissionPreference,
                Permission.ALWAYS,
            )
        }

    @Test
    fun requestedParseWebPermission_denyWithDoNotAskFalse_doesNotSavePreferenceAndReturnsDeniedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val webUrlString = "$inputUriString/web"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseWebPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                pointsFromUri,
                webUrlString,
            )
            assertEquals(
                DeniedParseHtmlPermission(stateContext, inputUriString, pointsFromUri),
                state.deny(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(ConnectionPermissionPreference),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseWebPermission_denyWithDoNotAskTrue_savesPreferenceAndReturnsDeniedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val webUrlString = "$inputUriString/web"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseWebPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                pointsFromUri,
                webUrlString,
            )
            assertEquals(
                DeniedParseHtmlPermission(stateContext, inputUriString, pointsFromUri),
                state.deny(true),
            )
            verify(mockUserPreferencesRepository).setValue(
                ConnectionPermissionPreference,
                Permission.NEVER,
            )
        }

    @Test
    fun grantedParseWebPermission_transition_isCancelled_returnsNull() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
        val webUrlString = "$inputUriString/web"
        val stateContext = mockStateContext()
        val state = GrantedParseWebPermission(
            stateContext, inputUriString, GoogleMapsInput, uri, pointsFromUri, webUrlString
        )
        var res: State? = Initial() // Use Initial as the default value to test that it gets set to null
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
        assertNull(res)
    }

    @Test
    fun grantedParseWebPermission_transition_isNotCancelled_returnsConversionFailedAfterTimeout() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
        val webUrlString = "$inputUriString/web"
        val timeout = 7.seconds
        val stateContext = mockStateContext()
        val state = GrantedParseWebPermission(
            stateContext, inputUriString, GoogleMapsInput, uri, pointsFromUri, webUrlString, timeout
        )
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString),
                state.transition(),
            )
        }
        assertEquals(timeout, workDuration)
    }

    @Test
    fun grantedParseWebPermission_onUrlChange_uriPatternMatchesAndParseUriReturnsSucceeded_returnsConversionSucceeded() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val resPoints = persistentListOf(WGS84Point(3.0, 4.0))
            val webUrlString = "$inputUriString/web"
            val currentWebUrlString = "$webUrlString/current"
            val mockInput = object : Input.HasWeb {
                override val uriPattern = Regex("""^https://maps\.apple\.com/\S*""")
                override val documentation =
                    InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())

                override suspend fun parseUri(uri: Uri) =
                    if (uri.toString() == currentWebUrlString) {
                        ParseUriResult.Succeeded(resPoints)
                    } else {
                        throw NotImplementedError()
                    }

                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = GrantedParseWebPermission(
                stateContext, inputUriString, mockInput, uri, pointsFromUri, webUrlString
            )
            assertEquals(
                ConversionSucceeded(stateContext, inputUriString, resPoints),
                state.onUrlChange(currentWebUrlString),
            )
        }

    @Test
    fun grantedParseWebPermission_onUrlChange_uriPatternMatchesAndParseUriReturnsSucceededAndSupportsHtmlParsing_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val resPoints = persistentListOf(WGS84Point(3.0, 4.0))
            val webUrlString = "$inputUriString/web"
            val currentWebUrlString = "$webUrlString/current"
            val mockInput = object : Input.HasWeb {
                override val uriPattern = Regex("""^https://maps\.apple\.com/\S*""")
                override val documentation =
                    InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())

                override suspend fun parseUri(uri: Uri) =
                    if (uri.toString() == currentWebUrlString) {
                        ParseUriResult.SucceededAndSupportsHtmlParsing(resPoints, "$inputUriString/html")
                    } else {
                        throw NotImplementedError()
                    }

                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = GrantedParseWebPermission(
                stateContext, inputUriString, mockInput, uri, pointsFromUri, webUrlString
            )
            assertEquals(
                ConversionSucceeded(stateContext, inputUriString, resPoints),
                state.onUrlChange(currentWebUrlString),
            )
        }

    @Test
    fun grantedParseWebPermission_onUrlChange_uriPatternMatchesAndParseUriReturnsSucceededAndSupportsWebParsing_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val resPoints = persistentListOf(WGS84Point(3.0, 4.0))
            val webUrlString = "$inputUriString/web"
            val currentWebUrlString = "$webUrlString/current"
            val mockInput = object : Input.HasWeb {
                override val uriPattern = Regex("""^https://maps\.apple\.com/\S*""")
                override val documentation =
                    InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())

                override suspend fun parseUri(uri: Uri) =
                    if (uri.toString() == currentWebUrlString) {
                        ParseUriResult.SucceededAndSupportsWebParsing(resPoints, "$inputUriString/web")
                    } else {
                        throw NotImplementedError()
                    }

                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = GrantedParseWebPermission(
                stateContext, inputUriString, mockInput, uri, pointsFromUri, webUrlString
            )
            assertEquals(
                ConversionSucceeded(stateContext, inputUriString, resPoints),
                state.onUrlChange(currentWebUrlString),
            )
        }

    @Test
    fun grantedParseWebPermission_onUrlChange_uriPatternMatchesAndParseUriReturnsFailed_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val webUrlString = "$inputUriString/web"
            val currentWebUrlString = "$webUrlString/current"
            val mockInput = object : Input.HasWeb {
                override val uriPattern = Regex("""^https://maps\.apple\.com/\S*""")
                override val documentation =
                    InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())

                override suspend fun parseUri(uri: Uri) =
                    if (uri.toString() == currentWebUrlString) {
                        ParseUriResult.Failed()
                    } else {
                        throw NotImplementedError()
                    }

                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = GrantedParseWebPermission(
                stateContext, inputUriString, mockInput, uri, pointsFromUri, webUrlString
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString),
                state.onUrlChange(currentWebUrlString),
            )
        }

    @Test
    fun grantedParseWebPermission_onUrlChange_uriPatternDoesNotMatch_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://spam.apple.com/"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar"))
            val resPoints = persistentListOf(WGS84Point(3.0, 4.0))
            val webUrlString = "$inputUriString/web"
            val currentWebUrlString = "$webUrlString/current"
            val mockInput = object : Input.HasWeb {
                override val uriPattern = Regex("""^https://maps\.apple\.com/\S*""")
                override val documentation =
                    InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())

                override suspend fun parseUri(uri: Uri) =
                    if (uri.toString() == currentWebUrlString) {
                        ParseUriResult.Succeeded(resPoints)
                    } else {
                        throw NotImplementedError()
                    }

                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = GrantedParseWebPermission(
                stateContext, inputUriString, mockInput, uri, pointsFromUri, webUrlString
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString),
                state.onUrlChange(currentWebUrlString),
            )
        }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsNoop_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = NoopAutomation
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn action
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertNull(state.transition())
    }

    @Test
    fun conversionSucceeded_billingStatusIsLoadingAndCachedProductIdIsNotSet_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = CoordinatesOutput.CopyDecCoordsAutomation
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(BillingStatus.Loading())
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn action
            on { setValue(eq(BillingCachedProductIdPreference), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertNull(state.transition())
        verify(mockUserPreferencesRepository, never()).setValue(
            eq(BillingCachedProductIdPreference),
            any(),
        )
    }

    @Test
    fun conversionSucceeded_billingStatusIsLoadingAndCachedProductIsAnUnknownProduct_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = CoordinatesOutput.CopyDecCoordsAutomation
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(BillingStatus.Loading())
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn action
            on { getValue(BillingCachedProductIdPreference) } doReturn "spam"
            on { setValue(eq(BillingCachedProductIdPreference), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertNull(state.transition())
        verify(mockUserPreferencesRepository, never()).setValue(
            eq(BillingCachedProductIdPreference),
            any(),
        )
    }

    @Test
    fun conversionSucceeded_billingStatusIsLoadingAndCachedProductIsAKnownProduct_returnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = CoordinatesOutput.CopyDecCoordsAutomation
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(BillingStatus.Loading())
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn action
            on { getValue(BillingCachedProductIdPreference) } doReturn "test"
            on { setValue(eq(BillingCachedProductIdPreference), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionReady(inputUriString, points, null, action),
            state.transition(),
        )
        verify(mockUserPreferencesRepository, never()).setValue(
            eq(BillingCachedProductIdPreference),
            any(),
        )
    }

    @Test
    fun conversionSucceeded_billingStatusDoesNotContainAutomationFeature_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = CoordinatesOutput.CopyDecCoordsAutomation
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    refundable = true,
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf()
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn action
            on { setValue(eq(BillingCachedProductIdPreference), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertNull(state.transition())
        verify(mockUserPreferencesRepository).setValue(
            BillingCachedProductIdPreference,
            "test",
        )
    }

    @Test
    fun conversionSucceeded_billingStatusContainsAutomationFeature_returnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = CoordinatesOutput.CopyDecCoordsAutomation
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    refundable = true,
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn action
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionReady(inputUriString, points, null, action),
            state.transition(),
        )
        verify(mockUserPreferencesRepository).setValue(
            BillingCachedProductIdPreference,
            "test",
        )
    }

    @Test
    fun conversionSucceeded_billingStatusIsLoadingAndItBecomesPurchasedWithinTimeout_returnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = CoordinatesOutput.CopyDecCoordsAutomation
        val mockStatus = MutableStateFlow<BillingStatus>(BillingStatus.Loading())
        val mockBilling: Billing = mock {
            on { status } doReturn mockStatus
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn action
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points, billingStatusTimeout = 3.seconds)
        var res: State? = null
        launch {
            res = state.transition()
        }
        advanceTimeBy(2.seconds)
        mockStatus.value = BillingStatus.Purchased(
            product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
            refundable = true,
        )
        advanceUntilIdle()
        assertEquals(
            ActionReady(inputUriString, points, null, action),
            res,
        )
        verify(mockUserPreferencesRepository).setValue(
            BillingCachedProductIdPreference,
            "test",
        )
    }

    @Test
    fun conversionSucceeded_billingStatusIsNotPurchasedAndItBecomesPurchasedWithinTimeout_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = CoordinatesOutput.CopyDecCoordsAutomation
        val mockStatus = MutableStateFlow<BillingStatus>(BillingStatus.NotPurchased())
        val mockBilling: Billing = mock {
            on { status } doReturn mockStatus
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn action
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points, billingStatusTimeout = 3.seconds)
        var res: State? = null
        launch {
            res = state.transition()
        }
        advanceTimeBy(2.seconds)
        mockStatus.value = BillingStatus.Purchased(
            product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
            refundable = true,
        )
        advanceUntilIdle()
        assertNull(res)
        verify(mockUserPreferencesRepository, never()).setValue(
            eq(BillingCachedProductIdPreference),
            any(),
        )
    }

    @Test
    fun conversionSucceeded_billingStatusIsLoadingAndItBecomesPurchasedAfterTimeout_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = CoordinatesOutput.CopyDecCoordsAutomation
        val mockStatus = MutableStateFlow<BillingStatus>(BillingStatus.Loading())
        val mockBilling: Billing = mock {
            on { status } doReturn mockStatus
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn action
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points, billingStatusTimeout = 3.seconds)
        var res: State? = null
        launch {
            res = state.transition()
        }
        advanceTimeBy(5.seconds)
        mockStatus.value = BillingStatus.Purchased(
            product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
            refundable = true,
        )
        advanceUntilIdle()
        assertNull(res)
        verify(mockUserPreferencesRepository, never()).setValue(
            eq(BillingCachedProductIdPreference),
            any(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsCopyCoords_returnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = CoordinatesOutput.CopyDecCoordsAutomation
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    refundable = true,
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn action
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionReady(inputUriString, points, null, action),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsOpenApp_returnsActionWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS)
        val delay = 2.seconds
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    refundable = true,
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn action
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionWaiting(stateContext, inputUriString, points, null, action, delay),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsSaveGpx_returnsActionWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.SaveGpxPointsAutomation
        val delay = 2.seconds
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    refundable = true,
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn action
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionWaiting(stateContext, inputUriString, points, null, action, delay),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsShare_returnsActionWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GeoUriOutput.ShareGeoUriAutomation
        val delay = 2.seconds
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    refundable = true,
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn action
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionWaiting(stateContext, inputUriString, points, null, action, delay),
            state.transition(),
        )
    }

    @Test
    fun conversionFailed_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val state = ConversionFailed(R.string.conversion_failed_missing_url, inputUriString)
        assertNull(state.transition())
    }

    @Test
    fun actionWaiting_executionIsNotCancelled_waitsAndReturnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.SaveGpxPointsAutomation
        val stateContext = mockStateContext()
        val state = ActionWaiting(stateContext, inputUriString, points, 2, action, 3.seconds)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionReady(inputUriString, points, 2, action),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun actionWaiting_delayIsNotPositive_doesNotWaitAndReturnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.SaveGpxPointsAutomation
        val stateContext = mockStateContext()
        val state = ActionWaiting(stateContext, inputUriString, points, 2, action, (-1).seconds)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionReady(inputUriString, points, 2, action),
                state.transition(),
            )
        }
        assertEquals(0.seconds, workDuration)
    }

    @Test
    fun actionWaiting_executionIsCancelled_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.SaveGpxPointsAutomation
        val stateContext = mockStateContext()
        val state = ActionWaiting(stateContext, inputUriString, points, 2, action, 3.seconds)
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
            res,
            ActionFinished(inputUriString, points, action),
        )
    }

    @Test
    fun actionReady_actionIsCopyAutomation_returnsBasicActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = CoordinatesOutput.CopyDecCoordsAction()
        val state = ActionReady(inputUriString, points, 2, action)
        assertEquals(
            BasicActionReady(inputUriString, points, 2, action),
            state.transition(),
        )
    }

    @Test
    fun actionReady_actionIsCopyAction_returnsBasicActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = CoordinatesOutput.CopyDecCoordsAutomation
        val state = ActionReady(inputUriString, points, 2, action)
        assertEquals(
            BasicActionReady(inputUriString, points, 2, action),
            state.transition(),
        )
    }

    @Test
    fun actionReady_actionIsShareGpxRouteAutomation_returnsLocationRationaleRequested() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.ShareGpxRouteAutomation
        val state = ActionReady(inputUriString, points, 2, action)
        assertEquals(
            LocationRationaleRequested(inputUriString, points, 2, action),
            state.transition(),
        )
    }

    @Test
    fun actionReady_actionIsShareGpxRouteAction_returnsLocationRationaleRequested() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.ShareGpxRouteAction()
        val state = ActionReady(inputUriString, points, 2, action)
        assertEquals(
            LocationRationaleRequested(inputUriString, points, 2, action),
            state.transition(),
        )
    }

    @Test
    fun basicActionReady_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.SaveGpxPointsAutomation
        val state = BasicActionReady(inputUriString, points, 2, action)
        assertNull(state.transition())
    }

    @Test
    fun locationActionReady_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.ShareGpxRouteAction()
        val state = LocationActionReady(inputUriString, points, 2, action, WGS84Point(3.0, 4.0))
        assertNull(state.transition())
    }

    @Test
    fun actionRan_automationIsNoop_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = NoopAutomation
        val state = ActionRan(inputUriString, points, action, null)
        assertEquals(
            ActionFinished(inputUriString, points, action),
            state.transition(),
        )
    }

    @Test
    fun actionRan_automationIsCopyCoordsAndSuccessIsTrue_returnsActionSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = CoordinatesOutput.CopyDecCoordsAutomation
        val state = ActionRan(inputUriString, points, action, true)
        assertEquals(
            ActionSucceeded(inputUriString, points, action),
            state.transition(),
        )
    }

    @Test
    fun actionRan_automationIsOpenAppAndSuccessIsTrue_returnsActionSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS)
        val state = ActionRan(inputUriString, points, action, true)
        assertEquals(
            ActionSucceeded(inputUriString, points, action),
            state.transition(),
        )
    }

    @Test
    fun actionRan_automationIsOpenAppAndSuccessIsFalse_returnsActionFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS)
        val state = ActionRan(inputUriString, points, action, false)
        assertEquals(
            ActionFailed(inputUriString, points, action),
            state.transition(),
        )
    }

    @Test
    fun actionRan_automationIsSaveGpxAndSuccessIsTrue_returnsActionSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.SaveGpxPointsAutomation
        val state = ActionRan(inputUriString, points, action, true)
        assertEquals(
            ActionSucceeded(inputUriString, points, action),
            state.transition(),
        )
    }

    @Test
    fun actionRan_automationIsSaveGpxAndSuccessIsFalse_returnsActionFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.SaveGpxPointsAutomation
        val state = ActionRan(inputUriString, points, action, false)
        assertEquals(
            ActionFailed(inputUriString, points, action),
            state.transition(),
        )
    }

    @Test
    fun actionRan_automationIsShareAndSuccessIsTrue_returnsActionSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GeoUriOutput.ShareGeoUriAutomation
        val state = ActionRan(inputUriString, points, action, true)
        assertEquals(
            ActionSucceeded(inputUriString, points, action),
            state.transition(),
        )
    }

    @Test
    fun actionRan_automationIsShareAndSuccessIsFalse_returnsActionFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GeoUriOutput.ShareGeoUriAutomation
        val state = ActionRan(inputUriString, points, action, false)
        assertEquals(
            ActionFailed(inputUriString, points, action),
            state.transition(),
        )
    }

    @Test
    fun actionSucceeded_executionIsNotCancelled_waitsAndReturnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS)
        val state = ActionSucceeded(inputUriString, points, action)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionFinished(inputUriString, points, action),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun actionSucceeded_executionIsCancelled_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS)
        val state = ActionSucceeded(inputUriString, points, action)
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
            res,
            ActionFinished(inputUriString, points, action),
        )
    }

    @Test
    fun actionFailed_executionIsNotCancelled_waitsAndReturnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS)
        val state = ActionFailed(inputUriString, points, action)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionFinished(inputUriString, points, action),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun actionFailed_executionIsCancelled_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS)
        val state = ActionFailed(inputUriString, points, action)
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
            res,
            ActionFinished(inputUriString, points, action),
        )
    }

    @Test
    fun actionFinished_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(PackageNames.GOOGLE_MAPS)
        val state = ActionFinished(inputUriString, points, action)
        assertNull(state.transition())
    }

    @Test
    fun locationRationaleRequested_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.ShareGpxRouteAction()
        val state = LocationRationaleRequested(inputUriString, points, 2, action)
        assertNull(state.transition())
    }

    @Test
    fun locationRationaleShown_grant_returnsLocationRationaleConfirmed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.ShareGpxRouteAction()
        val state = LocationRationaleShown(inputUriString, points, 2, action)
        assertEquals(
            LocationRationaleConfirmed(inputUriString, points, 2, action),
            state.grant(false),
        )
    }

    @Test
    fun locationRationaleShown_deny_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.ShareGpxRouteAction()
        val state = LocationRationaleShown(inputUriString, points, 2, action)
        assertEquals(
            ActionFinished(inputUriString, points, action),
            state.deny(false),
        )
    }

    @Test
    fun locationRationaleConfirmed_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.ShareGpxRouteAction()
        val state = LocationRationaleConfirmed(inputUriString, points, 2, action)
        assertNull(state.transition())
    }

    @Test
    fun locationPermissionReceived_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.ShareGpxRouteAction()
        val state = LocationPermissionReceived(inputUriString, points, 2, action)
        assertNull(state.transition())
    }

    @Test
    fun locationReceived_locationIsNull_returnsLocationFindingFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.ShareGpxRouteAction()
        val state = LocationReceived(inputUriString, points, 2, action, null)
        assertEquals(
            LocationFindingFailed(inputUriString, points, action),
            state.transition(),
        )
    }

    @Test
    fun locationReceived_locationIsNotNull_returnsLocationActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.ShareGpxRouteAction()
        val location = WGS84Point(3.0, 4.0)
        val state = LocationReceived(inputUriString, points, 2, action, location)
        assertEquals(
            LocationActionReady(inputUriString, points, 2, action, location),
            state.transition(),
        )
    }

    @Test
    fun locationFindingFailed_executionIsNotCancelled_waitsAndReturnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.ShareGpxRouteAction()
        val state = LocationFindingFailed(inputUriString, points, action)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionFinished(inputUriString, points, action),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun locationFindingFailed_executionIsCancelled_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0))
        val action = GpxOutput.ShareGpxRouteAction()
        val state = LocationFindingFailed(inputUriString, points, action)
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
            res,
            ActionFinished(inputUriString, points, action),
        )
    }
}
