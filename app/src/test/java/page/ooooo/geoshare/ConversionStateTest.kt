package page.ooooo.geoshare

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter
import page.ooooo.geoshare.lib.converters.UrlConverter
import java.net.URL

class ConversionStateTest {

    private lateinit var fakeLog: ILog
    private lateinit var fakeOnMessage: (message: Message) -> Unit
    private lateinit var fakeUriQuote: FakeUriQuote
    private lateinit var googleMapsUrlConverter: GoogleMapsUrlConverter
    private lateinit var urlConverters: List<UrlConverter>
    private lateinit var mockClipboard: Clipboard
    private lateinit var mockContext: Context
    private lateinit var mockIntentTools: IntentTools
    private lateinit var mockNetworkTools: NetworkTools
    private lateinit var mockSettingsLauncherWrapper: ManagedActivityResultLauncherWrapper
    private lateinit var mockXiaomiTools: XiaomiTools
    private lateinit var fakeUserPreferencesRepository: UserPreferencesRepository

    @Before
    fun before() = runTest {
        fakeLog = FakeLog()
        fakeOnMessage = {}
        fakeUriQuote = FakeUriQuote()

        googleMapsUrlConverter = GoogleMapsUrlConverter(fakeLog, fakeUriQuote)
        urlConverters = listOf(googleMapsUrlConverter)

        mockClipboard = Mockito.mock(Clipboard::class.java)
        Mockito.`when`(mockClipboard.setClipEntry(any<ClipEntry>()))
            .thenThrow(NotImplementedError::class.java)

        mockContext = Mockito.mock(Context::class.java)
        Mockito.`when`(mockContext.packageName)
            .thenThrow(NotImplementedError::class.java)
        Mockito.`when`(mockContext.getSystemService(any<String>()))
            .thenThrow(NotImplementedError::class.java)

        mockIntentTools = Mockito.mock(IntentTools::class.java)
        Mockito.`when`(mockIntentTools.getIntentPosition(any<Intent>()))
            .thenThrow(NotImplementedError::class.java)
        Mockito.`when`(mockIntentTools.getIntentUriString(any<Intent>()))
            .thenThrow(NotImplementedError::class.java)
        Mockito.`when`(
            mockIntentTools.createChooserIntent(
                any<Uri>(),
            )
        ).thenThrow(NotImplementedError::class.java)

        mockNetworkTools = Mockito.mock(NetworkTools::class.java)
        Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
            .thenThrow(NotImplementedError::class.java)
        Mockito.`when`(mockNetworkTools.getText(any<URL>()))
            .thenThrow(NotImplementedError::class.java)

        mockSettingsLauncherWrapper =
            Mockito.mock(ManagedActivityResultLauncherWrapper::class.java)
        Mockito.`when`(mockSettingsLauncherWrapper.launcher)
            .thenThrow(NotImplementedError::class.java)

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
    fun receivedIntent_intentContainsGeoUri_returnsSucceeded() =
        runTest {
            val position = Position("1", "2", q = "fromIntent")
            val mockUri = Mockito.mock(Uri::class.java)
            val mockIntent = Mockito.mock(Intent::class.java)
            Mockito.`when`(mockIntent.data)
                .thenReturn(mockUri)
            val mockIntentTools = Mockito.mock(IntentTools::class.java)
            Mockito.`when`(mockIntentTools.getIntentPosition(mockIntent))
                .thenReturn(position)
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
                ConversionSucceeded(mockUri.toString(), position),
                state.transition(),
            )
        }

    @Test
    fun receivedIntent_intentDoesNotContainUrl_returnsFailed() = runTest {
        val intent = Intent()
        val mockIntentTools = Mockito.mock(IntentTools::class.java)
        Mockito.`when`(mockIntentTools.getIntentPosition(intent)).thenReturn(null)
        Mockito.`when`(mockIntentTools.getIntentUriString(intent))
            .thenReturn(null)
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

    /* TODO

    @Test
    fun receivedIntent_intentContainsUrl_returnsReceivedUrlStringWithPermissionNull() =
        runTest {
            val intent = Intent()
            val mockIntentTools = Mockito.mock(IntentTools::class.java)
            Mockito.`when`(mockIntentTools.getIntentGeoUri(intent))
                .thenReturn(null)
            Mockito.`when`(mockIntentTools.getIntentUrlString(intent))
                .thenReturn("https://maps.google.com/foo")
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = ReceivedIntent(stateContext, intent)
            assertEquals(
                ReceivedUrlString(
                    stateContext, "https://maps.google.com/foo", null
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUriString_isGeoUri_returnsSucceeded() = runTest {
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
            onMessage = fakeOnMessage,
        )
        val uriString = "geo:1,2?q="
        val mockUri = Mockito.mock(Uri::class.java)
        Mockito.`when`(mockUri.scheme).thenReturn("geo")
        val state = ReceivedUriString(stateContext, uriString) { mockUri }
        assertEquals(
            ConversionSucceeded(uriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isNotGeoUri_returnsReceivedUrlStringWithPermissionNull() =
        runTest {
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val uriString = "https://www.example.com/"
            val mockUri = Mockito.mock(Uri::class.java)
            Mockito.`when`(mockUri.scheme).thenReturn("https")
            val state = ReceivedUriString(stateContext, uriString) { mockUri }
            assertEquals(
                ReceivedUrlString(stateContext, uriString, null),
                state.transition(),
            )
        }

    @Test
    fun receivedUriString_isMissingScheme_returnsReceivedUrlStringWithPermissionNull() =
        runTest {
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val uriString = "www.example.com"
            val mockUri = Mockito.mock(Uri::class.java)
            Mockito.`when`(mockUri.scheme).thenReturn(null)
            val state = ReceivedUriString(stateContext, uriString) { mockUri }
            assertEquals(
                ReceivedUrlString(stateContext, uriString, null),
                state.transition(),
            )
        }

    @Test
    fun receivedUrlString_isValidUrl_returnsReceivedUrlAndPassesPermission() =
        runTest {
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val urlString = "https://maps.google.com/foo"
            val permission = Permission.NEVER
            val state = ReceivedUrlString(stateContext, urlString, permission)
            assertEquals(
                ReceivedUrl(
                    stateContext,
                    URL(urlString),
                    permission,
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrlString_isNotValidUrl_returnsFailed() = runTest {
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
            onMessage = fakeOnMessage,
        )
        val urlString = "https://[invalid:ipv6]/"
        val permission = Permission.NEVER
        val state = ReceivedUrlString(stateContext, urlString, permission)
        assertEquals(
            ConversionFailed(
                stateContext,
                R.string.conversion_failed_invalid_url
            ),
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
            onMessage = fakeOnMessage,
        )
        val urlString = ""
        val permission = Permission.NEVER
        val state = ReceivedUrlString(stateContext, urlString, permission)
        assertEquals(
            ConversionFailed(
                stateContext,
                R.string.conversion_failed_invalid_url
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUrlString_isMissingScheme_returnsReceivedUrlWithSchemeAndPassesPermission() =
        runTest {
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val urlString = "www.example.com/"
            val permission = Permission.NEVER
            val state = ReceivedUrlString(stateContext, urlString, permission)
            assertEquals(
                ReceivedUrl(
                    stateContext,
                    URL("https://$urlString"),
                    permission,
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrlString_isRelativeScheme_returnsReceivedUrlWithHttpsSchemeAndPassesPermission() =
        runTest {
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val urlString = "//www.example.com/"
            val permission = Permission.NEVER
            val state = ReceivedUrlString(stateContext, urlString, permission)
            assertEquals(
                ReceivedUrl(
                    stateContext,
                    URL("https:$urlString"),
                    permission,
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrlString_isNotHttpsScheme_returnsReceivedUrlWithHttpsSchemeAndPassesPermission() =
        runTest {
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val urlString = "ftp://www.example.com/"
            val permission = Permission.NEVER
            val state = ReceivedUrlString(stateContext, urlString, permission)
            assertEquals(
                ReceivedUrl(
                    stateContext,
                    URL("https://www.example.com/"),
                    permission,
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsUnsupportedMapService_returnsConversionFailed() =
        runTest {
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val permission = Permission.NEVER
            val state = ReceivedUrl(
                stateContext,
                URL("https://maps.example.com/foo"),
                permission,
            )
            assertEquals(
                ConversionFailed(stateContext, R.string.conversion_failed_unsupported_service),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsFullUrl_returnsUnshortenedUrlAndPassesPermission() =
        runTest {
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val permission = Permission.NEVER
            val state = ReceivedUrl(
                stateContext,
                URL("https://maps.google.com/foo"),
                permission,
            )
            assertEquals(
                UnshortenedUrl(
                    stateContext,
                    googleMapsUrlConverter,
                    URL("https://maps.google.com/foo"),
                    permission,
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsAlways_returnsGrantedUnshortenPermission() =
        runTest {
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
                onMessage = fakeOnMessage,
            )
            val state = ReceivedUrl(
                stateContext,
                URL("https://maps.app.goo.gl/foo"),
                Permission.ALWAYS,
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext,
                    googleMapsUrlConverter,
                    URL("https://maps.app.goo.gl/foo"),
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsAsk_returnsRequestedUnshortenPermission() =
        runTest {
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
                onMessage = fakeOnMessage,
            )
            val state = ReceivedUrl(
                stateContext,
                URL("https://maps.app.goo.gl/foo"),
                Permission.ASK,
            )
            assertEquals(
                RequestedUnshortenPermission(
                    stateContext,
                    googleMapsUrlConverter,
                    URL("https://maps.app.goo.gl/foo"),
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNever_returnsDeniedUnshortenPermission() =
        runTest {
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
                onMessage = fakeOnMessage,
            )
            val state = ReceivedUrl(
                stateContext,
                URL("https://maps.app.goo.gl/foo"),
                Permission.NEVER,
            )
            assertTrue(state.transition() is DeniedConnectionPermission)
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedUnshortenPermission() =
        runTest {
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
                onMessage = fakeOnMessage,
            )
            val state = ReceivedUrl(
                stateContext,
                URL("https://maps.app.goo.gl/foo"),
                null,
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext,
                    googleMapsUrlConverter,
                    URL("https://maps.app.goo.gl/foo"),
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedUnshortenPermission() =
        runTest {
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
                onMessage = fakeOnMessage,
            )
            val state = ReceivedUrl(
                stateContext,
                URL("https://maps.app.goo.gl/foo"),
                null,
            )
            assertEquals(
                RequestedUnshortenPermission(
                    stateContext,
                    googleMapsUrlConverter,
                    URL("https://maps.app.goo.gl/foo"),
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedUnshortenPermission() =
        runTest {
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
                onMessage = fakeOnMessage,
            )
            val state = ReceivedUrl(
                stateContext,
                URL("https://maps.app.goo.gl/foo"),
                null,
            )
            assertTrue(state.transition() is DeniedConnectionPermission)
        }

    @Test
    fun requestedUnshortenPermission_transition_returnsNull() = runTest {
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
            onMessage = fakeOnMessage,
        )
        val state = RequestedUnshortenPermission(
            stateContext,
            googleMapsUrlConverter,
            URL("https://maps.app.goo.gl/foo"),
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedUnshortenPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val url = URL("https://maps.app.goo.gl/foo")
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
                onMessage = fakeOnMessage,
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, googleMapsUrlConverter, url),
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
            val url = URL("https://maps.app.goo.gl/foo")
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
                onMessage = fakeOnMessage,
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, googleMapsUrlConverter, url),
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
            val url = URL("https://maps.app.goo.gl/foo")
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
                onMessage = fakeOnMessage,
            )
            val state = RequestedUnshortenPermission(
                stateContext,
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
            val url = URL("https://maps.app.goo.gl/foo")
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
                onMessage = fakeOnMessage,
            )
            val state = RequestedUnshortenPermission(
                stateContext,
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
                onMessage = fakeOnMessage,
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                googleMapsUrlConverter,
                URL("https://maps.app.goo.gl/foo"),
            )
            assertEquals(
                ConversionFailed(
                    stateContext,
                    R.string.conversion_failed_cancelled,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_gettingLocationHeaderThrowsSocketTimeoutException_returnsConversionFailedWithConnectionErrorMessage() =
        runTest {
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(SocketTimeoutException::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                googleMapsUrlConverter,
                URL("https://maps.app.goo.gl/foo"),
            )
            assertEquals(
                ConversionFailed(
                    stateContext,
                    R.string.conversion_failed_unshorten_connection_error,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_gettingLocationHeaderThrowsMalformedURLException_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(MalformedURLException::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                googleMapsUrlConverter,
                URL("https://maps.app.goo.gl/foo"),
            )
            assertEquals(
                ConversionFailed(
                    stateContext,
                    R.string.conversion_failed_unshorten_error,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_gettingLocationHeaderThrowsUnexpectedResponseCodeException_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(UnexpectedResponseCodeException::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                googleMapsUrlConverter,
                URL("https://maps.app.goo.gl/foo"),
            )
            assertEquals(
                ConversionFailed(
                    stateContext,
                    R.string.conversion_failed_unshorten_error,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_gettingLocationHeaderSucceeds_returnsUnshortenedUrl() =
        runTest {
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenReturn(URL("https://maps.google.com/foo"))
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                googleMapsUrlConverter,
                URL("https://maps.app.goo.gl/foo"),
            )
            assertEquals(
                UnshortenedUrl(
                    stateContext,
                    googleMapsUrlConverter,
                    URL("https://maps.google.com/foo"),
                    Permission.ALWAYS,
                ),
                state.transition(),
            )
        }

    @Test
    fun deniedConnectionPermission_returnsFailed() = runTest {
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
            onMessage = fakeOnMessage,
        )
        val state = DeniedConnectionPermission(stateContext, googleMapsUrlConverter)
        assertEquals(
            ConversionFailed(stateContext, R.string.conversion_failed_connection_permission_denied),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parsingUrlFails_returnsFailed() = runTest {
        val url = URL("https://maps.google.com/foo")
        val mockGoogleMapsUrlConverter =
            Mockito.mock(GoogleMapsUrlConverter::class.java)
        Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
            .thenReturn(null)
        val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
        Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
            .thenThrow(NotImplementedError::class.java)
        Mockito.`when`(mockNetworkTools.getText(any<URL>()))
            .thenThrow(NotImplementedError::class.java)
        val stateContext = ConversionStateContext(
            listOf(mockGoogleMapsUrlConverter),
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
            onMessage = fakeOnMessage,
        )
        val state = UnshortenedUrl(stateContext, googleMapsUrlConverter, url, Permission.ALWAYS)
        assertEquals(
            ConversionFailed(stateContext, R.string.conversion_failed_parse_url_error),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsing())
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = UnshortenedUrl(stateContext, mockGoogleMapsUrlConverter, url, Permission.ALWAYS)
            assertEquals(
                GrantedParseHtmlPermission(stateContext, mockGoogleMapsUrlConverter, url),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsing())
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = UnshortenedUrl(stateContext, mockGoogleMapsUrlConverter, url, Permission.ASK)
            assertEquals(
                RequestedParseHtmlPermission(stateContext, mockGoogleMapsUrlConverter, url),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNever_returnsDeniedConnectionPermission() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsing())
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = UnshortenedUrl(stateContext, mockGoogleMapsUrlConverter, url, Permission.NEVER)
            assertEquals(
                DeniedConnectionPermission(stateContext, mockGoogleMapsUrlConverter),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsing())
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
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
                onMessage = fakeOnMessage,
            )
            val state = UnshortenedUrl(stateContext, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                GrantedParseHtmlPermission(stateContext, mockGoogleMapsUrlConverter, url),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsing())
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
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
                onMessage = fakeOnMessage,
            )
            val state = UnshortenedUrl(stateContext, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                RequestedParseHtmlPermission(stateContext, mockGoogleMapsUrlConverter, url),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedConnectionPermission() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsing())
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
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
                onMessage = fakeOnMessage,
            )
            val state = UnshortenedUrl(stateContext, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                DeniedConnectionPermission(stateContext, mockGoogleMapsUrlConverter),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsAlways_returnsGrantedParseHtmlToGetCoordsPermission() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsingToGetCoords(geoUriBuilderFromUrl))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = UnshortenedUrl(
                stateContext,
                mockGoogleMapsUrlConverter,
                url,
                Permission.ALWAYS,
            )
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    mockGoogleMapsUrlConverter,
                    url,
                    geoUriBuilderFromUrl.toString(),
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsAsk_returnsRequestedParseHtmlToGetCoordsPermission() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsingToGetCoords(geoUriBuilderFromUrl))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = UnshortenedUrl(
                stateContext,
                mockGoogleMapsUrlConverter,
                url,
                Permission.ASK,
            )
            assertEquals(
                RequestedParseHtmlToGetCoordsPermission(
                    stateContext,
                    mockGoogleMapsUrlConverter,
                    url,
                    geoUriBuilderFromUrl.toString(),
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsNever_returnsDeniedParseHtmlToGetCoordsPermission() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsingToGetCoords(geoUriBuilderFromUrl))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                listOf(mockGoogleMapsUrlConverter),
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = UnshortenedUrl(
                stateContext,
                mockGoogleMapsUrlConverter,
                url,
                Permission.NEVER,
            )
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(geoUriBuilderFromUrl.toString()),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseHtmlToGetCoordsPermission() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsingToGetCoords(geoUriBuilderFromUrl))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
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
                onMessage = fakeOnMessage,
            )
            val state = UnshortenedUrl(stateContext, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    mockGoogleMapsUrlConverter,
                    url,
                    geoUriBuilderFromUrl.toString(),
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedParseHtmlToGetCoordsPermission() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsingToGetCoords(geoUriBuilderFromUrl))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
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
                onMessage = fakeOnMessage,
            )
            val state = UnshortenedUrl(stateContext, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                RequestedParseHtmlToGetCoordsPermission(
                    stateContext,
                    mockGoogleMapsUrlConverter,
                    url,
                    geoUriBuilderFromUrl.toString(),
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsRequiresHtmlParsingToGetCoordsAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedParseHtmlToGetCoordsPermission() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.RequiresHtmlParsingToGetCoords(geoUriBuilderFromUrl))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
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
                onMessage = fakeOnMessage,
            )
            val state = UnshortenedUrl(stateContext, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(geoUriBuilderFromUrl.toString()),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsNonZeroLat_returnsSucceedWithGeoUriFromUrl() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("1", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.Parsed(geoUriBuilderFromUrl))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
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
                onMessage = fakeOnMessage,
            )
            val state = UnshortenedUrl(stateContext, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                ConversionSucceeded(geoUriBuilderFromUrl.toString()),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsNonZeroLon_returnsSucceedWithGeoUriFromUrl() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "1")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(ParseUrlResult.Parsed(geoUriBuilderFromUrl))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
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
                onMessage = fakeOnMessage,
            )
            val state = UnshortenedUrl(stateContext, mockGoogleMapsUrlConverter, url, null)
            assertEquals(
                ConversionSucceeded(geoUriBuilderFromUrl.toString()),
                state.transition(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_transition_returnsNull() = runTest {
        val url = URL("https://maps.apple.com/foo")
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
            onMessage = fakeOnMessage,
        )
        val state = RequestedParseHtmlPermission(
            stateContext,
            googleMapsUrlConverter,
            url,
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedParseHtmlPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val url = URL("https://maps.apple.com/foo")
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
                onMessage = fakeOnMessage,
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
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
            val url = URL("https://maps.apple.com/foo")
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
                onMessage = fakeOnMessage,
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
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
            val url = URL("https://maps.apple.com/foo")
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
                onMessage = fakeOnMessage,
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                DeniedConnectionPermission(stateContext, googleMapsUrlConverter), state.deny(false)
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_denyWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val url = URL("https://maps.apple.com/foo")
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
                onMessage = fakeOnMessage,
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                DeniedConnectionPermission(stateContext, googleMapsUrlConverter), state.deny(true)
            )
            verify(mockUserPreferencesRepository).setValue(
                connectionPermission,
                Permission.NEVER,
            )
        }

    @Test
    fun grantedParseHtmlPermission_downloadingHtmlThrowsCancellationException_returnsConversionFailedWithCancelledMessage() =
        runTest {
            val url = URL("https://maps.apple.com/foo")
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
                onMessage = fakeOnMessage,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                ConversionFailed(stateContext, R.string.conversion_failed_cancelled),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_downloadingHtmlThrowsSocketTimeoutException_returnsConversionFailedWithConnectionErrorMessage() =
        runTest {
            val url = URL("https://maps.apple.com/foo")
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
                onMessage = fakeOnMessage,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                ConversionFailed(stateContext, R.string.conversion_failed_parse_html_connection_error),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_downloadingHtmlThrowsUnexpectedResponseCodeException_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val url = URL("https://maps.apple.com/foo")
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
                onMessage = fakeOnMessage,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
            )
            assertEquals(
                ConversionFailed(stateContext, R.string.conversion_failed_parse_html_error),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsParsed_returnsSucceededWithTheParsedGeoUri() =
        runTest {
            val url = URL("https://maps.apple.com/foo")
            val html = "<html></html>"
            val mockAppleMapsUrlConverter =
                Mockito.mock(AppleMapsUrlConverter::class.java)
            val geoUriBuilderFromHtml = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromHtml.coords = GeoUriCoords("1", "2")
            geoUriBuilderFromHtml.params =
                GeoUriParams(q = "fromHtml", uriQuote = fakeUriQuote)
            Mockito.`when`(mockAppleMapsUrlConverter.parseHtml(html))
                .thenReturn(ParseHtmlResult.Parsed(geoUriBuilderFromHtml))
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
                onMessage = fakeOnMessage,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                mockAppleMapsUrlConverter,
                url,
            )
            assertEquals(
                ConversionSucceeded(geoUriBuilderFromHtml.toString()),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsRedirect_returnsReceivedUrlWithTheRedirectUrlAndPermissionAlways() =
        runTest {
            val url = URL("https://maps.apple.com/foo")
            val html = "<html></html>"
            val mockAppleMapsUrlConverter =
                Mockito.mock(AppleMapsUrlConverter::class.java)
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
                onMessage = fakeOnMessage,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                mockAppleMapsUrlConverter,
                url,
            )
            assertEquals(
                ReceivedUrl(
                    stateContext,
                    URL("https://maps.apple.com/foo-redirect"),
                    Permission.ALWAYS,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parseHtmlFails_returnsConversionFailed() =
        runTest {
            val url = URL("https://maps.apple.com/foo")
            val html = "<html></html>"
            val mockAppleMapsUrlConverter =
                Mockito.mock(AppleMapsUrlConverter::class.java)
            Mockito.`when`(mockAppleMapsUrlConverter.parseHtml(html))
                .thenReturn(null)
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
                onMessage = fakeOnMessage,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                mockAppleMapsUrlConverter,
                url,
            )
            assertEquals(
                ConversionFailed(stateContext, R.string.conversion_failed_parse_html_error),
                state.transition(),
            )
        }

    @Test
    fun requestedParseHtmlToGetCoordsPermission_transition_returnsNull() = runTest {
        val url = URL("https://maps.app.goo.gl/foo")
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
            onMessage = fakeOnMessage,
        )
        val state = RequestedParseHtmlToGetCoordsPermission(
            stateContext,
            googleMapsUrlConverter,
            url,
            "geo:1,2?q=fromUrl",
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedParseHtmlToGetCoordsPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val url = URL("https://maps.app.goo.gl/foo")
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.setValue(
                    eq(connectionPermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    googleMapsUrlConverter,
                    url,
                    geoUriBuilderFromUrl.toString(),
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
            val url = URL("https://maps.app.goo.gl/foo")
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
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
                onMessage = fakeOnMessage,
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertEquals(
                GrantedParseHtmlToGetCoordsPermission(
                    stateContext,
                    googleMapsUrlConverter,
                    url,
                    geoUriBuilderFromUrl.toString(),
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
            val url = URL("https://maps.app.goo.gl/foo")
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
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
                onMessage = fakeOnMessage,
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(geoUriBuilderFromUrl.toString()), state.deny(false)
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlToGetCoordsPermission_denyWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val url = URL("https://maps.app.goo.gl/foo")
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
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
                onMessage = fakeOnMessage,
            )
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(geoUriBuilderFromUrl.toString()), state.deny(true)
            )
            verify(mockUserPreferencesRepository).setValue(
                connectionPermission,
                Permission.NEVER,
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_downloadingHtmlThrowsCancellationException_returnsConversionFailedWithCancelledErrorMessage() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
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
                onMessage = fakeOnMessage,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertEquals(
                ConversionFailed(stateContext, R.string.conversion_failed_cancelled),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_downloadingHtmlThrowsSocketTimeoutException_returnsConversionFailedWithConnectionErrorMessage() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
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
                onMessage = fakeOnMessage,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertEquals(
                ConversionFailed(stateContext, R.string.conversion_failed_parse_html_connection_error),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_downloadingHtmlThrowsUnexpectedResponseCodeException_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
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
                onMessage = fakeOnMessage,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                googleMapsUrlConverter,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertEquals(
                ConversionFailed(stateContext, R.string.conversion_failed_parse_html_error),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_parseHtmlReturnsParsed_returnsSucceededWithTheParsedGeoUri() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val html = "<html></html>"
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            val geoUriBuilderFromHtml = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromHtml.coords = GeoUriCoords("1", "2")
            geoUriBuilderFromHtml.params =
                GeoUriParams(q = "fromHtml", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseHtml(html))
                .thenReturn(ParseHtmlResult.Parsed(geoUriBuilderFromHtml))
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
                onMessage = fakeOnMessage,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                mockGoogleMapsUrlConverter,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertEquals(
                ConversionSucceeded(geoUriBuilderFromHtml.toString()),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_parseHtmlReturnsRedirect_returnsReceivedUrlWithTheRedirectUrlAndPermissionAlways() =
        runTest {
            val url = URL("https://g.co/kgs/foo")
            val html = "<html></html>"
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseHtml(html))
                .thenReturn(ParseHtmlResult.Redirect(URL("https://maps.google.com/foo")))
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
                onMessage = fakeOnMessage,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                mockGoogleMapsUrlConverter,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertEquals(
                ReceivedUrl(
                    stateContext,
                    URL("https://maps.google.com/foo"),
                    Permission.ALWAYS,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_parseHtmlFails_returnsSucceededWithGeoUriFromUrl() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val html = "<html></html>"
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseHtml(html))
                .thenReturn(null)
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
                onMessage = fakeOnMessage,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                mockGoogleMapsUrlConverter,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertEquals(
                ConversionSucceeded(geoUriBuilderFromUrl.toString()),
                state.transition(),
            )
        }

    @Test
    fun deniedParseHtmlToGetCoordsPermission_returnsSucceededWithGeoUriFromUrl() =
        runTest {
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            val state = DeniedParseHtmlToGetCoordsPermission(
                geoUriBuilderFromUrl.toString()
            )
            assertEquals(
                ConversionSucceeded(geoUriBuilderFromUrl.toString()),
                state.transition(),
            )
        }

    @Test
    fun conversionSucceeded_returnsNull() = runTest {
        val state = ConversionSucceeded("geo:1,2")
        assertNull(state.transition())
    }

    @Test
    fun conversionFailed_callsOnMessageAndReturnsNull() = runTest {
        val messageResId = 987
        var message: Message? = null
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
            onMessage = { message = it },
        )
        val state = ConversionFailed(stateContext, messageResId)
        assertNull(state.transition())
        assertEquals(message, Message(messageResId, Message.Type.ERROR))
    }

    @Test
    fun acceptedSharing_backgroundStartActivityPermissionIsGranted_returnsGrantedSharePermission() =
        runTest {
            val geoUri = "geo:1,1"
            val mockXiaomiTools = Mockito.mock(XiaomiTools::class.java)
            Mockito.`when`(
                mockXiaomiTools.isBackgroundStartActivityPermissionGranted(
                    any<Context>()
                )
            ).thenReturn(true)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = AcceptedSharing(
                stateContext,
                mockContext,
                mockSettingsLauncherWrapper,
                geoUri,
            )
            assertEquals(
                GrantedSharePermission(
                    stateContext,
                    mockContext,
                    geoUri,
                ),
                state.transition(),
            )
        }

    @Test
    fun acceptedSharing_backgroundStartActivityPermissionIsNotGranted_returnsRequestedSharePermission() =
        runTest {
            val geoUri = "geo:1,1"
            val mockXiaomiTools = Mockito.mock(XiaomiTools::class.java)
            Mockito.`when`(
                mockXiaomiTools.isBackgroundStartActivityPermissionGranted(
                    mockContext
                )
            ).thenReturn(false)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = AcceptedSharing(
                stateContext,
                mockContext,
                mockSettingsLauncherWrapper,
                geoUri,
            )
            assertEquals(
                RequestedSharePermission(
                    stateContext,
                    mockContext,
                    mockSettingsLauncherWrapper,
                    geoUri,
                ),
                state.transition(),
            )
        }

    @Test
    fun requestedSharePermission_grant_showPermissionEditorReturnsTrue_returnsShowedSharePermissionEditor() =
        runTest {
            val geoUri = "geo:1,1"
            val mockXiaomiTools = Mockito.mock(XiaomiTools::class.java)
            Mockito.`when`(
                mockXiaomiTools.showPermissionEditor(
                    mockContext,
                    mockSettingsLauncherWrapper,
                )
            ).thenReturn(true)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = RequestedSharePermission(
                stateContext,
                mockContext,
                mockSettingsLauncherWrapper,
                geoUri,
            )
            assertEquals(
                ShowedSharePermissionEditor(
                    stateContext,
                    mockContext,
                    mockSettingsLauncherWrapper,
                    geoUri,
                ),
                state.grant(false),
            )
        }

    @Test
    fun requestedSharePermission_grant_showPermissionEditorReturnsFalse_returnsSharingFailed() =
        runTest {
            val geoUri = "geo:1,1"
            val mockXiaomiTools = Mockito.mock(XiaomiTools::class.java)
            Mockito.`when`(
                mockXiaomiTools.showPermissionEditor(
                    mockContext,
                    mockSettingsLauncherWrapper,
                )
            ).thenReturn(false)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = RequestedSharePermission(
                stateContext,
                mockContext,
                mockSettingsLauncherWrapper,
                geoUri,
            )
            assertEquals(
                SharingFailed(
                    stateContext,
                    R.string.sharing_failed_xiaomi_permission_show_editor_error,
                ),
                state.grant(false),
            )
        }

    @Test
    fun requestedSharePermission_deny_returnsDismissedSharePermissionEditor() =
        runTest {
            val geoUri = "geo:1,1"
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = RequestedSharePermission(
                stateContext,
                mockContext,
                mockSettingsLauncherWrapper,
                geoUri,
            )
            assertEquals(
                SharingFailed(
                    stateContext,
                    R.string.sharing_failed_xiaomi_permission_denied,
                ),
                state.deny(false),
            )
        }

    @Test
    fun showedSharePermissionEditor_grant_returnsAcceptedSharing() = runTest {
        val geoUri = "geo:1,1"
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
            onMessage = fakeOnMessage,
        )
        val state = ShowedSharePermissionEditor(
            stateContext,
            mockContext,
            mockSettingsLauncherWrapper,
            geoUri,
        )
        assertEquals(
            AcceptedSharing(
                stateContext,
                mockContext,
                mockSettingsLauncherWrapper,
                geoUri,
            ),
            state.grant(false),
        )
    }

    @Test(expected = NotImplementedError::class)
    fun showedSharePermissionEditor_deny_throwsNotImplementedError() = runTest {
        val geoUri = "geo:1,1"
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
            onMessage = fakeOnMessage,
        )
        val state = ShowedSharePermissionEditor(
            stateContext,
            mockContext,
            mockSettingsLauncherWrapper,
            geoUri,
        )
        state.deny(false)
    }

    @Test
    fun grantedSharePermission_intentToolsShareDoesNotThrow_returnsSharingSucceeded() =
        runTest {
            val geoUri = "geo:1,1"
            val mockIntentTools = Mockito.mock(IntentTools::class.java)
            Mockito.doNothing().`when`(mockIntentTools)
                .share(any<Context>(), any<String>(), any<String>())
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = GrantedSharePermission(
                stateContext,
                mockContext,
                geoUri,
            )
            assertEquals(
                SharingSucceeded(stateContext, R.string.sharing_succeeded),
                state.transition(),
            )
            verify(mockIntentTools).share(
                mockContext,
                Intent.ACTION_VIEW,
                geoUri,
            )
        }

    @Test
    fun grantedSharePermission_intentToolsShareThrows_returnsSharingFailed() =
        runTest {
            val geoUri = "geo:1,1"
            val mockIntentTools = Mockito.mock(IntentTools::class.java)
            Mockito.`when`(
                mockIntentTools.share(
                    any<Context>(),
                    any<String>(),
                    any<String>(),
                )
            ).thenThrow(ActivityNotFoundException::class.java)
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = GrantedSharePermission(
                stateContext,
                mockContext,
                geoUri,
            )
            assertEquals(
                SharingFailed(
                    stateContext, R.string.sharing_failed_activity_not_found
                ),
                state.transition(),
            )
        }

    @Test
    fun sharingSucceeded_callsOnMessageAndReturnsNull() = runTest {
        val messageResId = 987
        var message: Message? = null
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
            onMessage = { message = it },
        )
        val state = SharingSucceeded(stateContext, messageResId)
        assertNull(state.transition())
        assertEquals(message, Message(messageResId, Message.Type.SUCCESS))
    }

    @Test
    fun sharingFailed_returnsNull() = runTest {
        val messageResId = 987
        var message: Message? = null
        val stateContext = ConversionStateContext(
            urlConverters,
            mockIntentTools,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
            log = fakeLog,
            onMessage = { message = it },
        )
        val state = SharingFailed(stateContext, messageResId)
        assertNull(state.transition())
        assertEquals(message, Message(messageResId, Message.Type.ERROR))
    }

    @Test
    fun acceptedCopying_setsClipboardTextAndReturnsCopyingSucceeded() =
        runTest {
            val geoUri = "geo:1,1,"

            val mockClipboardTools = Mockito.mock(ClipboardTools::class.java)
            Mockito.`when`(
                mockClipboardTools.setPlainText(
                    any<Clipboard>(),
                    any<String>(),
                    any<String>(),
                )
            ).thenReturn(Unit)

            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                clipboardTools = mockClipboardTools,
                log = fakeLog,
                onMessage = fakeOnMessage,
            )
            val state = AcceptedCopying(
                stateContext,
                mockClipboard,
                geoUri,
            )
            assertEquals(
                CopyingFinished(stateContext),
                state.transition(),
            )
            verify(mockClipboardTools).setPlainText(
                mockClipboard,
                "geo: URI",
                geoUri,
            )
        }

    @Test
    fun copyingSucceeded_buildVersionIsGreaterOrEqualThanTiramisu_doesNotCallOnMessageAndReturnsNull() =
        runTest {
            var message: Message? = null
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = { message = it },
                getBuildVersionSdkInt = { Build.VERSION_CODES.UPSIDE_DOWN_CAKE },
            )
            val state = CopyingFinished(stateContext)
            assertNull(state.transition())
            assertNull(message)
        }

    @Test
    fun copyingSucceeded_buildVersionIsLessThanTiramisu_callsOnMessageAndReturnsNull() =
        runTest {
            var message: Message? = null
            val stateContext = ConversionStateContext(
                urlConverters,
                mockIntentTools,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
                log = fakeLog,
                onMessage = { message = it },
                getBuildVersionSdkInt = { Build.VERSION_CODES.R },
            )
            val state = CopyingFinished(stateContext)
            assertNull(state.transition())
            assertEquals(
                message,
                Message(R.string.copying_finished, Message.Type.SUCCESS),
            )
        }
     */
}
