package page.ooooo.geoshare

import android.content.Intent
import android.net.Uri
import com.google.re2j.Matcher
import io.ktor.client.engine.mock.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.connectionPermission
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.*
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException

class ConversionStateTest {

    private lateinit var fakeLog: ILog
    private lateinit var fakeUriQuote: FakeUriQuote
    private lateinit var googleMapsUrlConverter: GoogleMapsUrlConverter
    private lateinit var urlConverters: List<UrlConverter>
    private lateinit var mockIntentTools: IntentTools
    private lateinit var mockNetworkTools: NetworkTools
    private lateinit var fakeUserPreferencesRepository: UserPreferencesRepository

    @Before
    fun before() = runTest {
        fakeLog = FakeLog()
        fakeUriQuote = FakeUriQuote()

        googleMapsUrlConverter = GoogleMapsUrlConverter()
        urlConverters = listOf(googleMapsUrlConverter)

        mockIntentTools = mock()
        whenever(mockIntentTools.getIntentPosition(any<Intent>())).thenThrow(NotImplementedError::class.java)
        whenever(mockIntentTools.getIntentUriString(any<Intent>())).thenThrow(NotImplementedError::class.java)
        whenever(
            mockIntentTools.createChooserIntent(
                any<Uri>(),
            )
        ).thenThrow(NotImplementedError::class.java)

        mockNetworkTools = mock()
        whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
        whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)

        fakeUserPreferencesRepository = FakeUserPreferencesRepository()
    }

    @Test
    fun initial_returnsNull() = runTest {
        val state = Initial()
        assertNull(state.transition())
    }

    @Test
    fun receivedIntent_intentContainsGeoUri_returnsSucceeded() = runTest {
        val inputUriString = "geo:1,2?q=fromIntent"
        val uri = mockUri(inputUriString)
        val mockIntent: Intent = mock {
            on { data } doReturn uri
        }
        val position = Position("1", "2", q = "fromIntent")
        val mockIntentTools: IntentTools = mock()
        whenever(mockIntentTools.getIntentPosition(mockIntent)).thenReturn(position)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            parseUri = { uri },
        )
        val state = ReceivedIntent(stateContext, mockIntent)
        assertEquals(
            ConversionSucceeded(inputUriString, position),
            state.transition(),
        )
    }

    @Test
    fun receivedIntent_intentDoesNotContainUrl_returnsFailed() = runTest {
        val intent = Intent()
        val mockIntentTools: IntentTools = mock()
        whenever(mockIntentTools.getIntentPosition(intent)).thenReturn(null)
        whenever(mockIntentTools.getIntentUriString(intent)).thenReturn(null)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            parseUri = { mockUri(it) },
        )
        val state = ReceivedIntent(stateContext, intent)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_missing_url),
            state.transition(),
        )
    }

    @Test
    fun receivedIntent_intentContainsUrl_returnsReceivedUriString() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val intent = Intent()
        val mockIntentTools: IntentTools = mock()
        whenever(mockIntentTools.getIntentPosition(intent)).thenReturn(null)
        whenever(mockIntentTools.getIntentUriString(intent)).thenReturn(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            uriQuote = fakeUriQuote,
            parseUri = { mockUri(it) },
        )
        val state = ReceivedIntent(stateContext, intent)
        assertEquals(
            ReceivedUriString(stateContext, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isGeoUri_returnsSucceeded() = runTest {
        val inputUriString = "geo:1,2?q="
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            uriQuote = fakeUriQuote,
            parseUri = { mockUri(it) },
        )
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionSucceeded(inputUriString, Position("1", "2", q = "")),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isValidUrl_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = mockUri(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            uriQuote = fakeUriQuote,
            parseUri = { uri },
        )
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ReceivedUri(
                stateContext,
                inputUriString,
                uri,
                null,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isNotValidUrl_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "https://[invalid:ipv6]/"
        val uri = mockUri(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            uriQuote = fakeUriQuote,
            parseUri = { uri },
        )
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ReceivedUri(stateContext, inputUriString, uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isEmpty_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = ""
        val uri = mockUri(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            uriQuote = fakeUriQuote,
            parseUri = { uri },
        )
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ReceivedUri(stateContext, inputUriString, uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isMissingScheme_returnsReceivedUriWithSchemeAndPermissionNull() = runTest {
        val inputUriString = "www.example.com/"
        val uri = mockUri("https://www.example.com/")
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            uriQuote = fakeUriQuote,
            parseUri = { uriString -> if (uriString == "https://www.example.com/") uri else throw Exception() },
        )
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ReceivedUri(
                stateContext,
                inputUriString,
                uri,
                null,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isRelativeScheme_returnsReceivedUriWithHttpsSchemeAndPermissionNull() = runTest {
        val inputUriString = "//www.example.com/"
        val uri = mockUri("https://www.example.com/")
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            uriQuote = fakeUriQuote,
            parseUri = { uriString -> if (uriString == "https://www.example.com/") uri else throw Exception() },
        )
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ReceivedUri(
                stateContext,
                inputUriString,
                uri,
                null,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isNotHttpsScheme_returnsReceivedUriWithHttpsSchemeAndPermissionNull() = runTest {
        val inputUriString = "ftp://www.example.com/"
        val uri = mockUri("https://www.example.com/")
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            uriQuote = fakeUriQuote,
            parseUri = { uriString -> if (uriString == "https://www.example.com/") uri else throw Exception() },
        )
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ReceivedUri(
                stateContext,
                inputUriString,
                uri,
                null,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_uriIsUnsupportedMapService_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.example.com/foo"
        val uri = mockUri(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            parseUri = { uri },
        )
        val permission = Permission.NEVER
        val state = ReceivedUri(
            stateContext,
            inputUriString,
            uri,
            permission,
        )
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_uriIsFullUrl_returnsUnshortenedUrlAndPassesPermission() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = mockUri(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            parseUri = { uri },
        )
        val permission = Permission.NEVER
        val state = ReceivedUri(
            stateContext,
            inputUriString,
            uri,
            permission,
        )
        assertEquals(
            UnshortenedUrl(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
                permission,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsAlways_returnsGrantedUnshortenPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = mockUri(inputUriString)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
        whenever(
            mockUserPreferencesRepository.getValue(
                connectionPermission
            )
        ).thenThrow(NotImplementedError::class.java)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            mockUserPreferencesRepository,
            log = fakeLog,
            parseUri = { uri },
        )
        val state = ReceivedUri(
            stateContext,
            inputUriString,
            uri,
            Permission.ALWAYS,
        )
        assertEquals(
            GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsAsk_returnsRequestedUnshortenPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = mockUri(inputUriString)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
        whenever(
            mockUserPreferencesRepository.getValue(
                connectionPermission
            )
        ).thenThrow(NotImplementedError::class.java)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            mockUserPreferencesRepository,
            log = fakeLog,
            parseUri = { uri },
        )
        val state = ReceivedUri(
            stateContext,
            inputUriString,
            uri,
            Permission.ASK,
        )
        assertEquals(
            RequestedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsNever_returnsDeniedUnshortenPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = mockUri(inputUriString)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
        whenever(
            mockUserPreferencesRepository.getValue(
                connectionPermission
            )
        ).thenThrow(NotImplementedError::class.java)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            mockUserPreferencesRepository,
            log = fakeLog,
            parseUri = { uri },
        )
        val state = ReceivedUri(
            stateContext,
            inputUriString,
            uri,
            Permission.NEVER,
        )
        assertTrue(state.transition() is DeniedConnectionPermission)
    }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = mockUri(inputUriString)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.getValue(
                    connectionPermission
                )
            ).thenReturn(Permission.ALWAYS)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = ReceivedUri(
                stateContext,
                inputUriString,
                uri,
                null,
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext,
                    inputUriString,
                    googleMapsUrlConverter,
                    uri,
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = mockUri(inputUriString)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.getValue(
                    connectionPermission
                )
            ).thenReturn(Permission.ASK)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = ReceivedUri(
                stateContext,
                inputUriString,
                uri,
                null,
            )
            assertEquals(
                RequestedUnshortenPermission(
                    stateContext,
                    inputUriString,
                    googleMapsUrlConverter,
                    uri,
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = mockUri(inputUriString)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.getValue(
                    connectionPermission
                )
            ).thenReturn(Permission.NEVER)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = ReceivedUri(
                stateContext,
                inputUriString,
                uri,
                null,
            )
            assertTrue(state.transition() is DeniedConnectionPermission)
        }

    @Test
    fun requestedUnshortenPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = mockUri(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            parseUri = { uri },
        )
        val state = RequestedUnshortenPermission(
            stateContext,
            inputUriString,
            googleMapsUrlConverter,
            uri,
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedUnshortenPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = mockUri(inputUriString)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.setValue(
                    eq(connectionPermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri),
                state.grant(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedUnshortenPermission_grantWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = mockUri(inputUriString)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.setValue(
                    eq(connectionPermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri),
                state.grant(true),
            )
            verify(mockUserPreferencesRepository).setValue(
                connectionPermission,
                Permission.ALWAYS,
            )
        }

    @Test
    fun requestedUnshortenPermission_denyWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = mockUri(inputUriString)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.setValue(
                    eq(connectionPermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            )
            assertTrue(state.deny(false) is DeniedConnectionPermission)
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedUnshortenPermission_denyWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = mockUri(inputUriString)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.setValue(
                    eq(connectionPermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            )
            assertTrue(state.deny(true) is DeniedConnectionPermission)
            verify(mockUserPreferencesRepository).setValue(
                connectionPermission,
                Permission.NEVER,
            )
        }

    @Test
    fun grantedUnshortenPermission_gettingLocationHeaderThrowsCancellationException_returnsConversionFailedWithCancelledErrorMessage() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = mockUri(inputUriString)
            val mockEngine = MockEngine {
                throw CancellationException()
            }
            val mockNetworkTools = NetworkTools(mockEngine, fakeLog)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_cancelled),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_gettingLocationHeaderThrowsSocketTimeoutException_returnsConversionFailedWithConnectionErrorMessage() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = mockUri(inputUriString)
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(SocketTimeoutException::class.java)
            whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_unshorten_connection_error),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_gettingLocationHeaderThrowsMalformedURLException_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = mockUri(inputUriString)
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(MalformedURLException::class.java)
            whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_unshorten_error),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_gettingLocationHeaderThrowsUnexpectedResponseCodeException_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = mockUri(inputUriString)
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(UnexpectedResponseCodeException::class.java)
            whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_unshorten_error),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_gettingLocationHeaderSucceeds_returnsUnshortenedUrl() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = mockUri(inputUriString)
        val mockNetworkTools: NetworkTools = mock()
        whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenReturn(URL(inputUriString))
        whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            parseUri = { uri },
        )
        val state = GrantedUnshortenPermission(
            stateContext,
            inputUriString,
            googleMapsUrlConverter,
            uri,
        )
        assertEquals(
            UnshortenedUrl(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
                Permission.ALWAYS,
            ),
            state.transition(),
        )
    }

    @Test
    fun deniedConnectionPermission_returnsFailed() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            parseUri = { mockUri(it) },
        )
        val state = DeniedConnectionPermission(stateContext, inputUriString, googleMapsUrlConverter)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_connection_permission_denied),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parsingUrlFails_returnsFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = mockUri(inputUriString)
        val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock()
        whenever(mockGoogleMapsUrlConverter.conversionUriPattern).thenReturn(null)
        val mockNetworkTools: NetworkTools = mock()
        whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
        whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
        val stateContext = ConversionStateContext(
            listOf(mockGoogleMapsUrlConverter),
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            uriQuote = fakeUriQuote,
            parseUri = { uri },
        )
        val state = UnshortenedUrl(stateContext, inputUriString, googleMapsUrlConverter, uri, Permission.ALWAYS)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_url_error),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = mockUri(inputUriString)
            val mockUriMatcher: Matcher = mock()
            val mockUriPattern: ConversionAllUriPattern = mock {
                on { matches(any(), any(), any()) } doReturn listOf(ConversionMatcher(mockUriMatcher))
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                uriQuote = fakeUriQuote,
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, Permission.ALWAYS)
            assertEquals(
                GrantedParseHtmlPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = mockUri(inputUriString)
            val mockUriMatcher: Matcher = mock()
            val mockUriPattern: ConversionAllUriPattern = mock {
                on { matches(any(), any(), any()) } doReturn listOf(ConversionMatcher(mockUriMatcher))
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                uriQuote = fakeUriQuote,
                parseUri = { uri },
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, Permission.ASK)
            assertEquals(
                RequestedParseHtmlPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNever_returnsDeniedConnectionPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = mockUri(inputUriString)
            val mockUriMatcher: Matcher = mock()
            val mockUriPattern: ConversionAllUriPattern = mock {
                on { matches(any(), any(), any()) } doReturn listOf(ConversionMatcher(mockUriMatcher))
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                uriQuote = fakeUriQuote,
                parseUri = { uri },
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, Permission.NEVER)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = mockUri(inputUriString)
            val mockUriMatcher: Matcher = mock()
            val mockUriPattern: ConversionAllUriPattern = mock {
                on { matches(any(), any(), any()) } doReturn listOf(ConversionMatcher(mockUriMatcher))
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.getValue(connectionPermission)
            ).thenReturn(Permission.ALWAYS)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                uriQuote = fakeUriQuote,
                parseUri = { uri },
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, null)
            assertEquals(
                GrantedParseHtmlPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = mockUri(inputUriString)
            val mockUriMatcher: Matcher = mock()
            val mockUriPattern: ConversionAllUriPattern = mock {
                on { matches(any(), any(), any()) } doReturn listOf(ConversionMatcher(mockUriMatcher))
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.getValue(connectionPermission)
            ).thenReturn(Permission.ASK)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                uriQuote = fakeUriQuote,
                parseUri = { uri },
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, null)
            assertEquals(
                RequestedParseHtmlPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedConnectionPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = mockUri(inputUriString)
            val mockUriMatcher: Matcher = mock()
            val mockUriPattern: ConversionAllUriPattern = mock {
                on { matches(any(), any(), any()) } doReturn listOf(ConversionMatcher(mockUriMatcher))
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.getValue(connectionPermission)
            ).thenReturn(Permission.NEVER)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                uriQuote = fakeUriQuote,
                parseUri = { uri },
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, null)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsAlways_returnsGrantedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = mockUri(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUriMatcher: Matcher = mock {
                on { group("q") } doReturn positionFromUrl.q
            }
            val mockUriPattern: ConversionAllUriPattern = mock {
                on { matches(any(), any()) } doReturn listOf(ConversionMatcher(mockUriMatcher))
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                uriQuote = fakeUriQuote,
                parseUri = { uri },
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, Permission.ALWAYS)
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUriString,
                    mockGoogleMapsUrlConverter,
                    uri,
                    positionFromUrl,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsAsk_returnsRequestedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = mockUri(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUriMatcher: Matcher = mock {
                on { group("q") } doReturn positionFromUrl.q
            }
            val mockUriPattern: ConversionAllUriPattern = mock {
                on { matches(any(), any()) } doReturn listOf(ConversionMatcher(mockUriMatcher))
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                uriQuote = fakeUriQuote,
                parseUri = { uri },
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, Permission.ASK)
            assertEquals(
                RequestedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUriString,
                    mockGoogleMapsUrlConverter,
                    uri,
                    positionFromUrl,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsNever_returnsDeniedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = mockUri(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUriMatcher: Matcher = mock {
                on { group("q") } doReturn positionFromUrl.q
            }
            val mockUriPattern: ConversionAllUriPattern = mock {
                on { matches(any(), any()) } doReturn listOf(ConversionMatcher(mockUriMatcher))
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                uriQuote = fakeUriQuote,
                parseUri = { uri },
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, Permission.NEVER)
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(inputUriString, positionFromUrl),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = mockUri(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUriMatcher: Matcher = mock {
                on { group("q") } doReturn positionFromUrl.q
            }
            val mockUriPattern: ConversionAllUriPattern = mock {
                on { matches(any(), any()) } doReturn listOf(ConversionMatcher(mockUriMatcher))
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.getValue(connectionPermission)
            ).thenReturn(Permission.ALWAYS)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                uriQuote = fakeUriQuote,
                parseUri = { uri },
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, null)
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUriString,
                    mockGoogleMapsUrlConverter,
                    uri,
                    positionFromUrl,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = mockUri(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUriMatcher: Matcher = mock {
                on { group("q") } doReturn positionFromUrl.q
            }
            val mockUriPattern: ConversionAllUriPattern = mock {
                on { matches(any(), any()) } doReturn listOf(ConversionMatcher(mockUriMatcher))
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.getValue(connectionPermission)
            ).thenReturn(Permission.ASK)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                uriQuote = fakeUriQuote,
                parseUri = { uri },
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, null)
            assertEquals(
                RequestedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUriString,
                    mockGoogleMapsUrlConverter,
                    uri,
                    positionFromUrl,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = mockUri(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUriMatcher: Matcher = mock {
                on { group("q") } doReturn positionFromUrl.q
            }
            val mockUriPattern: ConversionAllUriPattern = mock {
                on { matches(any(), any()) } doReturn listOf(ConversionMatcher(mockUriMatcher))
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.getValue(connectionPermission)
            ).thenReturn(Permission.NEVER)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                uriQuote = fakeUriQuote,
                parseUri = { uri },
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, null)
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(inputUriString, positionFromUrl),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsNonZeroLat_returnsSucceedWithGeoUriFromUrl() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = mockUri(inputUriString)
        val positionFromUrl = Position("1", "0", q = "fromUrl")
        val mockUriMatcher: Matcher = mock {
            on { group("lat") } doReturn positionFromUrl.lat
            on { group("lon") } doReturn positionFromUrl.lon
            on { group("q") } doReturn positionFromUrl.q
        }
        val mockUriPattern: ConversionAllUriPattern = mock {
            on { matches(any(), any()) } doReturn listOf(ConversionMatcher(mockUriMatcher))
        }
        val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
            on { conversionUriPattern }.doReturn(mockUriPattern)
        }
        val mockNetworkTools: NetworkTools = mock()
        whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
        whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
        whenever(
            mockUserPreferencesRepository.getValue(connectionPermission)
        ).thenThrow(NotImplementedError::class.java)
        val stateContext = ConversionStateContext(
            listOf(mockGoogleMapsUrlConverter),
            mockIntentTools,
            mockNetworkTools,
            mockUserPreferencesRepository,
            log = fakeLog,
            uriQuote = fakeUriQuote,
            parseUri = { uri },
        )
        val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, null)
        assertEquals(
            ConversionSucceeded(inputUriString, positionFromUrl),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parsingUrlReturnsNonZeroLon_returnsSucceedWithGeoUriFromUrl() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = mockUri(inputUriString)
        val positionFromUrl = Position("1", "0", q = "fromUrl")
        val mockUriMatcher: Matcher = mock {
            on { group("lat") } doReturn positionFromUrl.lat
            on { group("lon") } doReturn positionFromUrl.lon
            on { group("q") } doReturn positionFromUrl.q
        }
        val mockUriPattern: ConversionAllUriPattern = mock {
            on { matches(any(), any()) } doReturn listOf(ConversionMatcher(mockUriMatcher))
        }
        val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
            on { conversionUriPattern }.doReturn(mockUriPattern)
        }
        val mockNetworkTools: NetworkTools = mock()
        whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
        whenever(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
        whenever(
            mockUserPreferencesRepository.getValue(connectionPermission)
        ).thenThrow(NotImplementedError::class.java)
        val stateContext = ConversionStateContext(
            listOf(mockGoogleMapsUrlConverter),
            mockIntentTools,
            mockNetworkTools,
            mockUserPreferencesRepository,
            log = fakeLog,
            uriQuote = fakeUriQuote,
            parseUri = { uri },
        )
        val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, null)
        assertEquals(
            ConversionSucceeded(inputUriString, positionFromUrl),
            state.transition(),
        )
    }

    @Test
    fun requestedParseHtmlPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = mockUri(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            parseUri = { uri },
        )
        val state = RequestedParseHtmlPermission(
            stateContext,
            inputUriString,
            googleMapsUrlConverter,
            uri,
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedParseHtmlPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = mockUri(inputUriString)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.setValue(
                    eq(connectionPermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    googleMapsUrlConverter,
                    uri,
                ),
                state.grant(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_grantWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = mockUri(inputUriString)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.setValue(
                    eq(connectionPermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    googleMapsUrlConverter,
                    uri,
                ),
                state.grant(true),
            )
            verify(mockUserPreferencesRepository).setValue(
                connectionPermission,
                Permission.ALWAYS,
            )
        }

    @Test
    fun requestedParseHtmlPermission_denyWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = mockUri(inputUriString)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.setValue(
                    eq(connectionPermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            )
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, googleMapsUrlConverter), state.deny(false)
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_denyWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = mockUri(inputUriString)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.setValue(
                    eq(connectionPermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            )
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, googleMapsUrlConverter), state.deny(true)
            )
            verify(mockUserPreferencesRepository).setValue(
                connectionPermission,
                Permission.NEVER,
            )
        }

    @Test
    fun grantedParseHtmlPermission_downloadingHtmlThrowsCancellationException_returnsConversionFailedWithCancelledMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = mockUri(inputUriString)
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(URL(inputUriString))).thenThrow(
                CancellationException()::class.java
            )
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_cancelled),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_downloadingHtmlThrowsSocketTimeoutException_returnsConversionFailedWithConnectionErrorMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = mockUri(inputUriString)
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(URL(inputUriString))).thenThrow(
                SocketTimeoutException::class.java
            )
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_connection_error),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_downloadingHtmlThrowsUnexpectedResponseCodeException_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = mockUri(inputUriString)
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(URL(inputUriString))).thenThrow(
                UnexpectedResponseCodeException::class.java
            )
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsParsed_returnsSucceededWithTheParsedGeoUri() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = mockUri(inputUriString)
        val html = "<html></html>"
        val positionFromHtml = Position("1", "2", q = "fromHtml")
        val mockHtmlMatcher: Matcher = mock {
            on { group("lat") } doReturn positionFromHtml.lat
            on { group("lon") } doReturn positionFromHtml.lon
            on { group("q") } doReturn positionFromHtml.q
        }
        val mockHtmlPattern: ConversionFirstHtmlPattern = mock {
            on { matches(any()) } doReturn listOf(ConversionMatcher(mockHtmlMatcher))
        }
        val mockAppleMapsUrlConverter: GoogleMapsUrlConverter = mock {
            on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
        }
        val mockNetworkTools: NetworkTools = mock()
        whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
        whenever(mockNetworkTools.getText(URL(inputUriString))).thenReturn(html)
        val stateContext = ConversionStateContext(
            listOf(mockAppleMapsUrlConverter),
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val state = GrantedParseHtmlPermission(
            stateContext,
            inputUriString,
            mockAppleMapsUrlConverter,
            uri,
        )
        assertEquals(
            ConversionSucceeded(inputUriString, positionFromHtml),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsRedirect_returnsReceivedUriWithTheRedirectUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = mockUri(inputUriString)
            val redirectUriString = "https://maps.apple.com/foo-redirect"
            val redirectUri = mockUri(redirectUriString)
            val html = "<html></html>"
            val mockHtmlRedirectMatcher: Matcher = mock {
                on { group("url") } doReturn redirectUriString
            }
            val mockHtmlPattern: ConversionFirstHtmlPattern = mock {
                on { matches(any()) } doReturn null
            }
            val mockHtmlRedirectPattern: ConversionFirstHtmlPattern = mock {
                on { matches(any()) } doReturn listOf(ConversionMatcher(mockHtmlRedirectMatcher))
            }
            val mockAppleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
                on { conversionHtmlRedirectPattern }.doReturn(mockHtmlRedirectPattern)
            }
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(URL(inputUriString))).thenReturn(html)
            val stateContext = ConversionStateContext(
                listOf(mockAppleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uriString -> if (uriString == redirectUriString) redirectUri else throw Exception() },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                mockAppleMapsUrlConverter,
                uri,
            )
            assertEquals(
                ReceivedUri(
                    stateContext,
                    inputUriString,
                    redirectUri,
                    Permission.ALWAYS,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parseHtmlFails_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = mockUri(inputUriString)
        val html = "<html></html>"
        val mockHtmlPattern: ConversionFirstHtmlPattern = mock {
            on { matches(any()) } doReturn null
        }
        val mockHtmlRedirectPattern: ConversionFirstHtmlPattern = mock {
            on { matches(any()) } doReturn null
        }
        val mockAppleMapsUrlConverter: GoogleMapsUrlConverter = mock {
            on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
            on { conversionHtmlRedirectPattern }.doReturn(mockHtmlRedirectPattern)
        }
        val mockNetworkTools: NetworkTools = mock()
        whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
        whenever(mockNetworkTools.getText(URL(inputUriString))).thenReturn(html)
        val stateContext = ConversionStateContext(
            listOf(mockAppleMapsUrlConverter),
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            parseUri = { uri },
        )
        val state = GrantedParseHtmlPermission(
            stateContext,
            inputUriString,
            mockAppleMapsUrlConverter,
            uri,
        )
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_html_error),
            state.transition(),
        )
    }

    @Test
    fun requestedParseHtmlToGetCoordsPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = mockUri(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            parseUri = { uri },
        )
        val state = RequestedParseHtmlToGetCoordsPermission(
            stateContext,
            inputUriString,
            googleMapsUrlConverter,
            uri,
            Position("1", "2", q = "fromUrl"),
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedParseHtmlToGetCoordsPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = mockUri(inputUriString)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.setValue(
                    eq(connectionPermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val positionFromUrl = Position(q = "fromUrl")
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
                positionFromUrl,
            )
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUriString,
                    googleMapsUrlConverter,
                    uri,
                    positionFromUrl,
                ),
                state.grant(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlToGetCoordsPermission_grantWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = mockUri(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.setValue(
                    eq(connectionPermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
                positionFromUrl,
            )
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUriString,
                    googleMapsUrlConverter,
                    uri,
                    positionFromUrl,
                ),
                state.grant(true),
            )
            verify(mockUserPreferencesRepository).setValue(
                connectionPermission,
                Permission.ALWAYS,
            )
        }

    @Test
    fun requestedParseHtmlToGetCoordsPermission_denyWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = mockUri(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.setValue(
                    eq(connectionPermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
                positionFromUrl,
            )
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(inputUriString, positionFromUrl), state.deny(false)
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlToGetCoordsPermission_denyWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = mockUri(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock()
            whenever(
                mockUserPreferencesRepository.setValue(
                    eq(connectionPermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
                positionFromUrl,
            )
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(inputUriString, positionFromUrl),
                state.deny(true),
            )
            verify(mockUserPreferencesRepository).setValue(
                connectionPermission,
                Permission.NEVER,
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_downloadingHtmlThrowsCancellationException_returnsConversionFailedWithCancelledErrorMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = mockUri(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(URL(inputUriString))).thenThrow(
                CancellationException()::class.java
            )
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
                positionFromUrl,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_cancelled),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_downloadingHtmlThrowsSocketTimeoutException_returnsConversionFailedWithConnectionErrorMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = mockUri(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(URL(inputUriString))).thenThrow(
                SocketTimeoutException::class.java
            )
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
                positionFromUrl,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_connection_error),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_downloadingHtmlThrowsUnexpectedResponseCodeException_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = mockUri(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(URL(inputUriString))).thenThrow(
                UnexpectedResponseCodeException::class.java
            )
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
                positionFromUrl,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_parseHtmlReturnsParsed_returnsSucceededWithTheParsedGeoUri() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = mockUri(inputUriString)
        val html = "<html></html>"
        val positionFromUrl = Position(q = "fromUrl")
        val positionFromHtml = Position(q = "fromHtml")
        val mockHtmlMatcher: Matcher = mock {
            on { group("q") } doReturn positionFromHtml.q
        }
        val mockHtmlPattern: ConversionFirstHtmlPattern = mock {
            on { matches(any()) } doReturn listOf(ConversionMatcher(mockHtmlMatcher))
        }
        val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
            on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
        }
        val mockNetworkTools: NetworkTools = mock()
        whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
        whenever(mockNetworkTools.getText(URL(inputUriString))).thenReturn(html)
        val stateContext = ConversionStateContext(
            listOf(mockGoogleMapsUrlConverter),
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            parseUri = { uri },
        )
        val state = GrantedParseHtmlToGetCoordsPermission(
            stateContext,
            inputUriString,
            mockGoogleMapsUrlConverter,
            uri,
            positionFromUrl,
        )
        assertEquals(
            ConversionSucceeded(inputUriString, positionFromHtml),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_parseHtmlReturnsRedirect_returnsReceivedUriWithTheRedirectUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://g.co/kgs/foo"
            val uri = mockUri(inputUriString)
            val html = "<html></html>"
            val positionFromUrl = Position(q = "fromUrl")
            val mockHtmlPattern: ConversionFirstHtmlPattern = mock {
                on { matches(any()) } doReturn null
            }
            val mockHtmlRedirectMatcher: Matcher = mock {
                on { group("url") } doReturn inputUriString
            }
            val mockHtmlRedirectPattern: ConversionFirstHtmlPattern = mock {
                on { matches(any()) } doReturn listOf(ConversionMatcher(mockHtmlRedirectMatcher))
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
                on { conversionHtmlRedirectPattern }.doReturn(mockHtmlRedirectPattern)
            }
            val mockNetworkTools: NetworkTools = mock()
            whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
            whenever(mockNetworkTools.getText(URL(inputUriString))).thenReturn(html)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
                parseUri = { uri },
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                mockGoogleMapsUrlConverter,
                uri,
                positionFromUrl,
            )
            assertEquals(
                ReceivedUri(
                    stateContext,
                    inputUriString,
                    uri,
                    Permission.ALWAYS,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_parseHtmlFails_returnsSucceededWithGeoUriFromUrl() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = mockUri(inputUriString)
        val html = "<html></html>"
        val positionFromUrl = Position(q = "fromUrl")
        val mockHtmlPattern: ConversionFirstHtmlPattern = mock {
            on { matches(any()) } doReturn null
        }
        val mockHtmlRedirectPattern: ConversionFirstHtmlPattern = mock {
            on { matches(any()) } doReturn null
        }
        val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
            on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
            on { conversionHtmlRedirectPattern }.doReturn(mockHtmlRedirectPattern)
        }
        val mockNetworkTools: NetworkTools = mock()
        whenever(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
        whenever(mockNetworkTools.getText(URL(inputUriString))).thenReturn(html)
        val stateContext = ConversionStateContext(
            listOf(mockGoogleMapsUrlConverter),
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
            parseUri = { uri },
        )
        val state = GrantedParseHtmlToGetCoordsPermission(
            stateContext,
            inputUriString,
            mockGoogleMapsUrlConverter,
            uri,
            positionFromUrl,
        )
        assertEquals(
            ConversionSucceeded(inputUriString, positionFromUrl),
            state.transition(),
        )
    }

    @Test
    fun deniedParseHtmlToGetCoordsPermission_returnsSucceededWithGeoUriFromUrl() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val positionFromUrl = Position(q = "fromUrl")
        val state = DeniedParseHtmlToGetCoordsPermission(inputUriString, positionFromUrl)
        assertEquals(
            ConversionSucceeded(inputUriString, positionFromUrl),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_returnsNull() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val state = ConversionSucceeded(inputUriString, Position("1", "2"))
        assertNull(state.transition())
    }

    @Test
    fun conversionFailed_returnsNull() = runTest {
        val state = ConversionFailed(R.string.conversion_failed_missing_url)
        assertNull(state.transition())
    }
}
