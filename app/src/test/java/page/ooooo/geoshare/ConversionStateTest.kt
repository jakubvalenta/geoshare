package page.ooooo.geoshare

import android.content.Intent
import android.net.Uri
import io.ktor.client.engine.mock.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
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
    private lateinit var fakeOnMessage: (message: Message) -> Unit
    private lateinit var fakeUriQuote: FakeUriQuote
    private lateinit var googleMapsUrlConverter: GoogleMapsUrlConverter
    private lateinit var urlConverters: List<UrlConverter>
    private lateinit var mockIntentTools: IntentTools
    private lateinit var mockNetworkTools: NetworkTools
    private lateinit var fakeUserPreferencesRepository: UserPreferencesRepository

    @Before
    fun before() = runTest {
        fakeLog = FakeLog()
        fakeOnMessage = {}
        fakeUriQuote = FakeUriQuote()

        googleMapsUrlConverter = GoogleMapsUrlConverter(fakeLog, fakeUriQuote)
        urlConverters = listOf(googleMapsUrlConverter)

        mockIntentTools = Mockito.mock(IntentTools::class.java)
        Mockito.`when`(mockIntentTools.getIntentPosition(any<Intent>())).thenThrow(NotImplementedError::class.java)
        Mockito.`when`(mockIntentTools.getIntentUriString(any<Intent>())).thenThrow(NotImplementedError::class.java)
        Mockito.`when`(
            mockIntentTools.createChooserIntent(
                any<Uri>(),
            )
        ).thenThrow(NotImplementedError::class.java)

        mockNetworkTools = Mockito.mock(NetworkTools::class.java)
        Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
        Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)

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
        val mockUri = Mockito.mock(Uri::class.java)
        Mockito.`when`(mockUri.toString()).thenReturn(inputUriString)
        val mockIntent = Mockito.mock(Intent::class.java)
        Mockito.`when`(mockIntent.data).thenReturn(mockUri)
        val position = Position("1", "2", q = "fromIntent")
        val mockIntentTools = Mockito.mock(IntentTools::class.java)
        Mockito.`when`(mockIntentTools.getIntentPosition(mockIntent)).thenReturn(position)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
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
        val mockIntentTools = Mockito.mock(IntentTools::class.java)
        Mockito.`when`(mockIntentTools.getIntentPosition(intent)).thenReturn(null)
        Mockito.`when`(mockIntentTools.getIntentUriString(intent)).thenReturn(null)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
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
        val mockIntentTools = Mockito.mock(IntentTools::class.java)
        Mockito.`when`(mockIntentTools.getIntentPosition(intent)).thenReturn(null)
        Mockito.`when`(mockIntentTools.getIntentUriString(intent)).thenReturn(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val state = ReceivedIntent(stateContext, intent, fakeUriQuote)
        assertEquals(
            ReceivedUriString(stateContext, inputUriString, fakeUriQuote),
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
        )
        val state = ReceivedUriString(stateContext, inputUriString, fakeUriQuote)
        assertEquals(
            ConversionSucceeded(inputUriString, Position("1", "2", q = "")),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isValidUrl_returnsReceivedUrlWithPermissionNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val url = URL(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val state = ReceivedUriString(stateContext, inputUriString, fakeUriQuote)
        assertEquals(
            ReceivedUrl(
                stateContext,
                inputUriString,
                url,
                null,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isNotValidUrl_returnsFailed() = runTest {
        val inputUriString = "https://[invalid:ipv6]/"
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val state = ReceivedUriString(stateContext, inputUriString, fakeUriQuote)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_invalid_url),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isEmpty_returnsFailed() = runTest {
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val inputUriString = ""
        val state = ReceivedUriString(stateContext, inputUriString, fakeUriQuote)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_invalid_url),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isMissingScheme_returnsReceivedUrlWithSchemeAndPermissionNull() = runTest {
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val inputUriString = "www.example.com/"
        val state = ReceivedUriString(stateContext, inputUriString, fakeUriQuote)
        assertEquals(
            ReceivedUrl(
                stateContext,
                inputUriString,
                URL("https://www.example.com/"),
                null,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isRelativeScheme_returnsReceivedUrlWithHttpsSchemeAndPermissionNull() = runTest {
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val inputUriString = "//www.example.com/"
        val state = ReceivedUriString(stateContext, inputUriString, fakeUriQuote)
        assertEquals(
            ReceivedUrl(
                stateContext,
                inputUriString,
                URL("https://www.example.com/"),
                null,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isNotHttpsScheme_returnsReceivedUrlWithHttpsSchemeAndPermissionNull() = runTest {
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val inputUriString = "ftp://www.example.com/"
        val state = ReceivedUriString(stateContext, inputUriString, fakeUriQuote)
        assertEquals(
            ReceivedUrl(
                stateContext,
                inputUriString,
                URL("https://www.example.com/"),
                null,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUrl_urlIsUnsupportedMapService_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.example.com/foo"
        val url = URL(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val permission = Permission.NEVER
        val state = ReceivedUrl(
            stateContext,
            inputUriString,
            url,
            permission,
        )
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service),
            state.transition(),
        )
    }

    @Test
    fun receivedUrl_urlIsFullUrl_returnsUnshortenedUrlAndPassesPermission() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val url = URL(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val permission = Permission.NEVER
        val state = ReceivedUrl(
            stateContext,
            inputUriString,
            url,
            permission,
        )
        assertEquals(
            UnshortenedUrl(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
                permission,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsAlways_returnsGrantedUnshortenPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val url = URL(inputUriString)
        val mockUserPreferencesRepository = Mockito.mock(
            FakeUserPreferencesRepository::class.java
        )
        Mockito.`when`(
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
        )
        val state = ReceivedUrl(
            stateContext,
            inputUriString,
            url,
            Permission.ALWAYS,
        )
        assertEquals(
            GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsAsk_returnsRequestedUnshortenPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val url = URL(inputUriString)
        val mockUserPreferencesRepository = Mockito.mock(
            FakeUserPreferencesRepository::class.java
        )
        Mockito.`when`(
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
        )
        val state = ReceivedUrl(
            stateContext,
            inputUriString,
            url,
            Permission.ASK,
        )
        assertEquals(
            RequestedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNever_returnsDeniedUnshortenPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val url = URL(inputUriString)
        val mockUserPreferencesRepository = Mockito.mock(
            FakeUserPreferencesRepository::class.java
        )
        Mockito.`when`(
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
        )
        val state = ReceivedUrl(
            stateContext,
            inputUriString,
            url,
            Permission.NEVER,
        )
        assertTrue(state.transition() is DeniedConnectionPermission)
    }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val url = URL(inputUriString)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
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
            )
            val state = ReceivedUrl(
                stateContext,
                inputUriString,
                url,
                null,
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext,
                    inputUriString,
                    googleMapsUrlConverter,
                    url,
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val url = URL(inputUriString)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
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
            )
            val state = ReceivedUrl(
                stateContext,
                inputUriString,
                url,
                null,
            )
            assertEquals(
                RequestedUnshortenPermission(
                    stateContext,
                    inputUriString,
                    googleMapsUrlConverter,
                    url,
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val url = URL(inputUriString)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
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
            )
            val state = ReceivedUrl(
                stateContext,
                inputUriString,
                url,
                null,
            )
            assertTrue(state.transition() is DeniedConnectionPermission)
        }

    @Test
    fun requestedUnshortenPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val url = URL(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val state = RequestedUnshortenPermission(
            stateContext,
            inputUriString,
            googleMapsUrlConverter,
            url,
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedUnshortenPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val url = URL(inputUriString)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
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
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, url),
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
            val url = URL(inputUriString)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
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
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, url),
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
            val url = URL(inputUriString)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
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
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
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
            val url = URL(inputUriString)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
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
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
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
            val url = URL(inputUriString)
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
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
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
            val url = URL(inputUriString)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(SocketTimeoutException::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
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
            val url = URL(inputUriString)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(MalformedURLException::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
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
            val url = URL(inputUriString)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(UnexpectedResponseCodeException::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_unshorten_error),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_gettingLocationHeaderSucceeds_returnsUnshortenedUrl() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val url = URL(inputUriString)
        val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
        Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>())).thenReturn(url)
        Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val state = GrantedUnshortenPermission(
            stateContext,
            inputUriString,
            googleMapsUrlConverter,
            url,
        )
        assertEquals(
            UnshortenedUrl(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
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
        val url = URL(inputUriString)
        val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
        Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url)).thenReturn(null)
        val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
        Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
        Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
        val stateContext = ConversionStateContext(
            listOf(mockGoogleMapsUrlConverter),
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val state = UnshortenedUrl(stateContext, inputUriString, googleMapsUrlConverter, url, Permission.ALWAYS)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_url_error),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val url = URL(inputUriString)
            val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url)).thenReturn(ParseUrlResult.RequiresHtmlParsing())
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state =
                UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, url, Permission.ALWAYS)
            assertEquals(
                GrantedParseHtmlPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter, url),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val url = URL(inputUriString)
            val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url)).thenReturn(ParseUrlResult.RequiresHtmlParsing())
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state =
                UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, url, Permission.ASK)
            assertEquals(
                RequestedParseHtmlPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter, url),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNever_returnsDeniedConnectionPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val url = URL(inputUriString)
            val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url)).thenReturn(ParseUrlResult.RequiresHtmlParsing())
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state =
                UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, url, Permission.NEVER)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val url = URL(inputUriString)
            val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url)).thenReturn(ParseUrlResult.RequiresHtmlParsing())
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(connectionPermission)
            ).thenReturn(Permission.ALWAYS)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                GrantedParseHtmlPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter, url),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val url = URL(inputUriString)
            val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url)).thenReturn(ParseUrlResult.RequiresHtmlParsing())
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(connectionPermission)
            ).thenReturn(Permission.ASK)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                RequestedParseHtmlPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter, url),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedConnectionPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val url = URL(inputUriString)
            val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url)).thenReturn(ParseUrlResult.RequiresHtmlParsing())
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(connectionPermission)
            ).thenReturn(Permission.NEVER)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsAlways_returnsGrantedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val url = URL(inputUriString)
            val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
            val positionFromUrl = Position(q = "fromUrl")
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsingToGetCoords(positionFromUrl))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state = UnshortenedUrl(
                stateContext,
                inputUriString,
                mockGoogleMapsUrlConverter,
                url,
                Permission.ALWAYS,
            )
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUriString,
                    mockGoogleMapsUrlConverter,
                    url,
                    positionFromUrl,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsAsk_returnsRequestedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val url = URL(inputUriString)
            val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
            val positionFromUrl = Position(q = "fromUrl")
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsingToGetCoords(positionFromUrl))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state = UnshortenedUrl(
                stateContext,
                inputUriString,
                mockGoogleMapsUrlConverter,
                url,
                Permission.ASK,
            )
            assertEquals(
                RequestedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUriString,
                    mockGoogleMapsUrlConverter,
                    url,
                    positionFromUrl,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsNever_returnsDeniedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val url = URL(inputUriString)
            val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
            val positionFromUrl = Position(q = "fromUrl")
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsingToGetCoords(positionFromUrl))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state = UnshortenedUrl(
                stateContext,
                inputUriString,
                mockGoogleMapsUrlConverter,
                url,
                Permission.NEVER,
            )
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(inputUriString, positionFromUrl),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val url = URL(inputUriString)
            val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
            val positionFromUrl = Position(q = "fromUrl")
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsingToGetCoords(positionFromUrl))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(connectionPermission)
            ).thenReturn(Permission.ALWAYS)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUriString,
                    mockGoogleMapsUrlConverter,
                    url,
                    positionFromUrl,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val url = URL(inputUriString)
            val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
            val positionFromUrl = Position(q = "fromUrl")
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsingToGetCoords(positionFromUrl))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(connectionPermission)
            ).thenReturn(Permission.ASK)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                RequestedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUriString,
                    mockGoogleMapsUrlConverter,
                    url,
                    positionFromUrl,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val url = URL(inputUriString)
            val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
            val positionFromUrl = Position(q = "fromUrl")
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsingToGetCoords(positionFromUrl))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(connectionPermission)
            ).thenReturn(Permission.NEVER)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                log = fakeLog,
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(inputUriString, positionFromUrl),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsNonZeroLat_returnsSucceedWithGeoUriFromUrl() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val url = URL(inputUriString)
        val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
        val positionFromUrl = Position("1", "0", q = "fromUrl")
        Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url)).thenReturn(ParseUrlResult.Parsed(positionFromUrl))
        val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
        Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
        Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
        val mockUserPreferencesRepository = Mockito.mock(
            FakeUserPreferencesRepository::class.java
        )
        Mockito.`when`(
            mockUserPreferencesRepository.getValue(connectionPermission)
        ).thenThrow(NotImplementedError::class.java)
        val stateContext = ConversionStateContext(
            listOf(mockGoogleMapsUrlConverter),
            mockIntentTools,
            mockNetworkTools,
            mockUserPreferencesRepository,
            log = fakeLog,
        )
        val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, url, null)
        assertEquals(
            ConversionSucceeded(inputUriString, positionFromUrl),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parsingUrlReturnsNonZeroLon_returnsSucceedWithGeoUriFromUrl() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val url = URL(inputUriString)
        val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
        val positionFromUrl = Position("0", "1", q = "fromUrl")
        Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url)).thenReturn(ParseUrlResult.Parsed(positionFromUrl))
        val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
        Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
        Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
        val mockUserPreferencesRepository = Mockito.mock(
            FakeUserPreferencesRepository::class.java
        )
        Mockito.`when`(
            mockUserPreferencesRepository.getValue(connectionPermission)
        ).thenThrow(NotImplementedError::class.java)
        val stateContext = ConversionStateContext(
            listOf(mockGoogleMapsUrlConverter),
            mockIntentTools,
            mockNetworkTools,
            mockUserPreferencesRepository,
            log = fakeLog,
        )
        val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, url, null)
        assertEquals(
            ConversionSucceeded(inputUriString, positionFromUrl),
            state.transition(),
        )
    }

    @Test
    fun requestedParseHtmlPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val url = URL(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val state = RequestedParseHtmlPermission(
            stateContext,
            inputUriString,
            googleMapsUrlConverter,
            url,
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedParseHtmlPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val url = URL(inputUriString)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
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
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    googleMapsUrlConverter,
                    url,
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
            val url = URL(inputUriString)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
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
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    googleMapsUrlConverter,
                    url,
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
            val url = URL(inputUriString)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
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
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
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
            val url = URL(inputUriString)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
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
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
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
            val url = URL(inputUriString)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(url)).thenThrow(
                CancellationException()::class.java
            )
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
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
            val url = URL(inputUriString)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(url)).thenThrow(
                SocketTimeoutException::class.java
            )
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
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
            val url = URL(inputUriString)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(url)).thenThrow(
                UnexpectedResponseCodeException::class.java
            )
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsParsed_returnsSucceededWithTheParsedGeoUri() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val url = URL(inputUriString)
        val html = "<html></html>"
        val mockAppleMapsUrlConverter = Mockito.mock(AppleMapsUrlConverter::class.java)
        val positionFromHtml = Position("1", "2", q = "fromHtml")
        Mockito.`when`(mockAppleMapsUrlConverter.parseHtml(html)).thenReturn(ParseHtmlResult.Parsed(positionFromHtml))
        val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
        Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
        Mockito.`when`(mockNetworkTools.getText(url)).thenReturn(html)
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
            url,
        )
        assertEquals(
            ConversionSucceeded(inputUriString, positionFromHtml),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsRedirect_returnsReceivedUrlWithTheRedirectUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val url = URL(inputUriString)
            val html = "<html></html>"
            val mockAppleMapsUrlConverter = Mockito.mock(AppleMapsUrlConverter::class.java)
            Mockito.`when`(mockAppleMapsUrlConverter.parseHtml(html))
                .thenReturn(ParseHtmlResult.Redirect(URL("https://maps.apple.com/foo-redirect")))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(url)).thenReturn(html)
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
                url,
            )
            assertEquals(
                ReceivedUrl(
                    stateContext,
                    inputUriString,
                    URL("https://maps.apple.com/foo-redirect"),
                    Permission.ALWAYS,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parseHtmlFails_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val url = URL(inputUriString)
        val html = "<html></html>"
        val mockAppleMapsUrlConverter = Mockito.mock(AppleMapsUrlConverter::class.java)
        Mockito.`when`(mockAppleMapsUrlConverter.parseHtml(html)).thenReturn(null)
        val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
        Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
        Mockito.`when`(mockNetworkTools.getText(url)).thenReturn(html)
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
            url,
        )
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_html_error),
            state.transition(),
        )
    }

    @Test
    fun requestedParseHtmlToGetCoordsPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val url = URL(inputUriString)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val state = RequestedParseHtmlToGetCoordsPermission(
            stateContext,
            inputUriString,
            googleMapsUrlConverter,
            url,
            Position("1", "2", q = "fromUrl"),
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedParseHtmlToGetCoordsPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val url = URL(inputUriString)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
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
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
                positionFromUrl,
            )
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUriString,
                    googleMapsUrlConverter,
                    url,
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
            val url = URL(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
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
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
                positionFromUrl,
            )
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUriString,
                    googleMapsUrlConverter,
                    url,
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
            val url = URL(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
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
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
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
            val url = URL(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
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
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
                positionFromUrl,
            )
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(inputUriString, positionFromUrl), state.deny(true)
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
            val url = URL(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(url)).thenThrow(
                CancellationException()::class.java
            )
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
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
            val url = URL(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(url)).thenThrow(
                SocketTimeoutException::class.java
            )
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
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
            val url = URL(inputUriString)
            val positionFromUrl = Position(q = "fromUrl")
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(url)).thenThrow(
                UnexpectedResponseCodeException::class.java
            )
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                url,
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
        val url = URL(inputUriString)
        val html = "<html></html>"
        val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
        val positionFromUrl = Position(q = "fromUrl")
        val positionFromHtml = Position(q = "fromHtml")
        Mockito.`when`(mockGoogleMapsUrlConverter.parseHtml(html)).thenReturn(ParseHtmlResult.Parsed(positionFromHtml))
        val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
        Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>())).thenThrow(NotImplementedError::class.java)
        Mockito.`when`(mockNetworkTools.getText(url)).thenReturn(html)
        val stateContext = ConversionStateContext(
            listOf(mockGoogleMapsUrlConverter),
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            log = fakeLog,
        )
        val state = GrantedParseHtmlToGetCoordsPermission(
            stateContext,
            inputUriString,
            mockGoogleMapsUrlConverter,
            url,
            positionFromUrl,
        )
        assertEquals(
            ConversionSucceeded(inputUriString, positionFromHtml),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_parseHtmlReturnsRedirect_returnsReceivedUrlWithTheRedirectUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://g.co/kgs/foo"
            val url = URL(inputUriString)
            val html = "<html></html>"
            val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
            val positionFromUrl = Position(q = "fromUrl")
            Mockito.`when`(mockGoogleMapsUrlConverter.parseHtml(html))
                .thenReturn(ParseHtmlResult.Redirect(URL(inputUriString)))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(url)).thenReturn(html)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                mockGoogleMapsUrlConverter,
                url,
                positionFromUrl,
            )
            assertEquals(
                ReceivedUrl(
                    stateContext,
                    inputUriString,
                    url,
                    Permission.ALWAYS,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_parseHtmlFails_returnsSucceededWithGeoUriFromUrl() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val url = URL(inputUriString)
            val html = "<html></html>"
            val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
            val positionFromUrl = Position(q = "fromUrl")
            Mockito.`when`(mockGoogleMapsUrlConverter.parseHtml(html)).thenReturn(null)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(url)).thenReturn(html)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                log = fakeLog,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                mockGoogleMapsUrlConverter,
                url,
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
