package page.ooooo.geoshare

import android.content.Context
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
    private lateinit var mockXiaomiTools: XiaomiTools
    private lateinit var fakeUserPreferencesRepository: UserPreferencesRepository

    private fun createMockUri(uriString: String): Uri {
        val mockUri = Mockito.mock(Uri::class.java)
        val schemeAndRest = uriString.split(":", limit = 2)
        val scheme = schemeAndRest[0]
        val authorityAndQuery = schemeAndRest.getOrNull(1)?.split("?", limit = 2)
        val authority = authorityAndQuery?.get(0)
        val query = authorityAndQuery?.getOrNull(1)
        Mockito.`when`(mockUri.scheme).thenReturn(scheme)
        Mockito.`when`(mockUri.authority).thenReturn(authority)
        Mockito.`when`(mockUri.query).thenReturn(query)
        Mockito.`when`(mockUri.toString()).thenReturn(uriString)
        return mockUri
    }

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

        mockXiaomiTools = Mockito.mock(XiaomiTools::class.java)
        Mockito.`when`(
            mockXiaomiTools.isBackgroundStartActivityPermissionGranted(
                any<Context>()
            )
        ).thenThrow(NotImplementedError::class.java)
        Mockito.`when`(
            mockXiaomiTools.showPermissionEditor(
                any<Context>(),
                any<ManagedActivityResultLauncherWrapper>(),
            )
        ).thenThrow(NotImplementedError::class.java)

        fakeUserPreferencesRepository = FakeUserPreferencesRepository()
    }

    @Test
    fun initial_returnsNull() = runTest {
        val state = Initial()
        assertNull(state.transition())
    }

    @Test
    fun receivedIntent_intentContainsGeoUri_returnsSucceeded() = runTest {
        val inputUri = "geo:1,2?q=fromIntent"
        val mockUri = createMockUri(inputUri)
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
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = ReceivedIntent(stateContext, mockIntent)
        assertEquals(
            ConversionSucceeded(inputUri, position),
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
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = ReceivedIntent(stateContext, intent)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_missing_url),
            state.transition(),
        )
    }

    @Test
    fun receivedIntent_intentContainsUrl_returnsReceivedUrlStringWithPermissionNull() = runTest {
        val inputUri = "https://maps.google.com/foo"
        val intent = Intent()
        val mockIntentTools = Mockito.mock(IntentTools::class.java)
        Mockito.`when`(mockIntentTools.getIntentPosition(intent)).thenReturn(null)
        Mockito.`when`(mockIntentTools.getIntentUriString(intent)).thenReturn(inputUri)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = ReceivedIntent(stateContext, intent)
        assertEquals(
            ReceivedUrlString(stateContext, inputUri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_isGeoUri_returnsSucceeded() = runTest {
        val inputUri = "geo:1,2?q="
        val mockUri = createMockUri(inputUri)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = ReceivedUri(stateContext, mockUri, fakeUriQuote)
        assertEquals(
            ConversionSucceeded(inputUri, Position("1", "2", q = "")),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_isNotGeoUri_returnsReceivedUrlStringWithPermissionNull() = runTest {
        val inputUri = "https://www.example.com/"
        val mockUri = createMockUri(inputUri)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = ReceivedUri(stateContext, mockUri, fakeUriQuote)
        assertEquals(
            ReceivedUrlString(stateContext, inputUri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_isMissingScheme_returnsReceivedUrlStringWithPermissionNull() = runTest {
        val inputUri = "www.example.com"
        val mockUri = createMockUri(inputUri)
        Mockito.`when`(mockUri.scheme).thenReturn(null)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = ReceivedUri(stateContext, mockUri, fakeUriQuote)
        assertEquals(
            ReceivedUrlString(stateContext, inputUri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUrlString_isValidUrl_returnsReceivedUrlAndPassesPermission() = runTest {
        val inputUri = "https://maps.google.com/foo"
        val url = URL(inputUri)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val permission = Permission.NEVER
        val state = ReceivedUrlString(stateContext, inputUri, permission)
        assertEquals(
            ReceivedUrl(
                stateContext,
                inputUri,
                url,
                permission,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUrlString_isNotValidUrl_returnsFailed() = runTest {
        val inputUri = "https://[invalid:ipv6]/"
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val permission = Permission.NEVER
        val state = ReceivedUrlString(stateContext, inputUri, permission)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_invalid_url),
            state.transition(),
        )
    }

    @Test
    fun receivedUrlString_isEmpty_returnsFailed() = runTest {
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val inputUri = ""
        val permission = Permission.NEVER
        val state = ReceivedUrlString(stateContext, inputUri, permission)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_invalid_url),
            state.transition(),
        )
    }

    @Test
    fun receivedUrlString_isMissingScheme_returnsReceivedUrlWithSchemeAndPassesPermission() = runTest {
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val inputUri = "www.example.com/"
        val permission = Permission.NEVER
        val state = ReceivedUrlString(stateContext, inputUri, permission)
        assertEquals(
            ReceivedUrl(
                stateContext,
                inputUri,
                URL("https://www.example.com/"),
                permission,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUrlString_isRelativeScheme_returnsReceivedUrlWithHttpsSchemeAndPassesPermission() = runTest {
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val inputUri = "//www.example.com/"
        val permission = Permission.NEVER
        val state = ReceivedUrlString(stateContext, inputUri, permission)
        assertEquals(
            ReceivedUrl(
                stateContext,
                inputUri,
                URL("https://www.example.com/"),
                permission,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUrlString_isNotHttpsScheme_returnsReceivedUrlWithHttpsSchemeAndPassesPermission() = runTest {
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val inputUri = "ftp://www.example.com/"
        val permission = Permission.NEVER
        val state = ReceivedUrlString(stateContext, inputUri, permission)
        assertEquals(
            ReceivedUrl(
                stateContext,
                inputUri,
                URL("https://www.example.com/"),
                permission,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUrl_urlIsUnsupportedMapService_returnsConversionFailed() = runTest {
        val inputUri = "https://maps.example.com/foo"
        val url = URL(inputUri)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val permission = Permission.NEVER
        val state = ReceivedUrl(
            stateContext,
            inputUri,
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
        val inputUri = "https://maps.google.com/foo"
        val url = URL(inputUri)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val permission = Permission.NEVER
        val state = ReceivedUrl(
            stateContext,
            inputUri,
            url,
            permission,
        )
        assertEquals(
            UnshortenedUrl(
                stateContext,
                inputUri,
                googleMapsUrlConverter,
                url,
                permission,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsAlways_returnsGrantedUnshortenPermission() = runTest {
        val inputUri = "https://maps.app.goo.gl/foo"
        val url = URL(inputUri)
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
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = ReceivedUrl(
            stateContext,
            inputUri,
            url,
            Permission.ALWAYS,
        )
        assertEquals(
            GrantedUnshortenPermission(
                stateContext,
                inputUri,
                googleMapsUrlConverter,
                url,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsAsk_returnsRequestedUnshortenPermission() = runTest {
        val inputUri = "https://maps.app.goo.gl/foo"
        val url = URL(inputUri)
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
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = ReceivedUrl(
            stateContext,
            inputUri,
            url,
            Permission.ASK,
        )
        assertEquals(
            RequestedUnshortenPermission(
                stateContext,
                inputUri,
                googleMapsUrlConverter,
                url,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNever_returnsDeniedUnshortenPermission() = runTest {
        val inputUri = "https://maps.app.goo.gl/foo"
        val url = URL(inputUri)
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
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = ReceivedUrl(
            stateContext,
            inputUri,
            url,
            Permission.NEVER,
        )
        assertTrue(state.transition() is DeniedConnectionPermission)
    }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedUnshortenPermission() =
        runTest {
            val inputUri = "https://maps.app.goo.gl/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = ReceivedUrl(
                stateContext,
                inputUri,
                url,
                null,
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext,
                    inputUri,
                    googleMapsUrlConverter,
                    url,
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedUnshortenPermission() =
        runTest {
            val inputUri = "https://maps.app.goo.gl/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = ReceivedUrl(
                stateContext,
                inputUri,
                url,
                null,
            )
            assertEquals(
                RequestedUnshortenPermission(
                    stateContext,
                    inputUri,
                    googleMapsUrlConverter,
                    url,
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedUnshortenPermission() =
        runTest {
            val inputUri = "https://maps.app.goo.gl/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = ReceivedUrl(
                stateContext,
                inputUri,
                url,
                null,
            )
            assertTrue(state.transition() is DeniedConnectionPermission)
        }

    @Test
    fun requestedUnshortenPermission_transition_returnsNull() = runTest {
        val inputUri = "https://maps.app.goo.gl/foo"
        val url = URL(inputUri)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = RequestedUnshortenPermission(
            stateContext,
            inputUri,
            googleMapsUrlConverter,
            url,
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedUnshortenPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUri = "https://maps.app.goo.gl/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                inputUri,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUri, googleMapsUrlConverter, url),
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
            val inputUri = "https://maps.app.goo.gl/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                inputUri,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUri, googleMapsUrlConverter, url),
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
            val inputUri = "https://maps.app.goo.gl/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                inputUri,
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
            val inputUri = "https://maps.app.goo.gl/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                inputUri,
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
            val inputUri = "https://maps.app.goo.gl/foo"
            val url = URL(inputUri)
            val mockEngine = MockEngine {
                throw CancellationException()
            }
            val mockNetworkTools = NetworkTools(mockEngine, fakeLog)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUri,
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
            val inputUri = "https://maps.app.goo.gl/foo"
            val url = URL(inputUri)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(SocketTimeoutException::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUri,
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
            val inputUri = "https://maps.app.goo.gl/foo"
            val url = URL(inputUri)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(MalformedURLException::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUri,
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
            val inputUri = "https://maps.app.goo.gl/foo"
            val url = URL(inputUri)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(UnexpectedResponseCodeException::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUri,
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
        val inputUri = "https://maps.google.com/foo"
        val url = URL(inputUri)
        val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
        Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>())).thenReturn(url)
        Mockito.`when`(mockNetworkTools.getText(any<URL>())).thenThrow(NotImplementedError::class.java)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = GrantedUnshortenPermission(
            stateContext,
            inputUri,
            googleMapsUrlConverter,
            url,
        )
        assertEquals(
            UnshortenedUrl(
                stateContext,
                inputUri,
                googleMapsUrlConverter,
                url,
                Permission.ALWAYS,
            ),
            state.transition(),
        )
    }

    @Test
    fun deniedConnectionPermission_returnsFailed() = runTest {
        val inputUri = "https://maps.app.goo.gl/foo"
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = DeniedConnectionPermission(stateContext, inputUri, googleMapsUrlConverter)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_connection_permission_denied),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parsingUrlFails_returnsFailed() = runTest {
        val inputUri = "https://maps.google.com/foo"
        val url = URL(inputUri)
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
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = UnshortenedUrl(stateContext, inputUri, googleMapsUrlConverter, url, Permission.ALWAYS)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_url_error),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUri = "https://maps.google.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state =
                UnshortenedUrl(stateContext, inputUri, mockGoogleMapsUrlConverter, url, Permission.ALWAYS)
            assertEquals(
                GrantedParseHtmlPermission(stateContext, inputUri, mockGoogleMapsUrlConverter, url),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUri = "https://maps.google.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state =
                UnshortenedUrl(stateContext, inputUri, mockGoogleMapsUrlConverter, url, Permission.ASK)
            assertEquals(
                RequestedParseHtmlPermission(stateContext, inputUri, mockGoogleMapsUrlConverter, url),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNever_returnsDeniedConnectionPermission() =
        runTest {
            val inputUri = "https://maps.google.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state =
                UnshortenedUrl(stateContext, inputUri, mockGoogleMapsUrlConverter, url, Permission.NEVER)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUri, mockGoogleMapsUrlConverter),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUri = "https://maps.google.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = UnshortenedUrl(stateContext, inputUri, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                GrantedParseHtmlPermission(stateContext, inputUri, mockGoogleMapsUrlConverter, url),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUri = "https://maps.google.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = UnshortenedUrl(stateContext, inputUri, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                RequestedParseHtmlPermission(stateContext, inputUri, mockGoogleMapsUrlConverter, url),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedConnectionPermission() =
        runTest {
            val inputUri = "https://maps.google.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = UnshortenedUrl(stateContext, inputUri, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUri, mockGoogleMapsUrlConverter),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsAlways_returnsGrantedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUri = "https://maps.google.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = UnshortenedUrl(
                stateContext,
                inputUri,
                mockGoogleMapsUrlConverter,
                url,
                Permission.ALWAYS,
            )
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUri,
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
            val inputUri = "https://maps.google.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = UnshortenedUrl(
                stateContext,
                inputUri,
                mockGoogleMapsUrlConverter,
                url,
                Permission.ASK,
            )
            assertEquals(
                RequestedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUri,
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
            val inputUri = "https://maps.google.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = UnshortenedUrl(
                stateContext,
                inputUri,
                mockGoogleMapsUrlConverter,
                url,
                Permission.NEVER,
            )
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(inputUri, positionFromUrl),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUri = "https://maps.google.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = UnshortenedUrl(stateContext, inputUri, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUri,
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
            val inputUri = "https://maps.google.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = UnshortenedUrl(stateContext, inputUri, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                RequestedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUri,
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
            val inputUri = "https://maps.google.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = UnshortenedUrl(stateContext, inputUri, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(inputUri, positionFromUrl),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsNonZeroLat_returnsSucceedWithGeoUriFromUrl() = runTest {
        val inputUri = "https://maps.google.com/foo"
        val url = URL(inputUri)
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
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = UnshortenedUrl(stateContext, inputUri, mockGoogleMapsUrlConverter, url, null)
        assertEquals(
            ConversionSucceeded(inputUri, positionFromUrl),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parsingUrlReturnsNonZeroLon_returnsSucceedWithGeoUriFromUrl() = runTest {
        val inputUri = "https://maps.google.com/foo"
        val url = URL(inputUri)
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
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = UnshortenedUrl(stateContext, inputUri, mockGoogleMapsUrlConverter, url, null)
        assertEquals(
            ConversionSucceeded(inputUri, positionFromUrl),
            state.transition(),
        )
    }

    @Test
    fun requestedParseHtmlPermission_transition_returnsNull() = runTest {
        val inputUri = "https://maps.apple.com/foo"
        val url = URL(inputUri)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = RequestedParseHtmlPermission(
            stateContext,
            inputUri,
            googleMapsUrlConverter,
            url,
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedParseHtmlPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUri,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUri,
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
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUri,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUri,
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
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUri,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUri, googleMapsUrlConverter), state.deny(false)
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_denyWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUri,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUri, googleMapsUrlConverter), state.deny(true)
            )
            verify(mockUserPreferencesRepository).setValue(
                connectionPermission,
                Permission.NEVER,
            )
        }

    @Test
    fun grantedParseHtmlPermission_downloadingHtmlThrowsCancellationException_returnsConversionFailedWithCancelledMessage() =
        runTest {
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUri,
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
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUri,
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
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUri,
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
        val inputUri = "https://maps.apple.com/foo"
        val url = URL(inputUri)
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
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = GrantedParseHtmlPermission(
            stateContext,
            inputUri,
            mockAppleMapsUrlConverter,
            url,
        )
        assertEquals(
            ConversionSucceeded(inputUri, positionFromHtml),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsRedirect_returnsReceivedUrlWithTheRedirectUrlAndPermissionAlways() =
        runTest {
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUri,
                mockAppleMapsUrlConverter,
                url,
            )
            assertEquals(
                ReceivedUrl(
                    stateContext,
                    inputUri,
                    URL("https://maps.apple.com/foo-redirect"),
                    Permission.ALWAYS,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parseHtmlFails_returnsConversionFailed() = runTest {
        val inputUri = "https://maps.apple.com/foo"
        val url = URL(inputUri)
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
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = GrantedParseHtmlPermission(
            stateContext,
            inputUri,
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
        val inputUri = "https://maps.apple.com/foo"
        val url = URL(inputUri)
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = RequestedParseHtmlToGetCoordsPermission(
            stateContext,
            inputUri,
            googleMapsUrlConverter,
            url,
            Position("1", "2", q = "fromUrl"),
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedParseHtmlToGetCoordsPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUri,
                googleMapsUrlConverter,
                url,
                positionFromUrl,
            )
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUri,
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
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUri,
                googleMapsUrlConverter,
                url,
                positionFromUrl,
            )
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    inputUri,
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
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUri,
                googleMapsUrlConverter,
                url,
                positionFromUrl,
            )
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(inputUri, positionFromUrl), state.deny(false)
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlToGetCoordsPermission_denyWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUri,
                googleMapsUrlConverter,
                url,
                positionFromUrl,
            )
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(inputUri, positionFromUrl), state.deny(true)
            )
            verify(mockUserPreferencesRepository).setValue(
                connectionPermission,
                Permission.NEVER,
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_downloadingHtmlThrowsCancellationException_returnsConversionFailedWithCancelledErrorMessage() =
        runTest {
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUri,
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
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUri,
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
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUri,
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
        val inputUri = "https://maps.apple.com/foo"
        val url = URL(inputUri)
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
            mockXiaomiTools,
            log = fakeLog,
        )
        val state = GrantedParseHtmlToGetCoordsPermission(
            stateContext,
            inputUri,
            mockGoogleMapsUrlConverter,
            url,
            positionFromUrl,
        )
        assertEquals(
            ConversionSucceeded(inputUri, positionFromHtml),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_parseHtmlReturnsRedirect_returnsReceivedUrlWithTheRedirectUrlAndPermissionAlways() =
        runTest {
            val inputUri = "https://g.co/kgs/foo"
            val url = URL(inputUri)
            val html = "<html></html>"
            val mockGoogleMapsUrlConverter = Mockito.mock(GoogleMapsUrlConverter::class.java)
            val positionFromUrl = Position(q = "fromUrl")
            Mockito.`when`(mockGoogleMapsUrlConverter.parseHtml(html))
                .thenReturn(ParseHtmlResult.Redirect(URL(inputUri)))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(url)).thenReturn(html)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUri,
                mockGoogleMapsUrlConverter,
                url,
                positionFromUrl,
            )
            assertEquals(
                ReceivedUrl(
                    stateContext,
                    inputUri,
                    url,
                    Permission.ALWAYS,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_parseHtmlFails_returnsSucceededWithGeoUriFromUrl() =
        runTest {
            val inputUri = "https://maps.apple.com/foo"
            val url = URL(inputUri)
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
                mockXiaomiTools,
                log = fakeLog,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUri,
                mockGoogleMapsUrlConverter,
                url,
                positionFromUrl,
            )
            assertEquals(
                ConversionSucceeded(inputUri, positionFromUrl),
                state.transition(),
            )
        }

    @Test
    fun deniedParseHtmlToGetCoordsPermission_returnsSucceededWithGeoUriFromUrl() = runTest {
        val inputUri = "https://maps.apple.com/foo"
        val positionFromUrl = Position(q = "fromUrl")
        val state = DeniedParseHtmlToGetCoordsPermission(inputUri, positionFromUrl)
        assertEquals(
            ConversionSucceeded(inputUri, positionFromUrl),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_returnsNull() = runTest {
        val inputUri = "https://maps.apple.com/foo"
        val state = ConversionSucceeded(inputUri, Position("1", "2"))
        assertNull(state.transition())
    }

    @Test
    fun conversionFailed_returnsNull() = runTest {
        val state = ConversionFailed(R.string.conversion_failed_missing_url)
        assertNull(state.transition())
    }
}
