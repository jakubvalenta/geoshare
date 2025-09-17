package page.ooooo.geoshare

import android.content.Intent
import com.google.re2j.Pattern
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.*
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.connectionPermission
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.GeoUrlConverter
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter
import page.ooooo.geoshare.lib.converters.UrlConverter
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

class ConversionStateTest {
    private val fakeLog = FakeLog()
    private val fakeUriQuote = FakeUriQuote()
    private val fakeUserPreferencesRepository: UserPreferencesRepository = FakeUserPreferencesRepository()
    private val geoUrlConverter = GeoUrlConverter()
    private val googleMapsUrlConverter = GoogleMapsUrlConverter()
    private val mockIntentTools: IntentTools = mock {
        on { getIntentUriString(any()) } doThrow NotImplementedError()
        on { createChooserIntent(any()) } doThrow NotImplementedError()
    }
    private val mockNetworkTools: NetworkTools = mock {
        onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
        onBlocking { getText(any()) } doThrow NotImplementedError()
    }

    fun mockStateContext(
        intentTools: IntentTools = mockIntentTools,
        userPreferencesRepository: UserPreferencesRepository = fakeUserPreferencesRepository,
        urlConverters: List<UrlConverter> = listOf(geoUrlConverter, googleMapsUrlConverter),
        networkTools: NetworkTools = mockNetworkTools,
        log: ILog = fakeLog,
        uriQuote: UriQuote = fakeUriQuote,
    ) = ConversionStateContext(
        urlConverters = urlConverters,
        intentTools = intentTools,
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
    fun receivedIntent_intentDoesNotContainUrl_returnsFailed() = runTest {
        val intent = Intent()
        val mockIntentTools: IntentTools = mock {
            on { getIntentUriString(intent) } doReturn null
        }
        val stateContext = mockStateContext(intentTools = mockIntentTools)
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
        val mockIntentTools: IntentTools = mock {
            on { getIntentUriString(intent) } doReturn inputUriString
        }
        val stateContext = mockStateContext(intentTools = mockIntentTools)
        val state = ReceivedIntent(stateContext, intent)
        assertEquals(
            ReceivedUriString(stateContext, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isGeoUri_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "geo:1,2?q="
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ReceivedUri(stateContext, inputUriString, geoUrlConverter, uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isUriInTheMiddleOfTheInputString_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "FOO https://maps.google.com/foo BAR"
        val matchedInputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(matchedInputUriString, fakeUriQuote)
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ReceivedUri(stateContext, inputUriString, googleMapsUrlConverter, uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isValidUrl_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ReceivedUri(stateContext, inputUriString, googleMapsUrlConverter, uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isNotValidUrl_returnsConversionFailed() = runTest {
        val inputUriString = "https://[invalid:ipv6]/"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isEmpty_returnsConversionFailed() = runTest {
        val inputUriString = ""
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isMissingScheme_returnsConversionFailed() = runTest {
        val inputUriString = "maps.google.com/"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isRelativeScheme_returnsConversionFailed() = runTest {
        val inputUriString = "//maps.google.com/"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isNotHttpsScheme_returnsConversionFailed() = runTest {
        val inputUriString = "ftp://maps.google.com/"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_uriIsUnsupportedMapService_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.example.com/foo"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_uriIsFullUrl_returnsUnshortenedUrlAndPassesPermission() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val stateContext = mockStateContext()
        val state = ReceivedUri(stateContext, inputUriString, googleMapsUrlConverter, uri, Permission.NEVER)
        assertEquals(
            UnshortenedUrl(stateContext, inputUriString, googleMapsUrlConverter, uri, Permission.NEVER),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_converterDoesNotSupportShortUri_returnsUnshortenedUrlAndPassesPermission() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, fakeUriQuote)

        class MockUrlConverter : UrlConverter.WithUriPattern {
            override val uriPattern: Pattern = Pattern.compile(".")
            override val conversionUriPattern: ConversionUriPattern<PositionRegex> = uriPattern {}
        }

        val mockUrlConverter = MockUrlConverter()
        val stateContext = mockStateContext(urlConverters = listOf(mockUrlConverter))
        val state = ReceivedUri(stateContext, inputUriString, mockUrlConverter, uri, Permission.NEVER)
        assertEquals(
            UnshortenedUrl(stateContext, inputUriString, mockUrlConverter, uri, Permission.NEVER),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsAlways_returnsGrantedUnshortenPermission() = runTest {
        val inputUriString = "https://example.com/foo/bar"
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val replacedUriString = "https://example.com/replaced-foo/bar"
        val replacedUri = Uri.parse(replacedUriString, fakeUriQuote)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(eq(connectionPermission)) } doThrow NotImplementedError()
        }
        val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
            on { shortUriPattern } doReturn Pattern.compile("""https://example\.com/(?P<path>\w+)/\w+""")
            on { shortUriReplacement } doReturn "https://example.com/replaced-\${path}/bar"
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            urlConverters = listOf(mockGoogleMapsUrlConverter),
        )
        val state = ReceivedUri(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, Permission.ALWAYS)
        assertEquals(
            GrantedUnshortenPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter, replacedUri),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_uriIsShortUrlAndConverterHasShortUriReplacementAndPermissionIsAlways_returnsGrantedUnshortenPermissionWithReplacedShortUri() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(eq(connectionPermission)) } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = ReceivedUri(stateContext, inputUriString, googleMapsUrlConverter, uri, Permission.ALWAYS)
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsAsk_returnsRequestedUnshortenPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(eq(connectionPermission)) } doThrow NotImplementedError()
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = ReceivedUri(stateContext, inputUriString, googleMapsUrlConverter, uri, Permission.ASK)
        assertEquals(
            RequestedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsNever_returnsDeniedUnshortenPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(eq(connectionPermission)) } doThrow NotImplementedError()
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = ReceivedUri(stateContext, inputUriString, googleMapsUrlConverter, uri, Permission.NEVER)
        assertEquals(
            DeniedConnectionPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
            ),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(eq(connectionPermission)) } doReturn Permission.ALWAYS
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = ReceivedUri(stateContext, inputUriString, googleMapsUrlConverter, uri, null)
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(eq(connectionPermission)) } doReturn Permission.ASK
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = ReceivedUri(stateContext, inputUriString, googleMapsUrlConverter, uri, null)
            assertEquals(
                RequestedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_uriIsShortUrlAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(eq(connectionPermission)) } doReturn Permission.NEVER
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = ReceivedUri(stateContext, inputUriString, googleMapsUrlConverter, uri, null)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, googleMapsUrlConverter),
                state.transition(),
            )
        }

    @Test
    fun requestedUnshortenPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val stateContext = mockStateContext()
        val state = RequestedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
        assertNull(state.transition())
    }

    @Test
    fun requestedUnshortenPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
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
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
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
    fun requestedUnshortenPermission_denyWithDoNotAskFalse_doesNotSavePreferenceAndReturnsDeniedConnectionPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, googleMapsUrlConverter),
                state.deny(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedUnshortenPermission_denyWithDoNotAskTrue_savesPreferenceAndDeniedConnectionPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = RequestedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
        assertEquals(
            DeniedConnectionPermission(stateContext, inputUriString, googleMapsUrlConverter),
            state.deny(true),
        )
        verify(mockUserPreferencesRepository).setValue(
            connectionPermission,
            Permission.NEVER,
        )
    }

    @Test
    fun grantedUnshortenPermission_inputUriStringIsInvalidURL_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://[invalid:ipv6]/"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val redirectUriString = "https://maps.google.com/foo-redirect"
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doReturn redirectUriString
                onBlocking { getText(any()) } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_unshorten_error),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderThrowsCancellationException_returnsConversionFailedWithCancelledErrorMessage() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow CancellationException()
                onBlocking { getText(any()) } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_cancelled),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderThrowsSocketTimeoutException_returnsConversionFailedWithConnectionErrorMessage() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow SocketTimeoutException()
                onBlocking { getText(any()) } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_unshorten_connection_error),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderThrowsUnexpectedResponseCodeException_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow UnexpectedResponseCodeException()
                onBlocking { getText(any()) } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_unshorten_error),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderReturnsNull_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doReturn null
                onBlocking { getText(any()) } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_unshorten_error),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderReturnsAbsoluteUrl_returnsUnshortenedUrl() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val redirectUriString = "https://maps.google.com/foo-redirect"
        val redirectUri = Uri.parse(redirectUriString, fakeUriQuote)
        val mockNetworkTools: NetworkTools = mock {
            onBlocking { requestLocationHeader(any()) } doReturn redirectUriString
            onBlocking { getText(any()) } doThrow NotImplementedError()
        }
        val stateContext = mockStateContext(networkTools = mockNetworkTools)
        val state = GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
        assertEquals(
            UnshortenedUrl(stateContext, inputUriString, googleMapsUrlConverter, redirectUri, Permission.ALWAYS),
            state.transition(),
        )
    }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderReturnsRelativeUrl_returnsUnshortenedUrl() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val redirectUriString = "foo-redirect"
        val redirectUri = Uri.parse("$inputUriString/$redirectUriString", fakeUriQuote)
        val mockNetworkTools: NetworkTools = mock {
            onBlocking { requestLocationHeader(any()) } doReturn redirectUriString
            onBlocking { getText(any()) } doThrow NotImplementedError()
        }
        val stateContext = mockStateContext(networkTools = mockNetworkTools)
        val state = GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
        assertEquals(
            UnshortenedUrl(stateContext, inputUriString, googleMapsUrlConverter, redirectUri, Permission.ALWAYS),
            state.transition(),
        )
    }

    @Test
    fun deniedConnectionPermission_returnsFailed() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val stateContext = mockStateContext()
        val state = DeniedConnectionPermission(stateContext, inputUriString, googleMapsUrlConverter)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_connection_permission_denied),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_urlConverterHasNeitherConversionUriPatternNorConversionHtmlPattern_returnsFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, fakeUriQuote)

        class MockUrlConverter : UrlConverter.WithShortUriPattern {
            override val uriPattern: Pattern = Pattern.compile(".")
            override val shortUriPattern: Pattern = Pattern.compile(".")
            override val shortUriReplacement: String? = null
            override val permissionTitleResId: Int = -1
            override val loadingIndicatorTitleResId: Int = -1
        }

        val mockUrlConverter = MockUrlConverter()
        val stateContext = mockStateContext(urlConverters = listOf(mockUrlConverter))
        val state = UnshortenedUrl(stateContext, inputUriString, mockUrlConverter, uri, Permission.ALWAYS)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_url_error),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_uriPatternDoesNotMatchInputUriString_returnsFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
            on { matches(any()) } doReturn null
        }
        val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
            on { conversionUriPattern } doReturn mockUriPattern
        }
        val stateContext = mockStateContext(urlConverters = listOf(mockGoogleMapsUrlConverter))
        val state = UnshortenedUrl(stateContext, inputUriString, googleMapsUrlConverter, uri, Permission.ALWAYS)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_url_error),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringButThereAreNoCoords_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern } doReturn mockUriPattern
            }
            val stateContext = mockStateContext(urlConverters = listOf(mockGoogleMapsUrlConverter))
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, Permission.ALWAYS)
            assertEquals(
                GrantedParseHtmlPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringButThereAreNoCoordsAndPermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern } doReturn mockUriPattern
            }
            val stateContext = mockStateContext(urlConverters = listOf(mockGoogleMapsUrlConverter))
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, Permission.ASK)
            assertEquals(
                RequestedParseHtmlPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringButThereAreNoCoordsAndPermissionIsNever_returnsDeniedConnectionPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val stateContext = mockStateContext(urlConverters = listOf(mockGoogleMapsUrlConverter))
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, Permission.NEVER)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringButThereAreNoCoordsAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(connectionPermission) } doReturn Permission.ALWAYS
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                urlConverters = listOf(mockGoogleMapsUrlConverter),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, null)
            assertEquals(
                GrantedParseHtmlPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringButThereAreNoCoordsAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(connectionPermission) } doReturn Permission.ASK
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                urlConverters = listOf(mockGoogleMapsUrlConverter),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, null)
            assertEquals(
                RequestedParseHtmlPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringButThereAreNoCoordsAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedConnectionPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(connectionPermission) } doReturn Permission.NEVER
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                urlConverters = listOf(mockGoogleMapsUrlConverter),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, null)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, mockGoogleMapsUrlConverter),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringAndThereIsQueryAndOnlyLatAndPermissionIsAlways_returnsGrantedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val positionFromUrl = Position(lat = "0", q = "fromUrl")
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = true
                    override val lat = positionFromUrl.lat
                    override val q = positionFromUrl.q
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val stateContext = mockStateContext(urlConverters = listOf(mockGoogleMapsUrlConverter))
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
    fun unshortenedUrl_uriPatternMatchesInputUriStringAndThereIsQueryAndOnlyLonAndPermissionIsAlways_returnsGrantedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val positionFromUrl = Position(lon = "0", q = "fromUrl")
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                    override val lon = positionFromUrl.lon
                    override val q = positionFromUrl.q
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val stateContext = mockStateContext(urlConverters = listOf(mockGoogleMapsUrlConverter))
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
    fun unshortenedUrl_uriPatternMatchesInputUriStringAndThereIsQueryAndPermissionIsAsk_returnsRequestedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                    override val q = positionFromUrl.q
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val stateContext = mockStateContext(urlConverters = listOf(mockGoogleMapsUrlConverter))
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
    fun unshortenedUrl_uriPatternMatchesInputUriStringAndThereIsQueryAndPermissionIsNever_returnsDeniedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                    override val q = positionFromUrl.q
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val stateContext = mockStateContext(urlConverters = listOf(mockGoogleMapsUrlConverter))
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, Permission.NEVER)
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(inputUriString, positionFromUrl),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringAndThereIsQueryAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                    override val q = positionFromUrl.q
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(connectionPermission) } doReturn Permission.ALWAYS
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                urlConverters = listOf(mockGoogleMapsUrlConverter),
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
    fun unshortenedUrl_uriPatternMatchesInputUriStringAndThereIsQueryAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                    override val q = positionFromUrl.q
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(connectionPermission) } doReturn Permission.ASK
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                urlConverters = listOf(mockGoogleMapsUrlConverter),
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
    fun unshortenedUrl_uriPatternMatchesInputUriStringAndThereIsQueryAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                    override val q = positionFromUrl.q
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(connectionPermission) } doReturn Permission.NEVER
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                urlConverters = listOf(mockGoogleMapsUrlConverter),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, null)
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(inputUriString, positionFromUrl),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringAndThereIsQueryAndConverterDoesNotSupportHtml_returnsSucceeded() =
        runTest {
            val inputUriString = "geo:foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                    override val q = positionFromUrl.q
                })
            }
            val mockGoogleMapsUrlConverter: GeoUrlConverter = mock {
                on { conversionUriPattern }.doReturn(mockUriPattern)
            }
            val stateContext = mockStateContext(
                urlConverters = listOf(mockGoogleMapsUrlConverter),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, null)
            assertEquals(
                ConversionSucceeded(inputUriString, positionFromUrl),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringAndThereIsNotNullLatAndLon_returnsSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val positionFromUrl = Position(lat = "1", lon = "2", q = "fromUrl")
        val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
            on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                override fun matches(input: String) = false
                override val lat = positionFromUrl.lat
                override val lon = positionFromUrl.lon
                override val q = positionFromUrl.q
            })
        }
        val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
            on { conversionUriPattern }.doReturn(mockUriPattern)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(connectionPermission) } doThrow NotImplementedError()
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            urlConverters = listOf(mockGoogleMapsUrlConverter),
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
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val stateContext = mockStateContext()
        val state = RequestedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
        assertNull(state.transition())
    }

    @Test
    fun requestedParseHtmlPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                GrantedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri),
                state.grant(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_grantWithDoNotAskTrue_savesPreferenceAndReturnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                GrantedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri),
                state.grant(true),
            )
            verify(mockUserPreferencesRepository).setValue(
                connectionPermission,
                Permission.ALWAYS,
            )
        }

    @Test
    fun requestedParseHtmlPermission_denyWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, googleMapsUrlConverter),
                state.deny(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_denyWithDoNotAskTrue_savesPreferenceAndReturnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, googleMapsUrlConverter),
                state.deny(true),
            )
            verify(mockUserPreferencesRepository).setValue(
                connectionPermission,
                Permission.NEVER,
            )
        }

    @Test
    fun grantedParseHtmlPermission_converterSupportsNeitherHtmlPatternNorRedirectPattern_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val html = "<html></html>"
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doReturn html
            }

            class MockUrlConverter : UrlConverter.WithHtmlPattern {
                override val uriPattern: Pattern = Pattern.compile(""".""")
                override val conversionHtmlPattern = null
                override val conversionHtmlRedirectPattern = null
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }

            val mockUrlConverter = MockUrlConverter()
            val stateContext = mockStateContext(
                urlConverters = listOf(mockUrlConverter),
                networkTools = mockNetworkTools,
            )
            val state = GrantedParseHtmlPermission(stateContext, inputUriString, mockUrlConverter, uri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_inputUriStringIsInvalidURL_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://[invalid:ipv6]/"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val html = "<html></html>"
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doReturn html
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_downloadingHtmlThrowsCancellationException_returnsConversionFailedWithCancelledMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doThrow CancellationException()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_cancelled),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_downloadingHtmlThrowsSocketTimeoutException_returnsConversionFailedWithConnectionErrorMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doThrow SocketTimeoutException()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_connection_error),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_downloadingHtmlThrowsUnexpectedResponseCodeException_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doThrow UnexpectedResponseCodeException()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_htmlPatternMatchesHtml_returnsSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val html = "<html></html>"
        val positionFromHtml = Position("1", "2", q = "fromHtml")
        val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
            on { find(any()) } doReturn listOf(object : PositionRegex("mock") {
                override fun matches(input: String) = true
                override val lat = positionFromHtml.lat
                override val lon = positionFromHtml.lon
                override val q = positionFromHtml.q
            })
        }
        val mockAppleMapsUrlConverter: GoogleMapsUrlConverter = mock {
            on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
        }
        val mockNetworkTools: NetworkTools = mock {
            onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
            onBlocking { getText(any()) } doReturn html
        }
        val stateContext = mockStateContext(
            urlConverters = listOf(mockAppleMapsUrlConverter),
            networkTools = mockNetworkTools,
        )
        val state = GrantedParseHtmlPermission(stateContext, inputUriString, mockAppleMapsUrlConverter, uri)
        assertEquals(
            ConversionSucceeded(inputUriString, positionFromHtml),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlPermission_htmlPatternDoesNotMatchHtmlButHtmlRedirectPatternMatchesHtmlAbsoluteUrl_returnsReceivedUriWithTheRedirectUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val redirectUriString = "https://maps.apple.com/foo-redirect"
            val redirectUri = Uri.parse(redirectUriString, fakeUriQuote)
            val html = "<html></html>"
            val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
                on { find(any()) } doReturn null
            }
            val mockHtmlRedirectPattern: ConversionFirstHtmlPattern<RedirectRegex> = mock {
                on { find(any()) } doReturn listOf(object : RedirectRegex("mock") {
                    override fun matches(input: String) = true
                    override val url = redirectUriString
                })
            }
            val mockAppleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
                on { conversionHtmlRedirectPattern }.doReturn(mockHtmlRedirectPattern)
            }
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doReturn html
            }
            val stateContext = mockStateContext(
                urlConverters = listOf(mockAppleMapsUrlConverter),
                networkTools = mockNetworkTools,
            )
            val state = GrantedParseHtmlPermission(stateContext, inputUriString, mockAppleMapsUrlConverter, uri)
            assertEquals(
                ReceivedUri(stateContext, inputUriString, mockAppleMapsUrlConverter, redirectUri, Permission.ALWAYS),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_htmlPatternDoesNotMatchHtmlButHtmlRedirectPatternMatchesHtmlRelativeUrl_returnsReceivedUriWithTheRedirectUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val redirectUriString = "foo-redirect"
            val redirectUri = Uri.parse("$inputUriString/$redirectUriString", fakeUriQuote)
            val html = "<html></html>"
            val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
                on { find(any()) } doReturn null
            }
            val mockHtmlRedirectPattern: ConversionFirstHtmlPattern<RedirectRegex> = mock {
                on { find(any()) } doReturn listOf(object : RedirectRegex("mock") {
                    override fun matches(input: String) = true
                    override val url = redirectUriString
                })
            }
            val mockAppleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
                on { conversionHtmlRedirectPattern }.doReturn(mockHtmlRedirectPattern)
            }
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doReturn html
            }
            val stateContext = mockStateContext(
                urlConverters = listOf(mockAppleMapsUrlConverter),
                networkTools = mockNetworkTools,
            )
            val state = GrantedParseHtmlPermission(stateContext, inputUriString, mockAppleMapsUrlConverter, uri)
            assertEquals(
                ReceivedUri(stateContext, inputUriString, mockAppleMapsUrlConverter, redirectUri, Permission.ALWAYS),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_htmlPatternDoesNotMatchHtmlAndHtmlRedirectPatternIsMissingMatchingGroup_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val html = "<html></html>"
            val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
                on { find(any()) } doReturn null
            }
            val mockHtmlRedirectPattern: ConversionFirstHtmlPattern<RedirectRegex> = mock {
                on { find(any()) } doReturn listOf(object : RedirectRegex("mock") {
                    override fun matches(input: String) = true
                    override val url = null
                })
            }
            val mockAppleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
                on { conversionHtmlRedirectPattern }.doReturn(mockHtmlRedirectPattern)
            }
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doReturn html
            }
            val stateContext = mockStateContext(
                urlConverters = listOf(mockAppleMapsUrlConverter),
                networkTools = mockNetworkTools,
            )
            val state = GrantedParseHtmlPermission(stateContext, inputUriString, mockAppleMapsUrlConverter, uri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_htmlPatternDoesNotMatchHtmlAndHtmlRedirectPatternAlsoDoesNotMatchHtml_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val html = "<html></html>"
            val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
                on { find(any()) } doReturn null
            }
            val mockHtmlRedirectPattern: ConversionFirstHtmlPattern<RedirectRegex> = mock {
                on { find(any()) } doReturn null
            }
            val mockAppleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
                on { conversionHtmlRedirectPattern }.doReturn(mockHtmlRedirectPattern)
            }
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doReturn html
            }
            val stateContext = mockStateContext(
                urlConverters = listOf(mockAppleMapsUrlConverter),
                networkTools = mockNetworkTools,
            )
            val state = GrantedParseHtmlPermission(stateContext, inputUriString, mockAppleMapsUrlConverter, uri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error),
                state.transition(),
            )
        }

    @Test
    fun requestedParseHtmlToGetCoordsPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val positionFromUrl = Position(q = "fromUrl")
        val stateContext = mockStateContext()
        val state = RequestedParseHtmlToGetCoordsPermission(
            stateContext,
            inputUriString,
            googleMapsUrlConverter,
            uri,
            positionFromUrl,
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedParseHtmlToGetCoordsPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
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
    fun requestedParseHtmlToGetCoordsPermission_grantWithDoNotAskTrue_savesPreferenceAndReturnsGrantedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
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
    fun requestedParseHtmlToGetCoordsPermission_denyWithDoNotAskFalse_doesNotSavePreferenceAndReturnsDeniedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
                positionFromUrl,
            )
            assertEquals(
                DeniedParseHtmlToGetCoordsPermission(inputUriString, positionFromUrl),
                state.deny(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectionPermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlToGetCoordsPermission_denyWithDoNotAskTrue_savesPreferenceAndReturnsDeniedParseHtmlToGetCoordsPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val positionFromUrl = Position(q = "fromUrl")
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
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
    fun grantedParseHtmlToGetCoordsPermission_converterSupportsNeitherHtmlPatternNorRedirectPattern_returnsSucceededWithPositionFromUrl() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val html = "<html></html>"
            val positionFromUrl = Position(q = "fromUrl")
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doReturn html
            }

            class MockUrlConverter : UrlConverter.WithHtmlPattern {
                override val uriPattern: Pattern = Pattern.compile(""".""")
                override val conversionHtmlPattern = null
                override val conversionHtmlRedirectPattern = null
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }

            val mockUrlConverter = MockUrlConverter()
            val stateContext = mockStateContext(
                urlConverters = listOf(mockUrlConverter),
                networkTools = mockNetworkTools,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                mockUrlConverter,
                uri,
                positionFromUrl,
            )
            assertEquals(
                ConversionSucceeded(inputUriString, positionFromUrl),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_inputUriStringIsInvalidURL_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://[invalid:ipv6]/"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val html = "<html></html>"
            val positionFromUrl = Position(q = "fromUrl")
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doReturn html
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
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
    fun grantedParseHtmlToGetCoordsPermission_downloadingHtmlThrowsCancellationException_returnsConversionFailedWithCancelledErrorMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val positionFromUrl = Position(q = "fromUrl")
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doThrow CancellationException()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
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
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val positionFromUrl = Position(q = "fromUrl")
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doThrow SocketTimeoutException()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
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
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val positionFromUrl = Position(q = "fromUrl")
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doThrow UnexpectedResponseCodeException()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
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
    fun grantedParseHtmlToGetCoordsPermission_htmlPatternMatchesHtml_returnsSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, fakeUriQuote)
        val html = "<html></html>"
        val positionFromUrl = Position(q = "fromUrl")
        val positionFromHtml = Position(q = "fromHtml")
        val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
            on { find(any()) } doReturn listOf(object : PositionRegex("mock") {
                override fun matches(input: String) = true
                override val q = positionFromHtml.q
            })
        }
        val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
            on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
        }
        val mockNetworkTools: NetworkTools = mock {
            onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
            onBlocking { getText(any()) } doReturn html
        }
        val stateContext = mockStateContext(
            urlConverters = listOf(mockGoogleMapsUrlConverter),
            networkTools = mockNetworkTools,
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
    fun grantedParseHtmlToGetCoordsPermission_htmlPatternDoesNotMatchHtmlButHtmlRedirectPatternMatchesHtmlAbsoluteUrl_returnsReceivedUriWithTheRedirectUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://g.co/kgs/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val redirectUriString = "https://maps.google.com/foo-redirect"
            val redirectUri = Uri.parse(redirectUriString, fakeUriQuote)
            val html = "<html></html>"
            val positionFromUrl = Position(q = "fromUrl")
            val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
                on { find(any()) } doReturn null
            }
            val mockHtmlRedirectPattern: ConversionFirstHtmlPattern<RedirectRegex> = mock {
                on { find(any()) } doReturn listOf(object : RedirectRegex("mock") {
                    override fun matches(input: String) = true
                    override val url = redirectUriString
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
                on { conversionHtmlRedirectPattern }.doReturn(mockHtmlRedirectPattern)
            }
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doReturn html
            }
            val stateContext = mockStateContext(
                urlConverters = listOf(mockGoogleMapsUrlConverter),
                networkTools = mockNetworkTools,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                mockGoogleMapsUrlConverter,
                uri,
                positionFromUrl,
            )
            assertEquals(
                ReceivedUri(stateContext, inputUriString, mockGoogleMapsUrlConverter, redirectUri, Permission.ALWAYS),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_htmlPatternDoesNotMatchHtmlButHtmlRedirectPatternMatchesHtmlRelativeUrl_returnsReceivedUriWithTheRedirectUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://g.co/kgs/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val redirectUriString = "foo-redirect"
            val redirectUri = Uri.parse("$inputUriString/$redirectUriString", fakeUriQuote)
            val html = "<html></html>"
            val positionFromUrl = Position(q = "fromUrl")
            val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
                on { find(any()) } doReturn null
            }
            val mockHtmlRedirectPattern: ConversionFirstHtmlPattern<RedirectRegex> = mock {
                on { find(any()) } doReturn listOf(object : RedirectRegex("mock") {
                    override fun matches(input: String) = true
                    override val url = redirectUriString
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
                on { conversionHtmlRedirectPattern }.doReturn(mockHtmlRedirectPattern)
            }
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doReturn html
            }
            val stateContext = mockStateContext(
                urlConverters = listOf(mockGoogleMapsUrlConverter),
                networkTools = mockNetworkTools,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                mockGoogleMapsUrlConverter,
                uri,
                positionFromUrl,
            )
            assertEquals(
                ReceivedUri(stateContext, inputUriString, mockGoogleMapsUrlConverter, redirectUri, Permission.ALWAYS),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_htmlPatternDoesNotMatchHtmlAndHtmlRedirectPatternIsMissingMatchingGroup_returnsSucceededWithPositionFromUrl() =
        runTest {
            val inputUriString = "https://g.co/kgs/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val html = "<html></html>"
            val positionFromUrl = Position(q = "fromUrl")
            val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
                on { find(any()) } doReturn null
            }
            val mockHtmlRedirectPattern: ConversionFirstHtmlPattern<RedirectRegex> = mock {
                on { find(any()) } doReturn listOf(object : RedirectRegex("mock") {
                    override fun matches(input: String) = true
                    override val url = null
                })
            }
            val mockAppleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
                on { conversionHtmlRedirectPattern }.doReturn(mockHtmlRedirectPattern)
            }
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doReturn html
            }
            val stateContext = mockStateContext(
                urlConverters = listOf(mockAppleMapsUrlConverter),
                networkTools = mockNetworkTools,
            )
            val state = GrantedParseHtmlToGetCoordsPermission(
                stateContext,
                inputUriString,
                mockAppleMapsUrlConverter,
                uri,
                positionFromUrl,
            )
            assertEquals(
                ConversionSucceeded(inputUriString, positionFromUrl),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlToGetCoordsPermission_htmlPatternDoesNotMatchHtmlAndHtmlRedirectPatternAlsoDoesNotMatchHtml_returnsSucceededWithPositionFromUrl() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, fakeUriQuote)
            val html = "<html></html>"
            val positionFromUrl = Position(q = "fromUrl")
            val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
                on { find(any()) } doReturn null
            }
            val mockHtmlRedirectPattern: ConversionFirstHtmlPattern<RedirectRegex> = mock {
                on { find(any()) } doReturn null
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionHtmlPattern }.doReturn(mockHtmlPattern)
                on { conversionHtmlRedirectPattern }.doReturn(mockHtmlRedirectPattern)
            }
            val mockNetworkTools: NetworkTools = mock {
                onBlocking { requestLocationHeader(any()) } doThrow NotImplementedError()
                onBlocking { getText(any()) } doReturn html
            }
            val stateContext = mockStateContext(
                urlConverters = listOf(mockGoogleMapsUrlConverter),
                networkTools = mockNetworkTools,
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
    fun deniedParseHtmlToGetCoordsPermission_returnsSucceededWithPositionFromUrl() = runTest {
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
