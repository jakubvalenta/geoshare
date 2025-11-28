package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.AutomationUserPreference
import page.ooooo.geoshare.data.local.preferences.ConnectionPermission
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.AndroidTools.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.conversion.*
import page.ooooo.geoshare.lib.outputs.CoordinatesOutput
import page.ooooo.geoshare.lib.outputs.GeoUriOutput
import page.ooooo.geoshare.lib.outputs.GpxOutput
import page.ooooo.geoshare.lib.outputs.NoopAutomation
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

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

    private val fakeLog = FakeLog()
    private val uriQuote = FakeUriQuote()
    private val fakeUserPreferencesRepository: UserPreferencesRepository = FakeUserPreferencesRepository()

    private fun mockStateContext(
        inputs: List<Input> = listOf(GeoUriInput, GoogleMapsInput),
        networkTools: NetworkTools = MockNetworkTools(),
        userPreferencesRepository: UserPreferencesRepository = fakeUserPreferencesRepository,
        log: ILog = fakeLog,
        uriQuote: UriQuote = this@ConversionStateTest.uriQuote,
    ) = ConversionStateContext(
        inputs = inputs,
        networkTools = networkTools,
        userPreferencesRepository = userPreferencesRepository,
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
            override val uriPattern: Pattern = Pattern.compile(".")
            override val documentation = Input.Documentation(nameResId = -1, inputs = emptyList())
            override fun parseUri(uri: Uri) = Pair(Position(), null)
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
                onBlocking { getValue(eq(ConnectionPermission)) } doThrow NotImplementedError()
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
                onBlocking { getValue(eq(ConnectionPermission)) } doThrow NotImplementedError()
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
                onBlocking { getValue(eq(ConnectionPermission)) } doThrow NotImplementedError()
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
                onBlocking { getValue(eq(ConnectionPermission)) } doReturn Permission.ALWAYS
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
                onBlocking { getValue(eq(ConnectionPermission)) } doReturn Permission.ASK
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
                onBlocking { getValue(eq(ConnectionPermission)) } doReturn Permission.NEVER
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
                onBlocking { setValue(eq(ConnectionPermission), any()) } doReturn Unit
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
                eq(ConnectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedUnshortenPermission_grantWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(ConnectionPermission), any()) } doReturn Unit
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
                ConnectionPermission,
                Permission.ALWAYS,
            )
        }

    @Test
    fun requestedUnshortenPermission_denyWithDoNotAskFalse_doesNotSavePreferenceAndReturnsDeniedConnectionPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(ConnectionPermission), any()) } doReturn Unit
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
                eq(ConnectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedUnshortenPermission_denyWithDoNotAskTrue_savesPreferenceAndDeniedConnectionPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { setValue(eq(ConnectionPermission), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = RequestedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput, uri)
        assertEquals(
            DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsInput),
            state.deny(true),
        )
        verify(mockUserPreferencesRepository).setValue(
            ConnectionPermission,
            Permission.NEVER,
        )
    }

    @Test
    fun grantedUnshortenPermission_inputUriStringIsInvalidURL_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://[invalid:ipv6]/"
            val uri = Uri.parse(inputUriString, uriQuote)
            val redirectUriString = "https://maps.google.com/foo-redirect"
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
                    override fun onRequestLocationHeader(url: URL): String? = if (url.toString() == inputUriString) {
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
                    override fun onRequestLocationHeader(url: URL): String? = if (url.toString() == inputUriString) {
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
                    override fun onRequestLocationHeader(url: URL): String? = if (url.toString() == inputUriString) {
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
                    override fun onRequestLocationHeader(url: URL): String? = if (url.toString() == inputUriString) {
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
                    override fun onRequestLocationHeader(url: URL): String? = if (url.toString() == inputUriString) {
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
                    override fun onRequestLocationHeader(url: URL): String? = if (url.toString() == inputUriString) {
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
            val redirectUriString = "https://maps.google.com/foo-redirect"
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
        val redirectUriString = "https://maps.google.com/foo-redirect"
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
        val redirectUriString = "foo-redirect"
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
        val redirectUriString = "https://maps.google.com/foo-redirect"
        val redirectUri = Uri.parse(redirectUriString, uriQuote)
        val mockInput = object : Input.HasShortUri {
            override val uriPattern: Pattern = Pattern.compile(".")
            override val documentation = Input.Documentation(nameResId = -1, inputs = emptyList())
            override val shortUriPattern: Pattern = Pattern.compile(".")
            override val shortUriMethod = Input.ShortUriMethod.GET
            override val permissionTitleResId = -1
            override val loadingIndicatorTitleResId = -1
            override fun parseUri(uri: Uri) = Pair(Position(), null)
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
    fun unshortenedUrl_parseUrlReturnsPositionWithPoint_returnsSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val positionFromUri = Position(Srs.WGS84, 1.0, 2.0)
        val mockGoogleMapsInput: GoogleMapsInput = mock {
            on { parseUri(any()) } doReturn Pair(positionFromUri, null)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(ConnectionPermission) } doThrow NotImplementedError()
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            inputs = listOf(mockGoogleMapsInput),
        )
        val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsInput, uri, null)
        assertEquals(
            ConversionSucceeded(stateContext, inputUriString, positionFromUri),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parseUriReturnsEmptyPositionAndNoUrl_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val mockGoogleMapsInput: GoogleMapsInput = mock {
            on { parseUri(any()) } doReturn Pair(Position(Srs.WGS84), null)
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
    fun unshortenedUrl_parseUriReturnsPositionWithQueryAndNoUrl_returnsParseHtmlFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val positionFromUri = Position(q = "bar")
        val mockGoogleMapsInput: GoogleMapsInput = mock {
            on { parseUri(any()) } doReturn Pair(positionFromUri, null)
        }
        val stateContext = mockStateContext(inputs = listOf(mockGoogleMapsInput))
        val state = UnshortenedUrl(
            stateContext, inputUriString, mockGoogleMapsInput, uri, Permission.ALWAYS
        )
        assertEquals(
            ParseHtmlFailed(stateContext, inputUriString, positionFromUri),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parseUriReturnsEmptyPositionAndUrlAndInputDoesNotSupportHtmlParsing_returnsParseHtmlFailed() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position()
            val htmlUriString = "$inputUriString/foo.html"
            val mockInput = object : Input {
                override val uriPattern: Pattern = Pattern.compile(".")
                override val documentation = Input.Documentation(nameResId = -1, inputs = emptyList())
                override fun parseUri(uri: Uri) = Pair(positionFromUri, htmlUriString)
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = UnshortenedUrl(
                stateContext, inputUriString, mockInput, uri, Permission.ALWAYS
            )
            assertEquals(
                ParseHtmlFailed(stateContext, inputUriString, positionFromUri),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPositionWithQueryAndUrlAndPermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
            val mockGoogleMapsInput: GoogleMapsInput = mock {
                on { parseUri(any()) } doReturn Pair(positionFromUri, htmlUriString)
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
                    positionFromUri,
                    htmlUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPositionWithQueryAndUrlAndPermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
            val mockGoogleMapsInput: GoogleMapsInput = mock {
                on { parseUri(any()) } doReturn Pair(positionFromUri, htmlUriString)
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
                    positionFromUri,
                    htmlUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPositionWithQueryAndUrlAndPermissionIsNever_returnsParseHtmlFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val positionFromUri = Position(q = "bar")
        val htmlUriString = "$inputUriString/foo.html"
        val mockGoogleMapsInput: GoogleMapsInput = mock {
            on { parseUri(any()) } doReturn Pair(positionFromUri, htmlUriString)
        }
        val stateContext = mockStateContext(inputs = listOf(mockGoogleMapsInput))
        val state = UnshortenedUrl(
            stateContext, inputUriString, mockGoogleMapsInput, uri, Permission.NEVER
        )
        assertEquals(
            ParseHtmlFailed(stateContext, inputUriString, positionFromUri),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parseUriReturnsPositionWithQueryAndUrlAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
            val mockGoogleMapsInput: GoogleMapsInput = mock {
                on { parseUri(any()) } doReturn Pair(positionFromUri, htmlUriString)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(ConnectionPermission) } doReturn Permission.ALWAYS
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
                    positionFromUri,
                    htmlUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPositionWithQueryAndUrlAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
            val mockGoogleMapsInput: GoogleMapsInput = mock {
                on { parseUri(any()) } doReturn Pair(positionFromUri, htmlUriString)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(ConnectionPermission) } doReturn Permission.ASK
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
                    positionFromUri,
                    htmlUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPositionWithQueryAndUrlAndPermissionIsNullAndPreferencePermissionIsNever_returnsParseHtmlFailed() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val position = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
            val mockGoogleMapsInput: GoogleMapsInput = mock {
                on { parseUri(any()) } doReturn Pair(position, htmlUriString)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(ConnectionPermission) } doReturn Permission.NEVER
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                inputs = listOf(mockGoogleMapsInput),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsInput, uri, null)
            assertEquals(
                ParseHtmlFailed(stateContext, inputUriString, position),
                state.transition(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val position = Position(q = "bar")
        val htmlUriString = "$inputUriString/foo.html"
        val stateContext = mockStateContext()
        val state = RequestedParseHtmlPermission(
            stateContext,

            inputUriString,
            GoogleMapsInput,
            uri,
            position,
            htmlUriString,
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedParseHtmlPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(ConnectionPermission), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseHtmlPermission(
                stateContext,

                inputUriString,
                GoogleMapsInput,
                uri,
                positionFromUri,
                htmlUriString,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,

                    inputUriString,
                    GoogleMapsInput,
                    uri,
                    positionFromUri,
                    htmlUriString,
                ),
                state.grant(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(ConnectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_grantWithDoNotAskTrue_savesPreferenceAndReturnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(ConnectionPermission), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseHtmlPermission(
                stateContext,

                inputUriString,
                GoogleMapsInput,
                uri,
                positionFromUri,
                htmlUriString,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,

                    inputUriString,
                    GoogleMapsInput,
                    uri,
                    positionFromUri,
                    htmlUriString,
                ),
                state.grant(true),
            )
            verify(mockUserPreferencesRepository).setValue(
                ConnectionPermission,
                Permission.ALWAYS,
            )
        }

    @Test
    fun requestedParseHtmlPermission_denyWithDoNotAskFalse_doesNotSavePreferenceAndReturnsParseHtmlFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val positionFromUri = Position(q = "bar")
        val htmlUriString = "$inputUriString/foo.html"
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { setValue(eq(ConnectionPermission), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = RequestedParseHtmlPermission(
            stateContext,

            inputUriString,
            GoogleMapsInput,
            uri,
            positionFromUri,
            htmlUriString,
        )
        assertEquals(
            ParseHtmlFailed(stateContext, inputUriString, positionFromUri),
            state.deny(false),
        )
        verify(mockUserPreferencesRepository, never()).setValue(
            eq(ConnectionPermission),
            any<Permission>(),
        )
    }

    @Test
    fun requestedParseHtmlPermission_denyWithDoNotAskTrue_savesPreferenceAndReturnsParseHtmlFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val positionFromUri = Position(q = "bar")
        val htmlUriString = "$inputUriString/foo.html"
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { setValue(eq(ConnectionPermission), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = RequestedParseHtmlPermission(
            stateContext,

            inputUriString,
            GoogleMapsInput,
            uri,
            positionFromUri,
            htmlUriString,
        )
        assertEquals(
            ParseHtmlFailed(stateContext, inputUriString, positionFromUri),
            state.deny(true),
        )
        verify(mockUserPreferencesRepository).setValue(
            ConnectionPermission,
            Permission.NEVER,
        )
    }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsNothing_returnsParseHtmlFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val positionFromUri = Position(q = "bar")
        val htmlUriString = "$inputUriString/foo.html"
        val html = "<html></html>"
        val mockInput = object : Input.HasHtml {
            override val uriPattern: Pattern = Pattern.compile(".")
            override val documentation = Input.Documentation(nameResId = -1, inputs = emptyList())
            override val permissionTitleResId = -1
            override val loadingIndicatorTitleResId = -1
            override fun parseUri(uri: Uri): Pair<Position, String?> {
                throw NotImplementedError()
            }

            override suspend fun parseHtml(channel: ByteReadChannel): Pair<Position, String?> = Pair(Position(), null)
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
            positionFromUri,
            htmlUriString,
        )
        assertEquals(
            ParseHtmlFailed(stateContext, inputUriString, positionFromUri),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlPermission_inputUriStringIsInvalidURL_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://[invalid:ipv6]/"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
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
                positionFromUri,
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
            val positionFromUri = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
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
                positionFromUri,
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
            val positionFromUri = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
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
                positionFromUri,
                htmlUriString,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,

                    inputUriString,
                    GoogleMapsInput,
                    uri,
                    positionFromUri,
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
            val positionFromUri = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
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
                positionFromUri,
                htmlUriString,
                retry = NetworkTools.Retry(1, tr),
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,

                    inputUriString,
                    GoogleMapsInput,
                    uri,
                    positionFromUri,
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
            val positionFromUri = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
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
                positionFromUri,
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
            val positionFromUri = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
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
                positionFromUri,
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
        val positionFromUri = Position(q = "bar")
        val htmlUriString = "maps.apple.com/foo"
        val positionFromHtml = Position(Srs.WGS84, 1.0, 2.0, q = "fromHtml")
        val mockInput = object : Input.HasHtml {
            override val uriPattern: Pattern = Pattern.compile(".")
            override val documentation = Input.Documentation(nameResId = -1, inputs = emptyList())
            override val permissionTitleResId = -1
            override val loadingIndicatorTitleResId = -1
            override fun parseUri(uri: Uri): Pair<Position, String?> {
                throw NotImplementedError()
            }

            override suspend fun parseHtml(channel: ByteReadChannel) = Pair(positionFromHtml, null)
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
            positionFromUri,
            htmlUriString,
        )
        assertEquals(
            ConversionSucceeded(stateContext, inputUriString, positionFromHtml),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsPoint_returnsSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val html = "<html></html>"
        val positionFromUri = Position(q = "bar")
        val htmlUriString = "https://api.apple.com/foo.json"
        val positionFromHtml = Position(Srs.WGS84, 1.0, 2.0, q = "fromHtml")
        val mockInput = object : Input.HasHtml {
            override val uriPattern: Pattern = Pattern.compile(".")
            override val documentation = Input.Documentation(nameResId = -1, inputs = emptyList())
            override val permissionTitleResId = -1
            override val loadingIndicatorTitleResId = -1
            override fun parseUri(uri: Uri): Pair<Position, String?> {
                throw NotImplementedError()
            }

            override suspend fun parseHtml(channel: ByteReadChannel) = Pair(positionFromHtml, null)
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
            positionFromUri,
            htmlUriString,
        )
        assertEquals(
            ConversionSucceeded(stateContext, inputUriString, positionFromHtml),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsAbsoluteUrl_returnsReceivedUriWithTheUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
            val redirectUriString = "https://maps.apple.com/foo-redirect"
            val redirectUri = Uri.parse(redirectUriString, uriQuote)
            val mockInput = object : Input.HasHtml {
                override val uriPattern: Pattern = Pattern.compile(".")
                override val documentation = Input.Documentation(nameResId = -1, inputs = emptyList())
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
                override fun parseUri(uri: Uri): Pair<Position, String?> {
                    throw NotImplementedError()
                }

                override suspend fun parseHtml(channel: ByteReadChannel) = Pair(Position(), redirectUriString)
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
                positionFromUri,
                htmlUriString,
            )
            assertEquals(
                ReceivedUri(stateContext, inputUriString, mockInput, redirectUri, Permission.ALWAYS),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsRelativeUrl_returnsReceivedUriWithTheUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
            val redirectUriString = "foo-redirect"
            val redirectUri = Uri.parse("$inputUriString/$redirectUriString", uriQuote)
            val html = "<html></html>"
            val mockInput = object : Input.HasHtml {
                override val uriPattern: Pattern = Pattern.compile(".")
                override val documentation = Input.Documentation(nameResId = -1, inputs = emptyList())
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
                override fun parseUri(uri: Uri): Pair<Position, String?> {
                    throw NotImplementedError()
                }

                override suspend fun parseHtml(channel: ByteReadChannel) = Pair(Position(), redirectUriString)
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
                positionFromUri,
                htmlUriString,
            )
            assertEquals(
                ReceivedUri(stateContext, inputUriString, mockInput, redirectUri, Permission.ALWAYS),
                state.transition(),
            )
        }

    @Test
    fun parseHtmlFailed_positionHasPoint_returnsConversionSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val stateContext = mockStateContext()
        val state = ParseHtmlFailed(stateContext, inputUriString, position)
        assertEquals(
            ConversionSucceeded(stateContext, inputUriString, position),
            state.transition(),
        )
    }

    @Test
    fun parseHtmlFailed_positionHasQuery_returnsConversionSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val position = Position(q = "bar")
        val stateContext = mockStateContext()
        val state = ParseHtmlFailed(stateContext, inputUriString, position)
        assertEquals(
            ConversionSucceeded(stateContext, inputUriString, position),
            state.transition(),
        )
    }

    @Test
    fun parseHtmlFailed_positionIsEmpty_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val position = Position()
        val stateContext = mockStateContext()
        val state = ParseHtmlFailed(stateContext, inputUriString, position)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsNoop_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = NoopAutomation
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(AutomationUserPreference) } doReturn action
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = ConversionSucceeded(stateContext, inputUriString, position)
        assertNull(state.transition())
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsCopyCoords_returnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = CoordinatesOutput.CopyDecCoordsAutomation
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(AutomationUserPreference) } doReturn action
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = ConversionSucceeded(stateContext, inputUriString, position)
        assertEquals(
            ActionReady(inputUriString, position, null, action),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsOpenApp_returnsActionWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(AutomationUserPreference) } doReturn action
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = ConversionSucceeded(stateContext, inputUriString, position)
        assertEquals(
            ActionWaiting(stateContext, inputUriString, position, null, action, 5.seconds),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsSaveGpx_returnsActionWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GpxOutput.SaveGpxPointsAutomation
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(AutomationUserPreference) } doReturn action
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = ConversionSucceeded(stateContext, inputUriString, position)
        assertEquals(
            ActionWaiting(stateContext, inputUriString, position, null, action, 5.seconds),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsShare_returnsActionWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GeoUriOutput.ShareGeoUriAutomation
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(AutomationUserPreference) } doReturn action
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = ConversionSucceeded(stateContext, inputUriString, position)
        assertEquals(
            ActionWaiting(stateContext, inputUriString, position, null, action, 5.seconds),
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
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GpxOutput.SaveGpxPointsAutomation
        val stateContext = mockStateContext()
        val state = ActionWaiting(stateContext, inputUriString, position, 2, action, 5.seconds)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionReady(inputUriString, position, 2, action),
                state.transition(),
            )
        }
        assertEquals(action.delay, workDuration)
    }

    @Test
    fun actionWaiting_delayIsNotPositive_doesNotWaitAndReturnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GpxOutput.SaveGpxPointsAutomation
        val stateContext = mockStateContext()
        val state = ActionWaiting(stateContext, inputUriString, position, 2, action, (-1).seconds)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionReady(inputUriString, position, 2, action),
                state.transition(),
            )
        }
        assertEquals(0.seconds, workDuration)
    }

    @Test
    fun actionWaiting_executionIsCancelled_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GpxOutput.SaveGpxPointsAutomation
        val stateContext = mockStateContext()
        val state = ActionWaiting(stateContext, inputUriString, position, 2, action, 5.seconds)
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
            ActionFinished(inputUriString, position, action),
        )
    }

    @Test
    fun actionReady_actionIsCopyAutomation_returnsBasicActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = CoordinatesOutput.CopyDecCoordsAction()
        val state = ActionReady(inputUriString, position, 2, action)
        assertEquals(
            BasicActionReady(inputUriString, position, 2, action),
            state.transition(),
        )
    }

    @Test
    fun actionReady_actionIsCopyAction_returnsBasicActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = CoordinatesOutput.CopyDecCoordsAutomation
        val state = ActionReady(inputUriString, position, 2, action)
        assertEquals(
            BasicActionReady(inputUriString, position, 2, action),
            state.transition(),
        )
    }

    @Test
    fun actionReady_actionIsShareGpxRouteAutomation_returnsLocationRationaleRequested() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GpxOutput.ShareGpxRouteAutomation
        val state = ActionReady(inputUriString, position, 2, action)
        assertEquals(
            LocationRationaleRequested(inputUriString, position, 2, action),
            state.transition(),
        )
    }

    @Test
    fun actionReady_actionIsShareGpxRouteAction_returnsLocationRationaleRequested() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GpxOutput.ShareGpxRouteAction()
        val state = ActionReady(inputUriString, position, 2, action)
        assertEquals(
            LocationRationaleRequested(inputUriString, position, 2, action),
            state.transition(),
        )
    }

    @Test
    fun basicActionReady_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GpxOutput.SaveGpxPointsAutomation
        val state = BasicActionReady(inputUriString, position, 2, action)
        assertNull(state.transition())
    }

    @Test
    fun locationActionReady_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GpxOutput.ShareGpxRouteAction()
        val state = LocationActionReady(inputUriString, position, 2, action, Point(Srs.WGS84, 3.0, 4.0))
        assertNull(state.transition())
    }

    @Test
    fun actionRan_automationIsNoop_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = NoopAutomation
        val state = ActionRan(inputUriString, position, action, null)
        assertEquals(
            ActionFinished(inputUriString, position, action),
            state.transition(),
        )
    }

    @Test
    fun actionRan_automationIsCopyCoordsAndSuccessIsTrue_returnsActionSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = CoordinatesOutput.CopyDecCoordsAutomation
        val state = ActionRan(inputUriString, position, action, true)
        assertEquals(
            ActionSucceeded(inputUriString, position, action),
            state.transition(),
        )
    }

    @Test
    fun actionRan_automationIsOpenAppAndSuccessIsTrue_returnsActionSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val state = ActionRan(inputUriString, position, action, true)
        assertEquals(
            ActionSucceeded(inputUriString, position, action),
            state.transition(),
        )
    }

    @Test
    fun actionRan_automationIsOpenAppAndSuccessIsFalse_returnsActionFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val state = ActionRan(inputUriString, position, action, false)
        assertEquals(
            ActionFailed(inputUriString, position, action),
            state.transition(),
        )
    }

    @Test
    fun actionRan_automationIsSaveGpxAndSuccessIsTrue_returnsActionSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GpxOutput.SaveGpxPointsAutomation
        val state = ActionRan(inputUriString, position, action, true)
        assertEquals(
            ActionSucceeded(inputUriString, position, action),
            state.transition(),
        )
    }

    @Test
    fun actionRan_automationIsSaveGpxAndSuccessIsFalse_returnsActionFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GpxOutput.SaveGpxPointsAutomation
        val state = ActionRan(inputUriString, position, action, false)
        assertEquals(
            ActionFailed(inputUriString, position, action),
            state.transition(),
        )
    }

    @Test
    fun actionRan_automationIsShareAndSuccessIsTrue_returnsActionSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GeoUriOutput.ShareGeoUriAutomation
        val state = ActionRan(inputUriString, position, action, true)
        assertEquals(
            ActionSucceeded(inputUriString, position, action),
            state.transition(),
        )
    }

    @Test
    fun actionRan_automationIsShareAndSuccessIsFalse_returnsActionFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GeoUriOutput.ShareGeoUriAutomation
        val state = ActionRan(inputUriString, position, action, false)
        assertEquals(
            ActionFailed(inputUriString, position, action),
            state.transition(),
        )
    }

    @Test
    fun actionSucceeded_executionIsNotCancelled_waitsAndReturnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val state = ActionSucceeded(inputUriString, position, action)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionFinished(inputUriString, position, action),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun actionSucceeded_executionIsCancelled_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val state = ActionSucceeded(inputUriString, position, action)
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
            ActionFinished(inputUriString, position, action),
        )
    }

    @Test
    fun actionFailed_executionIsNotCancelled_waitsAndReturnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val state = ActionFailed(inputUriString, position, action)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionFinished(inputUriString, position, action),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun actionFailed_executionIsCancelled_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val state = ActionFailed(inputUriString, position, action)
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
            ActionFinished(inputUriString, position, action),
        )
    }

    @Test
    fun actionFinished_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GeoUriOutput.ShareGeoUriWithAppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val state = ActionFinished(inputUriString, position, action)
        assertNull(state.transition())
    }

    @Test
    fun locationRationaleRequested_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GpxOutput.ShareGpxRouteAction()
        val state = LocationRationaleRequested(inputUriString, position, 2, action)
        assertNull(state.transition())
    }

    @Test
    fun locationRationaleShown_grant_returnsLocationRationaleConfirmed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GpxOutput.ShareGpxRouteAction()
        val state = LocationRationaleShown(inputUriString, position, 2, action)
        assertEquals(
            LocationRationaleConfirmed(inputUriString, position, 2, action),
            state.grant(false),
        )
    }

    @Test
    fun locationRationaleShown_deny_returnsActionFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GpxOutput.ShareGpxRouteAction()
        val state = LocationRationaleShown(inputUriString, position, 2, action)
        assertEquals(
            ActionFailed(inputUriString, position, action),
            state.deny(false),
        )
    }

    @Test
    fun locationRationaleConfirmed_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GpxOutput.ShareGpxRouteAction()
        val state = LocationRationaleConfirmed(inputUriString, position, 2, action)
        assertNull(state.transition())
    }

    @Test
    fun locationPermissionReceived_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val action = GpxOutput.ShareGpxRouteAction()
        val state = LocationPermissionReceived(inputUriString, position, 2, action)
        assertNull(state.transition())
    }
}
