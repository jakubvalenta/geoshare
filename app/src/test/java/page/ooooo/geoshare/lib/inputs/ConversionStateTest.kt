package page.ooooo.geoshare.lib.inputs

import android.content.Intent
import com.google.re2j.Pattern
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.io.RawSource
import kotlinx.io.Source
import kotlinx.io.asSource
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
import page.ooooo.geoshare.lib.IntentTools.Companion.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.conversion.*
import page.ooooo.geoshare.lib.outputs.Automation
import page.ooooo.geoshare.lib.outputs.CoordinatesOutputGroup
import page.ooooo.geoshare.lib.outputs.GeoUriOutputGroup
import page.ooooo.geoshare.lib.outputs.GpxOutputGroup
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
            block: (source: RawSource) -> T,
        ): T = block(onGetSource(url).byteInputStream().asSource())

        open fun onGetSource(url: URL): String {
            throw NotImplementedError()
        }
    }

    private val fakeLog = FakeLog()
    private val uriQuote = FakeUriQuote()
    private val fakeUserPreferencesRepository: UserPreferencesRepository = FakeUserPreferencesRepository()
    private val mockIntentTools: IntentTools = mock {
        on { getIntentUriString(any()) } doThrow NotImplementedError()
        on { createChooserIntent(any()) } doThrow NotImplementedError()
        on { queryApp(any(), any()) } doThrow NotImplementedError()
        on { queryGeoUriPackageNames(any()) } doReturn emptyList()
    }

    private fun mockStateContext(
        inputs: List<Input> = listOf(GeoInput, GoogleMapsInput),
        intentTools: IntentTools = mockIntentTools,
        networkTools: NetworkTools = MockNetworkTools(),
        userPreferencesRepository: UserPreferencesRepository = fakeUserPreferencesRepository,
        log: ILog = fakeLog,
        uriQuote: UriQuote = this@ConversionStateTest.uriQuote,
    ) = ConversionStateContext(
        inputs = inputs,
        intentTools = intentTools,
        networkTools = networkTools,
        userPreferencesRepository = userPreferencesRepository,
        log = log,
        uriQuote = uriQuote,
    )

    private fun mockRunContext() = ConversionRunContext(
        context = mock {
            on { packageManager } doReturn mock()
        },
        clipboard = mock(),
        saveGpxLauncher = mock(),
    )

    @Test
    fun initial_returnsNull() = runTest {
        val state = Initial()
        assertNull(state.transition())
    }

    @Test
    fun receivedIntent_intentDoesNotContainUrl_returnsConversionFailed() = runTest {
        val intent = Intent()
        val mockIntentTools: IntentTools = mock {
            on { getIntentUriString(intent) } doReturn null
        }
        val stateContext = mockStateContext(intentTools = mockIntentTools)
        val runContext = mockRunContext()
        val state = ReceivedIntent(stateContext, runContext, intent)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_missing_url, ""),
            state.transition(),
        )
    }

    @Test
    fun receivedIntent_intentContainsUrl_returnsReceivedUriString() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val intent = Intent()
        val mockIntentTools: IntentTools = mock {
            on { getIntentUriString(intent) } doReturn inputUriString
        }
        val stateContext = mockStateContext(intentTools = mockIntentTools)
        val runContext = mockRunContext()
        val state = ReceivedIntent(stateContext, runContext, intent)
        assertEquals(
            ReceivedUriString(stateContext, runContext, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isGeoUri_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "geo:1,2?q="
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = ReceivedUriString(stateContext, runContext, inputUriString)
        assertEquals(
            ReceivedUri(stateContext, runContext, inputUriString, GeoInput, uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isUriInTheMiddleOfTheInputString_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "FOO https://maps.google.com/foo BAR"
        val matchedInputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(matchedInputUriString, uriQuote)
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = ReceivedUriString(stateContext, runContext, inputUriString)
        assertEquals(
            ReceivedUri(stateContext, runContext, inputUriString, GoogleMapsInput, uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isValidUrl_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = ReceivedUriString(stateContext, runContext, inputUriString)
        assertEquals(
            ReceivedUri(stateContext, runContext, inputUriString, GoogleMapsInput, uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isNotValidUrl_returnsConversionFailed() = runTest {
        val inputUriString = "https://[invalid:ipv6]/"
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = ReceivedUriString(stateContext, runContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isEmpty_returnsConversionFailed() = runTest {
        val inputUriString = ""
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = ReceivedUriString(stateContext, runContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isMissingScheme_returnsConversionFailed() = runTest {
        val inputUriString = "maps.google.com/"
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = ReceivedUriString(stateContext, runContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isRelativeScheme_returnsConversionFailed() = runTest {
        val inputUriString = "//maps.google.com/"
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = ReceivedUriString(stateContext, runContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isNotHttpsScheme_returnsConversionFailed() = runTest {
        val inputUriString = "ftp://maps.google.com/"
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = ReceivedUriString(stateContext, runContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_uriIsUnsupportedMapService_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.example.com/foo"
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = ReceivedUriString(stateContext, runContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_uriIsFullUrl_returnsUnshortenedUrlAndPassesPermission() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = ReceivedUri(stateContext, runContext, inputUriString, GoogleMapsInput, uri, Permission.NEVER)
        assertEquals(
            UnshortenedUrl(stateContext, runContext, inputUriString, GoogleMapsInput, uri, Permission.NEVER),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_inputDoesNotSupportShortUri_returnsUnshortenedUrlAndPassesPermission() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val mockInput = object : Input {
            override val uriPattern: Pattern = Pattern.compile(".")
            override val documentation = Input.Documentation(nameResId = -1, inputs = emptyList())
            override fun parseUri(uri: Uri) = Pair(Position(), null)
        }
        val stateContext = mockStateContext(inputs = listOf(mockInput))
        val runContext = mockRunContext()
        val state = ReceivedUri(stateContext, runContext, inputUriString, mockInput, uri, Permission.NEVER)
        assertEquals(
            UnshortenedUrl(stateContext, runContext, inputUriString, mockInput, uri, Permission.NEVER),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsAlways_returnsGrantedUnshortenPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(eq(ConnectionPermission)) } doThrow NotImplementedError()
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val runContext = mockRunContext()
        val state = ReceivedUri(
            stateContext, runContext, inputUriString, GoogleMapsInput, uri, Permission.ALWAYS
        )
        assertEquals(
            GrantedUnshortenPermission(stateContext, runContext, inputUriString, GoogleMapsInput, uri),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsAsk_returnsRequestedUnshortenPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(eq(ConnectionPermission)) } doThrow NotImplementedError()
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val runContext = mockRunContext()
        val state = ReceivedUri(stateContext, runContext, inputUriString, GoogleMapsInput, uri, Permission.ASK)
        assertEquals(
            RequestedUnshortenPermission(stateContext, runContext, inputUriString, GoogleMapsInput, uri),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsNever_returnsDeniedUnshortenPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(eq(ConnectionPermission)) } doThrow NotImplementedError()
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val runContext = mockRunContext()
        val state = ReceivedUri(stateContext, runContext, inputUriString, GoogleMapsInput, uri, Permission.NEVER)
        assertEquals(
            DeniedConnectionPermission(stateContext, runContext, inputUriString, GoogleMapsInput),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(eq(ConnectionPermission)) } doReturn Permission.ALWAYS
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val runContext = mockRunContext()
            val state = ReceivedUri(stateContext, runContext, inputUriString, GoogleMapsInput, uri, null)
            assertEquals(
                GrantedUnshortenPermission(stateContext, runContext, inputUriString, GoogleMapsInput, uri),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(eq(ConnectionPermission)) } doReturn Permission.ASK
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val runContext = mockRunContext()
            val state = ReceivedUri(stateContext, runContext, inputUriString, GoogleMapsInput, uri, null)
            assertEquals(
                RequestedUnshortenPermission(stateContext, runContext, inputUriString, GoogleMapsInput, uri),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(eq(ConnectionPermission)) } doReturn Permission.NEVER
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val runContext = mockRunContext()
            val state = ReceivedUri(stateContext, runContext, inputUriString, GoogleMapsInput, uri, null)
            assertEquals(
                DeniedConnectionPermission(stateContext, runContext, inputUriString, GoogleMapsInput),
                state.transition(),
            )
        }

    @Test
    fun requestedUnshortenPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = RequestedUnshortenPermission(stateContext, runContext, inputUriString, GoogleMapsInput, uri)
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
            val runContext = mockRunContext()
            val state = RequestedUnshortenPermission(
                stateContext, runContext, inputUriString, GoogleMapsInput, uri
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, runContext, inputUriString, GoogleMapsInput, uri),
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
            val runContext = mockRunContext()
            val state = RequestedUnshortenPermission(
                stateContext, runContext, inputUriString, GoogleMapsInput, uri
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, runContext, inputUriString, GoogleMapsInput, uri),
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
            val runContext = mockRunContext()
            val state = RequestedUnshortenPermission(
                stateContext, runContext, inputUriString, GoogleMapsInput, uri
            )
            assertEquals(
                DeniedConnectionPermission(stateContext, runContext, inputUriString, GoogleMapsInput),
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
        val runContext = mockRunContext()
        val state = RequestedUnshortenPermission(stateContext, runContext, inputUriString, GoogleMapsInput, uri)
        assertEquals(
            DeniedConnectionPermission(stateContext, runContext, inputUriString, GoogleMapsInput),
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
            val runContext = mockRunContext()
            val state = GrantedUnshortenPermission(
                stateContext, runContext, inputUriString, GoogleMapsInput, uri
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
            val runContext = mockRunContext()
            val state = GrantedUnshortenPermission(
                stateContext, runContext, inputUriString, GoogleMapsInput, uri
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
            val runContext = mockRunContext()
            val state = GrantedUnshortenPermission(
                stateContext, runContext, inputUriString, GoogleMapsInput, uri
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext, runContext, inputUriString, GoogleMapsInput, uri, NetworkTools.Retry(1, tr),
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
            val runContext = mockRunContext()
            val state = GrantedUnshortenPermission(
                stateContext,
                runContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                retry = NetworkTools.Retry(1, tr),
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext, runContext, inputUriString, GoogleMapsInput, uri, NetworkTools.Retry(2, tr),
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
            val runContext = mockRunContext()
            val state = GrantedUnshortenPermission(
                stateContext, runContext, inputUriString, GoogleMapsInput, uri
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
            val runContext = mockRunContext()
            val state = GrantedUnshortenPermission(
                stateContext, runContext, inputUriString, GoogleMapsInput, uri
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
            val runContext = mockRunContext()
            val state = GrantedUnshortenPermission(
                stateContext, runContext, inputUriString, GoogleMapsInput, uri
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
            val runContext = mockRunContext()
            val state = GrantedUnshortenPermission(
                stateContext, runContext, inputUriString, GoogleMapsInput, uri
            )
            assertEquals(
                UnshortenedUrl(
                    stateContext,
                    runContext,
                    inputUriString,
                    GoogleMapsInput,
                    redirectUri,
                    Permission.ALWAYS
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
                override fun onRequestLocationHeader(url: URL): String? =
                    if (url.toString() == inputUriString) {
                        redirectUriString
                    } else {
                        super.onRequestLocationHeader(url)
                    }
            },
        )
        val runContext = mockRunContext()
        val state = GrantedUnshortenPermission(stateContext, runContext, inputUriString, GoogleMapsInput, uri)
        assertEquals(
            UnshortenedUrl(
                stateContext,
                runContext,
                inputUriString,
                GoogleMapsInput,
                redirectUri,
                Permission.ALWAYS
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
                override fun onRequestLocationHeader(url: URL): String? =
                    if (url.toString() == inputUriString) {
                        redirectUriString
                    } else {
                        super.onRequestLocationHeader(url)
                    }
            },
        )
        val runContext = mockRunContext()
        val state = GrantedUnshortenPermission(stateContext, runContext, inputUriString, GoogleMapsInput, uri)
        assertEquals(
            UnshortenedUrl(
                stateContext,
                runContext,
                inputUriString,
                GoogleMapsInput,
                redirectUri,
                Permission.ALWAYS
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
        val runContext = mockRunContext()
        val state = GrantedUnshortenPermission(stateContext, runContext, inputUriString, mockInput, uri)
        assertEquals(
            UnshortenedUrl(stateContext, runContext, inputUriString, mockInput, redirectUri, Permission.ALWAYS),
            state.transition(),
        )
    }

    @Test
    fun deniedConnectionPermission_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = DeniedConnectionPermission(stateContext, runContext, inputUriString, GoogleMapsInput)
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
        val runContext = mockRunContext()
        val state = UnshortenedUrl(stateContext, runContext, inputUriString, mockGoogleMapsInput, uri, null)
        assertEquals(
            ConversionSucceeded(stateContext, runContext, inputUriString, positionFromUri),
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
        val runContext = mockRunContext()
        val state = UnshortenedUrl(
            stateContext, runContext, inputUriString, GoogleMapsInput, uri, Permission.ALWAYS
        )
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_url_error, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parseUriReturnsPositionWithQueryAndNoUrl_returnsParseHtmlFailed() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val mockGoogleMapsInput: GoogleMapsInput = mock {
                on { parseUri(any()) } doReturn Pair(positionFromUri, null)
            }
            val stateContext = mockStateContext(inputs = listOf(mockGoogleMapsInput))
            val runContext = mockRunContext()
            val state = UnshortenedUrl(
                stateContext,
                runContext,
                inputUriString,
                mockGoogleMapsInput,
                uri,
                Permission.ALWAYS
            )
            assertEquals(
                ParseHtmlFailed(stateContext, runContext, inputUriString, positionFromUri),
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
            val runContext = mockRunContext()
            val state = UnshortenedUrl(
                stateContext, runContext, inputUriString, mockInput, uri, Permission.ALWAYS
            )
            assertEquals(
                ParseHtmlFailed(stateContext, runContext, inputUriString, positionFromUri),
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
            val runContext = mockRunContext()
            val state = UnshortenedUrl(
                stateContext,
                runContext,
                inputUriString,
                mockGoogleMapsInput,
                uri,
                Permission.ALWAYS
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    runContext,
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
            val runContext = mockRunContext()
            val state = UnshortenedUrl(
                stateContext,
                runContext,
                inputUriString,
                mockGoogleMapsInput,
                uri,
                Permission.ASK
            )
            assertEquals(
                RequestedParseHtmlPermission(
                    stateContext,
                    runContext,
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
    fun unshortenedUrl_parseUriReturnsPositionWithQueryAndUrlAndPermissionIsNever_returnsParseHtmlFailed() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val htmlUriString = "$inputUriString/foo.html"
            val mockGoogleMapsInput: GoogleMapsInput = mock {
                on { parseUri(any()) } doReturn Pair(positionFromUri, htmlUriString)
            }
            val stateContext = mockStateContext(inputs = listOf(mockGoogleMapsInput))
            val runContext = mockRunContext()
            val state = UnshortenedUrl(
                stateContext,
                runContext,
                inputUriString,
                mockGoogleMapsInput,
                uri,
                Permission.NEVER
            )
            assertEquals(
                ParseHtmlFailed(stateContext, runContext, inputUriString, positionFromUri),
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
            val runContext = mockRunContext()
            val state = UnshortenedUrl(stateContext, runContext, inputUriString, mockGoogleMapsInput, uri, null)
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    runContext,
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
            val runContext = mockRunContext()
            val state = UnshortenedUrl(stateContext, runContext, inputUriString, mockGoogleMapsInput, uri, null)
            assertEquals(
                RequestedParseHtmlPermission(
                    stateContext,
                    runContext,
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
            val runContext = mockRunContext()
            val state = UnshortenedUrl(stateContext, runContext, inputUriString, mockGoogleMapsInput, uri, null)
            assertEquals(
                ParseHtmlFailed(stateContext, runContext, inputUriString, position),
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
        val runContext = mockRunContext()
        val state = RequestedParseHtmlPermission(
            stateContext,
            runContext,
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
            val runContext = mockRunContext()
            val state = RequestedParseHtmlPermission(
                stateContext,
                runContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                positionFromUri,
                htmlUriString,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    runContext,
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
            val runContext = mockRunContext()
            val state = RequestedParseHtmlPermission(
                stateContext,
                runContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                positionFromUri,
                htmlUriString,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    runContext,
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
        val runContext = mockRunContext()
        val state = RequestedParseHtmlPermission(
            stateContext,
            runContext,
            inputUriString,
            GoogleMapsInput,
            uri,
            positionFromUri,
            htmlUriString,
        )
        assertEquals(
            ParseHtmlFailed(stateContext, runContext, inputUriString, positionFromUri),
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
        val runContext = mockRunContext()
        val state = RequestedParseHtmlPermission(
            stateContext,
            runContext,
            inputUriString,
            GoogleMapsInput,
            uri,
            positionFromUri,
            htmlUriString,
        )
        assertEquals(
            ParseHtmlFailed(stateContext, runContext, inputUriString, positionFromUri),
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

            override fun parseHtml(source: Source): Pair<Position, String?> = Pair(Position(), null)
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
        val runContext = mockRunContext()
        val state = GrantedParseHtmlPermission(
            stateContext,
            runContext,
            inputUriString,
            mockInput,
            uri,
            positionFromUri,
            htmlUriString,
        )
        assertEquals(
            ParseHtmlFailed(stateContext, runContext, inputUriString, positionFromUri),
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
            val runContext = mockRunContext()
            val state = GrantedParseHtmlPermission(
                stateContext,
                runContext,
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
            val runContext = mockRunContext()
            val state = GrantedParseHtmlPermission(
                stateContext,
                runContext,
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
            val runContext = mockRunContext()
            val state = GrantedParseHtmlPermission(
                stateContext,
                runContext,
                inputUriString,
                GoogleMapsInput,
                uri,
                positionFromUri,
                htmlUriString,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    runContext,
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
            val runContext = mockRunContext()
            val state = GrantedParseHtmlPermission(
                stateContext,
                runContext,
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
                    runContext,
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
            val runContext = mockRunContext()
            val state = GrantedParseHtmlPermission(
                stateContext,
                runContext,
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
            val runContext = mockRunContext()
            val state = GrantedParseHtmlPermission(
                stateContext,
                runContext,
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

            override fun parseHtml(source: Source) = Pair(positionFromHtml, null)
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
        val runContext = mockRunContext()
        val state = GrantedParseHtmlPermission(
            stateContext,
            runContext,
            inputUriString,
            mockInput,
            uri,
            positionFromUri,
            htmlUriString,
        )
        assertEquals(
            ConversionSucceeded(stateContext, runContext, inputUriString, positionFromHtml),
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

            override fun parseHtml(source: Source) = Pair(positionFromHtml, null)
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
        val runContext = mockRunContext()
        val state = GrantedParseHtmlPermission(
            stateContext,
            runContext,
            inputUriString,
            mockInput,
            uri,
            positionFromUri,
            htmlUriString,
        )
        assertEquals(
            ConversionSucceeded(stateContext, runContext, inputUriString, positionFromHtml),
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

                override fun parseHtml(source: Source) = Pair(Position(), redirectUriString)
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
            val runContext = mockRunContext()
            val state = GrantedParseHtmlPermission(
                stateContext,
                runContext,
                inputUriString,
                mockInput,
                uri,
                positionFromUri,
                htmlUriString,
            )
            assertEquals(
                ReceivedUri(stateContext, runContext, inputUriString, mockInput, redirectUri, Permission.ALWAYS),
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

                override fun parseHtml(source: Source) = Pair(Position(), redirectUriString)
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
            val runContext = mockRunContext()
            val state = GrantedParseHtmlPermission(
                stateContext,
                runContext,
                inputUriString,
                mockInput,
                uri,
                positionFromUri,
                htmlUriString,
            )
            assertEquals(
                ReceivedUri(stateContext, runContext, inputUriString, mockInput, redirectUri, Permission.ALWAYS),
                state.transition(),
            )
        }

    @Test
    fun parseHtmlFailed_positionHasPoint_returnsConversionSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = ParseHtmlFailed(stateContext, runContext, inputUriString, position)
        assertEquals(
            ConversionSucceeded(stateContext, runContext, inputUriString, position),
            state.transition(),
        )
    }

    @Test
    fun parseHtmlFailed_positionHasQuery_returnsConversionSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val position = Position(q = "bar")
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = ParseHtmlFailed(stateContext, runContext, inputUriString, position)
        assertEquals(
            ConversionSucceeded(stateContext, runContext, inputUriString, position),
            state.transition(),
        )
    }

    @Test
    fun parseHtmlFailed_positionIsEmpty_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val position = Position()
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = ParseHtmlFailed(stateContext, runContext, inputUriString, position)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsNoop_returnsAutomationReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = Automation.Noop
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(AutomationUserPreference) } doReturn Automation.Noop
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val runContext = mockRunContext()
        val state = ConversionSucceeded(stateContext, runContext, inputUriString, position)
        assertEquals(
            AutomationReady(stateContext, runContext, inputUriString, position, automationValue),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsCopyCoords_returnsAutomationReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = CoordinatesOutputGroup.CopyDecAutomation
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(AutomationUserPreference) } doReturn automationValue
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val runContext = mockRunContext()
        val state = ConversionSucceeded(stateContext, runContext, inputUriString, position)
        assertEquals(
            AutomationReady(stateContext, runContext, inputUriString, position, automationValue),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsOpenApp_returnsAutomationWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GeoUriOutputGroup.AppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(AutomationUserPreference) } doReturn automationValue
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val runContext = mockRunContext()
        val state = ConversionSucceeded(stateContext, runContext, inputUriString, position)
        assertEquals(
            AutomationWaiting(stateContext, runContext, inputUriString, position, automationValue),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsSaveGpx_returnsAutomationWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GpxOutputGroup.SaveAutomation
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(AutomationUserPreference) } doReturn automationValue
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val runContext = mockRunContext()
        val state = ConversionSucceeded(stateContext, runContext, inputUriString, position)
        assertEquals(
            AutomationWaiting(stateContext, runContext, inputUriString, position, automationValue),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsShare_returnsAutomationWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GeoUriOutputGroup.ChooserAutomation
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(AutomationUserPreference) } doReturn automationValue
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val runContext = mockRunContext()
        val state = ConversionSucceeded(stateContext, runContext, inputUriString, position)
        assertEquals(
            AutomationWaiting(stateContext, runContext, inputUriString, position, automationValue),
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
    fun automationWaiting_executionIsNotCancelled_waitsAndReturnsAutomationReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GpxOutputGroup.SaveAutomation
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = AutomationWaiting(stateContext, runContext, inputUriString, position, automationValue)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                AutomationReady(stateContext, runContext, inputUriString, position, automationValue),
                state.transition(),
            )
        }
        assertTrue(automationValue.delay.isPositive())
        assertEquals(automationValue.delay, workDuration)
    }

    @Test
    fun automationWaiting_executionIsCancelled_returnsAutomationFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GpxOutputGroup.SaveAutomation
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = AutomationWaiting(stateContext, runContext, inputUriString, position, automationValue)
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
            AutomationFinished(inputUriString, position, automationValue),
        )
    }

    @Test
    fun automationReady_automationIsNoop_returnsAutomationFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = Automation.Noop
        val stateContext = mockStateContext()
        val runContext = mockRunContext()
        val state = AutomationReady(stateContext, runContext, inputUriString, position, automationValue)
        assertEquals(
            AutomationFinished(inputUriString, position, automationValue),
            state.transition(),
        )
    }

    @Test
    fun automationReady_automationIsCopyCoords_callsIntentToolsAndReturnsAutomationSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = CoordinatesOutputGroup.CopyDecAutomation
        val runContext = mockRunContext()
        val mockIntentTools: IntentTools = mock {
            onBlocking { copyToClipboard(any(), any(), any()) } doThrow NotImplementedError()
            onBlocking {
                copyToClipboard(
                    eq(runContext.context),
                    eq(runContext.clipboard),
                    argThat { toString() == automationValue.getAction(position, uriQuote).text },
                )
            } doReturn Unit
        }
        val stateContext = mockStateContext(intentTools = mockIntentTools)
        val state = AutomationReady(stateContext, runContext, inputUriString, position, automationValue)
        assertEquals(
            AutomationSucceeded(inputUriString, position, automationValue),
            state.transition(),
        )
    }

    @Test
    fun automationReady_automationIsOpenApp_callsIntentToolsAndReturnsAutomationSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GeoUriOutputGroup.AppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val runContext = mockRunContext()
        val mockIntentTools: IntentTools = mock {
            on { openApp(any(), any(), any()) } doThrow NotImplementedError()
            on {
                openApp(
                    eq(runContext.context),
                    eq(automationValue.packageName),
                    argThat { toString() == automationValue.getAction(position, uriQuote).uriString },
                )
            } doReturn true
        }
        val stateContext = mockStateContext(intentTools = mockIntentTools)
        val state = AutomationReady(stateContext, runContext, inputUriString, position, automationValue)
        assertEquals(
            AutomationSucceeded(inputUriString, position, automationValue),
            state.transition(),
        )
    }

    @Test
    fun automationReady_automationIsOpenAppAndIntentToolsReturnFalse_returnsAutomationFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GeoUriOutputGroup.AppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val runContext = mockRunContext()
        val mockIntentTools: IntentTools = mock {
            on { openApp(any(), any(), any()) } doThrow NotImplementedError()
            on {
                openApp(
                    eq(runContext.context),
                    eq(automationValue.packageName),
                    argThat { toString() == automationValue.getAction(position, uriQuote).uriString },
                )
            } doReturn false
        }
        val stateContext = mockStateContext(intentTools = mockIntentTools)
        val state = AutomationReady(stateContext, runContext, inputUriString, position, automationValue)
        assertEquals(
            AutomationFailed(inputUriString, position, automationValue),
            state.transition(),
        )
    }

    @Test
    fun automationReady_automationIsSaveGpx_callsIntentToolsAndReturnsAutomationSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GpxOutputGroup.SaveAutomation
        val runContext = mockRunContext()
        val mockIntentTools: IntentTools = mock()
        doThrow(NotImplementedError::class).whenever(mockIntentTools).launchSaveGpx(
            any(),
            any(),
        )
        doReturn(true).whenever(mockIntentTools).launchSaveGpx(
            eq(runContext.context),
            eq(runContext.saveGpxLauncher),
        )
        val stateContext = mockStateContext(intentTools = mockIntentTools)
        val state = AutomationReady(stateContext, runContext, inputUriString, position, automationValue)
        assertEquals(
            AutomationSucceeded(inputUriString, position, automationValue),
            state.transition(),
        )
    }

    @Test
    fun automationReady_automationIsSaveGpxAndIntentToolsReturnFalse_returnsAutomationFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GpxOutputGroup.SaveAutomation
        val runContext = mockRunContext()
        val mockIntentTools: IntentTools = mock()
        doThrow(NotImplementedError::class).whenever(mockIntentTools).launchSaveGpx(
            any(),
            any(),
        )
        doReturn(false).whenever(mockIntentTools).launchSaveGpx(
            eq(runContext.context),
            eq(runContext.saveGpxLauncher),
        )
        val stateContext = mockStateContext(intentTools = mockIntentTools)
        val state = AutomationReady(stateContext, runContext, inputUriString, position, automationValue)
        assertEquals(
            AutomationFailed(inputUriString, position, automationValue),
            state.transition(),
        )
    }

    @Test
    fun automationReady_automationIsShare_callsIntentToolsAndReturnsAutomationSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GeoUriOutputGroup.ChooserAutomation
        val runContext = mockRunContext()
        val mockIntentTools: IntentTools = mock {
            on { openChooser(any(), any()) } doThrow NotImplementedError()
            on {
                openChooser(
                    eq(runContext.context),
                    argThat { toString() == automationValue.getAction(position, uriQuote).uriString },
                )
            } doReturn true
        }
        val stateContext = mockStateContext(intentTools = mockIntentTools)
        val state = AutomationReady(stateContext, runContext, inputUriString, position, automationValue)
        assertEquals(
            AutomationSucceeded(inputUriString, position, automationValue),
            state.transition(),
        )
    }

    @Test
    fun automationReady_automationIsShareAndIntentToolsReturnFalse_returnsAutomationFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GeoUriOutputGroup.ChooserAutomation
        val runContext = mockRunContext()
        val mockIntentTools: IntentTools = mock {
            on { openChooser(any(), any()) } doThrow NotImplementedError()
            on {
                openChooser(
                    eq(runContext.context),
                    argThat { toString() == automationValue.getAction(position, uriQuote).uriString },
                )
            } doReturn false
        }
        val stateContext = mockStateContext(intentTools = mockIntentTools)
        val state = AutomationReady(stateContext, runContext, inputUriString, position, automationValue)
        assertEquals(
            AutomationFailed(inputUriString, position, automationValue),
            state.transition(),
        )
    }

    @Test
    fun automationSucceeded_executionIsNotCancelled_waitsAndReturnsAutomationFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GeoUriOutputGroup.AppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val state = AutomationSucceeded(inputUriString, position, automationValue)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                AutomationFinished(inputUriString, position, automationValue),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun automationSucceeded_executionIsCancelled_returnsAutomationFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GeoUriOutputGroup.AppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val state = AutomationSucceeded(inputUriString, position, automationValue)
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
            AutomationFinished(inputUriString, position, automationValue),
        )
    }

    @Test
    fun automationFailed_executionIsNotCancelled_waitsAndReturnsAutomationFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GeoUriOutputGroup.AppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val state = AutomationFailed(inputUriString, position, automationValue)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                AutomationFinished(inputUriString, position, automationValue),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun automationFailed_executionIsCancelled_returnsAutomationFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GeoUriOutputGroup.AppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val state = AutomationFailed(inputUriString, position, automationValue)
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
            AutomationFinished(inputUriString, position, automationValue),
        )
    }

    @Test
    fun automationFinished_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val position = Position(Srs.WGS84, 1.0, 2.0)
        val automationValue = GeoUriOutputGroup.AppAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val state = AutomationFinished(inputUriString, position, automationValue)
        assertNull(state.transition())
    }
}
