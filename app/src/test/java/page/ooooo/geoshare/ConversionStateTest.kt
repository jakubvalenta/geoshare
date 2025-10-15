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
import page.ooooo.geoshare.lib.converters.*
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException

class ConversionStateTest {
    private val fakeLog = FakeLog()
    private val uriQuote = FakeUriQuote()
    private val fakeUserPreferencesRepository: UserPreferencesRepository = FakeUserPreferencesRepository()
    private val geoUrlConverter = GeoUrlConverter()
    private val googleMapsUrlConverter = GoogleMapsUrlConverter()
    private val mockIntentTools: IntentTools = mock {
        on { getIntentUriString(any()) } doThrow NotImplementedError()
        on { createChooserIntent(any()) } doThrow NotImplementedError()
    }
    private val mockNetworkTools: NetworkTools = mock {
        onBlocking {
            requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
        } doThrow NotImplementedError()
        onBlocking {
            getText(url = any(), retry = anyOrNull(), dispatcher = any())
        } doThrow NotImplementedError()
        onBlocking {
            getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
        } doThrow NotImplementedError()
    }

    fun mockStateContext(
        intentTools: IntentTools = mockIntentTools,
        userPreferencesRepository: UserPreferencesRepository = fakeUserPreferencesRepository,
        urlConverters: List<UrlConverter> = listOf(geoUrlConverter, googleMapsUrlConverter),
        networkTools: NetworkTools = mockNetworkTools,
        log: ILog = fakeLog,
        uriQuote: UriQuote = this@ConversionStateTest.uriQuote,
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
    fun receivedIntent_intentDoesNotContainUrl_returnsConversionFailed() = runTest {
        val intent = Intent()
        val mockIntentTools: IntentTools = mock {
            on { getIntentUriString(intent) } doReturn null
        }
        val stateContext = mockStateContext(intentTools = mockIntentTools)
        val state = ReceivedIntent(stateContext, intent)
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
        val state = ReceivedIntent(stateContext, intent)
        assertEquals(
            ReceivedUriString(stateContext, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isGeoUri_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "geo:1,2?q="
        val uri = Uri.parse(inputUriString, uriQuote)
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
        val uri = Uri.parse(matchedInputUriString, uriQuote)
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
        val uri = Uri.parse(inputUriString, uriQuote)
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
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isEmpty_returnsConversionFailed() = runTest {
        val inputUriString = ""
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isMissingScheme_returnsConversionFailed() = runTest {
        val inputUriString = "maps.google.com/"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isRelativeScheme_returnsConversionFailed() = runTest {
        val inputUriString = "//maps.google.com/"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_isNotHttpsScheme_returnsConversionFailed() = runTest {
        val inputUriString = "ftp://maps.google.com/"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_unsupported_service, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_uriIsUnsupportedMapService_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.example.com/foo"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
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
        val state = ReceivedUri(stateContext, inputUriString, googleMapsUrlConverter, uri, Permission.NEVER)
        assertEquals(
            UnshortenedUrl(stateContext, inputUriString, googleMapsUrlConverter, uri, Permission.NEVER),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_converterDoesNotSupportShortUri_returnsUnshortenedUrlAndPassesPermission() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)

        class MockUrlConverter : UrlConverter.WithUriPattern {
            override val uriPattern: Pattern = Pattern.compile(".")
            override val documentation = Documentation(nameResId = -1, inputs = emptyList())
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
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
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
        val uri = Uri.parse(inputUriString, uriQuote)
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
        val uri = Uri.parse(inputUriString, uriQuote)
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
            val uri = Uri.parse(inputUriString, uriQuote)
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
            val uri = Uri.parse(inputUriString, uriQuote)
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
            val uri = Uri.parse(inputUriString, uriQuote)
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
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = RequestedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
        assertNull(state.transition())
    }

    @Test
    fun requestedUnshortenPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), anyOrNull()) } doReturn Unit
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
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), anyOrNull()) } doReturn Unit
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
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), anyOrNull()) } doReturn Unit
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
        val uri = Uri.parse(inputUriString, uriQuote)
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { setValue(eq(connectionPermission), anyOrNull()) } doReturn Unit
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
            val uri = Uri.parse(inputUriString, uriQuote)
            val redirectUriString = "https://maps.google.com/foo-redirect"
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doReturn redirectUriString
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
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
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    requestLocationHeader(
                        url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any()
                    )
                } doThrow CancellationException()
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
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
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    requestLocationHeader(
                        url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any()
                    )
                } doThrow tr
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext, inputUriString, googleMapsUrlConverter, uri, NetworkTools.Retry(1, tr),
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
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    requestLocationHeader(
                        url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any()
                    )
                } doThrow tr
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedUnshortenPermission(
                stateContext, inputUriString, googleMapsUrlConverter, uri, retry = NetworkTools.Retry(1, tr),
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext, inputUriString, googleMapsUrlConverter, uri, NetworkTools.Retry(2, tr),
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderThrowsUnexpectedResponseCodeException_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    requestLocationHeader(
                        url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any()
                    )
                } doThrow NetworkTools.UnrecoverableException(
                    R.string.network_exception_server_response_error,
                    Exception(),
                )
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
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
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    requestLocationHeader(
                        url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any()
                    )
                } doThrow NetworkTools.UnrecoverableException(
                    R.string.network_exception_server_response_error,
                    SocketTimeoutException(),
                )
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
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
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    requestLocationHeader(
                        url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any()
                    )
                } doReturn null
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
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
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    requestLocationHeader(
                        url = argThat { toString() == "https://$inputUriString" },
                        retry = anyOrNull(),
                        dispatcher = any(),
                    )
                } doReturn redirectUriString
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
            assertEquals(
                UnshortenedUrl(stateContext, inputUriString, googleMapsUrlConverter, redirectUri, Permission.ALWAYS),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderReturnsAbsoluteUrl_returnsUnshortenedUrl() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val redirectUriString = "https://maps.google.com/foo-redirect"
        val redirectUri = Uri.parse(redirectUriString, uriQuote)
        val mockNetworkTools: NetworkTools = mock {
            onBlocking {
                requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
            } doThrow NotImplementedError()
            onBlocking {
                requestLocationHeader(
                    url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any()
                )
            } doReturn redirectUriString
            onBlocking {
                getText(url = any(), retry = anyOrNull(), dispatcher = any())
            } doThrow NotImplementedError()
            onBlocking {
                getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
            } doThrow NotImplementedError()
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
        val uri = Uri.parse(inputUriString, uriQuote)
        val redirectUriString = "foo-redirect"
        val redirectUri = Uri.parse("$inputUriString/$redirectUriString", uriQuote)
        val mockNetworkTools: NetworkTools = mock {
            onBlocking {
                requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
            } doThrow NotImplementedError()
            onBlocking {
                requestLocationHeader(
                    url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any()
                )
            } doReturn redirectUriString
            onBlocking { getText(url = any(), retry = anyOrNull(), dispatcher = any()) } doThrow NotImplementedError()
            onBlocking {
                getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
            } doThrow NotImplementedError()
        }
        val stateContext = mockStateContext(networkTools = mockNetworkTools)
        val state = GrantedUnshortenPermission(stateContext, inputUriString, googleMapsUrlConverter, uri)
        assertEquals(
            UnshortenedUrl(stateContext, inputUriString, googleMapsUrlConverter, redirectUri, Permission.ALWAYS),
            state.transition(),
        )
    }

    @Test
    fun grantedUnshortenPermission_urlConverterHasShortUriMethodGet_usesGetRedirectUrlString() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val redirectUriString = "https://maps.google.com/foo-redirect"
        val redirectUri = Uri.parse(redirectUriString, uriQuote)
        val mockNetworkTools: NetworkTools = mock {
            onBlocking {
                requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
            } doThrow NotImplementedError()
            onBlocking { getText(url = any(), retry = anyOrNull(), dispatcher = any()) } doThrow NotImplementedError()
            onBlocking {
                getRedirectUrlString(
                    url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any()
                )
            } doReturn redirectUriString
        }

        class MockUrlConverter : UrlConverter.WithShortUriPattern {
            override val uriPattern: Pattern = Pattern.compile(".")
            override val documentation = Documentation(nameResId = -1, inputs = emptyList())
            override val shortUriPattern: Pattern = Pattern.compile(".")
            override val shortUriMethod = ShortUriMethod.GET
            override val permissionTitleResId = -1
            override val loadingIndicatorTitleResId = -1
        }

        val mockUrlConverter = MockUrlConverter()
        val stateContext = mockStateContext(
            urlConverters = listOf(mockUrlConverter),
            networkTools = mockNetworkTools,
        )
        val state = GrantedUnshortenPermission(stateContext, inputUriString, mockUrlConverter, uri)
        assertEquals(
            UnshortenedUrl(stateContext, inputUriString, mockUrlConverter, redirectUri, Permission.ALWAYS),
            state.transition(),
        )
    }

    @Test
    fun deniedConnectionPermission_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val stateContext = mockStateContext()
        val state = DeniedConnectionPermission(stateContext, inputUriString, googleMapsUrlConverter)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_connection_permission_denied, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_uriPatternDoesNotMatchInputUriString_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
            on { matches(any()) } doReturn null
        }
        val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
            on { conversionUriPattern } doReturn mockUriPattern
        }
        val stateContext = mockStateContext(urlConverters = listOf(mockGoogleMapsUrlConverter))
        val state = UnshortenedUrl(stateContext, inputUriString, googleMapsUrlConverter, uri, Permission.ALWAYS)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_url_error, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringAndThereIsAPoint_returnsSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val position = Position(lat = "1", lon = "2")
        val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
            on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                override fun matches(input: String) = false
                override val points = position.points
                override val q = position.q
            })
        }
        val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
            on { conversionUriPattern } doReturn mockUriPattern
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
            ConversionSucceeded(inputUriString, position),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringButThereIsNoPointAndPermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                    override val q = positionFromUri.q
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern } doReturn mockUriPattern
            }

            val stateContext = mockStateContext(urlConverters = listOf(mockGoogleMapsUrlConverter))
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, Permission.ALWAYS)
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, positionFromUri
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringButThereIsNoPointAndPermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                    override val q = positionFromUri.q
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern } doReturn mockUriPattern
            }
            val stateContext = mockStateContext(urlConverters = listOf(mockGoogleMapsUrlConverter))
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, Permission.ASK)
            assertEquals(
                RequestedParseHtmlPermission(
                    stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, positionFromUri
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringButThereIsNoPointAndPermissionIsNever_returnsParseHtmlFailed() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                    override val q = positionFromUri.q
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern } doReturn mockUriPattern
            }
            val stateContext = mockStateContext(urlConverters = listOf(mockGoogleMapsUrlConverter))
            val state = UnshortenedUrl(stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, Permission.NEVER)
            assertEquals(
                ParseHtmlFailed(inputUriString, positionFromUri),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringButThereIsNoPointAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                    override val q = positionFromUri.q
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern } doReturn mockUriPattern
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
                GrantedParseHtmlPermission(
                    stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, positionFromUri
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringButThereIsNoPointAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                    override val q = positionFromUri.q
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern } doReturn mockUriPattern
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
                RequestedParseHtmlPermission(
                    stateContext, inputUriString, mockGoogleMapsUrlConverter, uri, positionFromUri
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_uriPatternMatchesInputUriStringButThereIsNoPointAndPermissionIsNullAndPreferencePermissionIsNever_returnsParseHtmlFailed() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val mockUriPattern: ConversionFirstUriPattern<PositionRegex> = mock {
                on { matches(any()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = false
                    override val q = positionFromUri.q
                })
            }
            val mockGoogleMapsUrlConverter: GoogleMapsUrlConverter = mock {
                on { conversionUriPattern } doReturn mockUriPattern
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
                ParseHtmlFailed(inputUriString, positionFromUri),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_urlConverterHasNeitherConversionUriPatternNorConversionHtmlPattern_returnsParseHtmlFailed() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)

            class MockUrlConverter : UrlConverter.WithShortUriPattern {
                override val uriPattern: Pattern = Pattern.compile(".")
                override val documentation = Documentation(nameResId = -1, inputs = emptyList())
                override val shortUriPattern: Pattern = Pattern.compile(".")
                override val shortUriMethod = ShortUriMethod.HEAD
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }

            val mockUrlConverter = MockUrlConverter()
            val stateContext = mockStateContext(urlConverters = listOf(mockUrlConverter))
            val state = UnshortenedUrl(stateContext, inputUriString, mockUrlConverter, uri, Permission.ALWAYS)
            assertEquals(
                ParseHtmlFailed(inputUriString, null),
                state.transition(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val positionFromUri = Position(q = "bar")
        val stateContext = mockStateContext()
        val state =
            RequestedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri, positionFromUri)
        assertNull(state.transition())
    }

    @Test
    fun requestedParseHtmlPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state =
                RequestedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri, positionFromUri)
            assertEquals(
                GrantedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri, positionFromUri),
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
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state =
                RequestedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri, positionFromUri)
            assertEquals(
                GrantedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri, positionFromUri),
                state.grant(true),
            )
            verify(mockUserPreferencesRepository).setValue(
                connectionPermission,
                Permission.ALWAYS,
            )
        }

    @Test
    fun requestedParseHtmlPermission_denyWithDoNotAskFalse_doesNotSavePreferenceAndReturnsParseHtmlFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val positionFromUri = Position(q = "bar")
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state =
            RequestedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri, positionFromUri)
        assertEquals(
            ParseHtmlFailed(inputUriString, positionFromUri),
            state.deny(false),
        )
        verify(mockUserPreferencesRepository, never()).setValue(
            eq(connectionPermission),
            any<Permission>(),
        )
    }

    @Test
    fun requestedParseHtmlPermission_denyWithDoNotAskTrue_savesPreferenceAndReturnsParseHtmlFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val positionFromUri = Position(q = "bar")
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { setValue(eq(connectionPermission), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state =
            RequestedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri, positionFromUri)
        assertEquals(
            ParseHtmlFailed(inputUriString, positionFromUri),
            state.deny(true),
        )
        verify(mockUserPreferencesRepository).setValue(
            connectionPermission,
            Permission.NEVER,
        )
    }

    @Test
    fun grantedParseHtmlPermission_converterSupportsNeitherHtmlPatternNorRedirectPattern_returnsParseHtmlFailed() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val html = "<html></html>"
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any())
                } doReturn html
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }

            class MockUrlConverter : UrlConverter.WithHtmlPattern {
                override val uriPattern: Pattern = Pattern.compile(".")
                override val documentation = Documentation(nameResId = -1, inputs = emptyList())
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }

            val mockUrlConverter = MockUrlConverter()
            val stateContext = mockStateContext(
                urlConverters = listOf(mockUrlConverter),
                networkTools = mockNetworkTools,
            )
            val state = GrantedParseHtmlPermission(stateContext, inputUriString, mockUrlConverter, uri, positionFromUri)
            assertEquals(
                ParseHtmlFailed(inputUriString, positionFromUri),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_inputUriStringIsInvalidURL_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://[invalid:ipv6]/"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val html = "<html></html>"
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any())
                } doReturn html
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state =
                GrantedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri, positionFromUri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getTextThrowsCancellationException_returnsConversionFailedWithCancelledMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any())
                } doThrow CancellationException()
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state =
                GrantedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri, positionFromUri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_cancelled, inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getTextThrowsSocketTimeoutException_returnsGrantedParseHtmlPermissionWithRetry() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val tr = NetworkTools.RecoverableException(
                R.string.network_exception_socket_timeout, SocketTimeoutException(),
            )
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any())
                } doThrow tr
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state =
                GrantedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri, positionFromUri)
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    googleMapsUrlConverter,
                    uri,
                    positionFromUri,
                    NetworkTools.Retry(1, tr),
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getTextThrowsSocketTimeoutExceptionAndRetryIsOne_returnsGrantedParseHtmlPermissionWithRetryTwo() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val tr = NetworkTools.RecoverableException(
                R.string.network_exception_socket_timeout, SocketTimeoutException(),
            )
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any())
                } doThrow tr
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                googleMapsUrlConverter,
                uri,
                positionFromUri,
                retry = NetworkTools.Retry(1, tr),
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    googleMapsUrlConverter,
                    uri,
                    positionFromUri,
                    NetworkTools.Retry(2, tr),
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getTextThrowsUnexpectedResponseCodeException_returnsConversionFailedWithGeneralErrorMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any())
                } doThrow NetworkTools.UnrecoverableException(
                    R.string.network_exception_server_response_error,
                    Exception(),
                )
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state =
                GrantedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri, positionFromUri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getTextThrowsUnexpectedResponseCodeExceptionWithSocketTimeoutExceptionCause_returnsConversionFailedWithConnectionErrorMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any())
                } doThrow NetworkTools.UnrecoverableException(
                    R.string.network_exception_server_response_error,
                    SocketTimeoutException(),
                )
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(networkTools = mockNetworkTools)
            val state =
                GrantedParseHtmlPermission(stateContext, inputUriString, googleMapsUrlConverter, uri, positionFromUri)
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_connection_error, inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_inputUriStringHasNoScheme_callsGetTextWithUrlWithHttpsScheme() = runTest {
        val inputUriString = "maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val positionFromUri = Position(q = "bar")
        val html = "<html></html>"
        val position = Position("1", "2", q = "fromHtml")
        val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
            on { find(anyOrNull()) } doReturn listOf(object : PositionRegex("mock") {
                override fun matches(input: String) = true
                override val points = position.points
                override val q = position.q
            })
        }
        val mockNetworkTools: NetworkTools = mock {
            onBlocking {
                requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
            } doThrow NotImplementedError()
            onBlocking { getText(url = any(), retry = anyOrNull(), dispatcher = any()) } doThrow NotImplementedError()
            onBlocking {
                getText(
                    url = argThat { toString() == "https://$inputUriString" }, retry = anyOrNull(), dispatcher = any()
                )
            } doReturn html
            onBlocking {
                getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
            } doThrow NotImplementedError()
        }

        class MockUrlConverter : UrlConverter.WithHtmlPattern {
            override val uriPattern: Pattern = Pattern.compile(".")
            override val documentation = Documentation(nameResId = -1, inputs = emptyList())
            override val conversionHtmlPattern = mockHtmlPattern
            override val permissionTitleResId = -1
            override val loadingIndicatorTitleResId = -1
        }

        val mockUrlConverter = MockUrlConverter()
        val stateContext = mockStateContext(
            urlConverters = listOf(mockUrlConverter),
            networkTools = mockNetworkTools,
        )
        val state = GrantedParseHtmlPermission(stateContext, inputUriString, mockUrlConverter, uri, positionFromUri)
        assertEquals(
            ConversionSucceeded(inputUriString, position),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlPermission_htmlPatternMatchesHtml_callsGetHtmlUrlAndGetTextAndReturnsSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val positionFromUri = Position(q = "bar")
        val htmlUrl = URL("https://api.apple.com/foo.json")
        val html = "<html></html>"
        val positionFromHtml = Position("1", "2", q = "fromHtml")
        val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
            on { find(anyOrNull()) } doReturn listOf(object : PositionRegex("mock") {
                override fun matches(input: String) = true
                override val points = positionFromHtml.points
                override val q = positionFromHtml.q
            })
        }
        val mockNetworkTools: NetworkTools = mock {
            onBlocking {
                requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
            } doThrow NotImplementedError()
            onBlocking { getText(url = any(), retry = anyOrNull(), dispatcher = any()) } doThrow NotImplementedError()
            onBlocking {
                getText(url = argThat { toString() == htmlUrl.toString() }, retry = anyOrNull(), dispatcher = any())
            } doReturn html
            onBlocking {
                getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
            } doThrow NotImplementedError()
        }

        class MockUrlConverter : UrlConverter.WithHtmlPattern {
            override val uriPattern: Pattern = Pattern.compile(".")
            override val documentation = Documentation(nameResId = -1, inputs = emptyList())
            override val conversionHtmlPattern = mockHtmlPattern
            override fun getHtmlUrl(uri: Uri): URL = htmlUrl
            override val permissionTitleResId = -1
            override val loadingIndicatorTitleResId = -1
        }

        val mockUrlConverter = MockUrlConverter()
        val stateContext = mockStateContext(
            urlConverters = listOf(mockUrlConverter),
            networkTools = mockNetworkTools,
        )
        val state = GrantedParseHtmlPermission(stateContext, inputUriString, mockUrlConverter, uri, positionFromUri)
        assertEquals(
            ConversionSucceeded(inputUriString, positionFromHtml),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlPermission_htmlPatternMatchesHtmlAndGetHtmlUrlReturnsNull_returnsConversionFailedWithGenericMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val htmlUrl = URL("https://api.apple.com/foo.json")
            val html = "<html></html>"
            val positionFromHtml = Position("1", "2", q = "fromHtml")
            val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
                on { find(anyOrNull()) } doReturn listOf(object : PositionRegex("mock") {
                    override fun matches(input: String) = true
                    override val points = positionFromHtml.points
                    override val q = positionFromHtml.q
                })
            }
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = argThat { toString() == htmlUrl.toString() }, retry = anyOrNull(), dispatcher = any())
                } doReturn html
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }

            class MockUrlConverter : UrlConverter.WithHtmlPattern {
                override val uriPattern: Pattern = Pattern.compile(".")
                override val documentation = Documentation(nameResId = -1, inputs = emptyList())
                override val conversionHtmlPattern = mockHtmlPattern
                override fun getHtmlUrl(uri: Uri) = null
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }

            val mockUrlConverter = MockUrlConverter()
            val stateContext = mockStateContext(
                urlConverters = listOf(mockUrlConverter),
                networkTools = mockNetworkTools,
            )
            val state = GrantedParseHtmlPermission(
                stateContext, inputUriString, mockUrlConverter, uri, positionFromUri
            )
            assertEquals(
                ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_htmlPatternDoesNotMatchHtmlButHtmlRedirectPatternMatchesHtmlAbsoluteUrl_returnsReceivedUriWithTheRedirectUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val redirectUriString = "https://maps.apple.com/foo-redirect"
            val redirectUri = Uri.parse(redirectUriString, uriQuote)
            val html = "<html></html>"
            val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
                on { find(anyOrNull()) } doReturn null
            }
            val mockHtmlRedirectPattern: ConversionFirstHtmlPattern<RedirectRegex> = mock {
                on { find(anyOrNull()) } doReturn listOf(object : RedirectRegex("mock") {
                    override fun matches(input: String) = true
                    override val url = redirectUriString
                })
            }
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any())
                } doReturn html
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }

            class MockUrlConverter : UrlConverter.WithHtmlPattern {
                override val uriPattern: Pattern = Pattern.compile(".")
                override val documentation = Documentation(nameResId = -1, inputs = emptyList())
                override val conversionHtmlPattern = mockHtmlPattern
                override val conversionHtmlRedirectPattern = mockHtmlRedirectPattern
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }

            val mockUrlConverter = MockUrlConverter()
            val stateContext = mockStateContext(
                urlConverters = listOf(mockUrlConverter),
                networkTools = mockNetworkTools,
            )
            val state = GrantedParseHtmlPermission(
                stateContext, inputUriString, mockUrlConverter, uri, positionFromUri
            )
            assertEquals(
                ReceivedUri(stateContext, inputUriString, mockUrlConverter, redirectUri, Permission.ALWAYS),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_htmlPatternDoesNotMatchHtmlButHtmlRedirectPatternMatchesHtmlRelativeUrl_returnsReceivedUriWithTheRedirectUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val redirectUriString = "foo-redirect"
            val redirectUri = Uri.parse("$inputUriString/$redirectUriString", uriQuote)
            val html = "<html></html>"
            val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
                on { find(anyOrNull()) } doReturn null
            }
            val mockHtmlRedirectPattern: ConversionFirstHtmlPattern<RedirectRegex> = mock {
                on { find(anyOrNull()) } doReturn listOf(object : RedirectRegex("mock") {
                    override fun matches(input: String) = true
                    override val url = redirectUriString
                })
            }
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any())
                } doReturn html
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }

            class MockUrlConverter : UrlConverter.WithHtmlPattern {
                override val uriPattern: Pattern = Pattern.compile(".")
                override val documentation = Documentation(nameResId = -1, inputs = emptyList())
                override val conversionHtmlPattern = mockHtmlPattern
                override val conversionHtmlRedirectPattern = mockHtmlRedirectPattern
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }

            val mockUrlConverter = MockUrlConverter()
            val stateContext = mockStateContext(
                urlConverters = listOf(mockUrlConverter),
                networkTools = mockNetworkTools,
            )
            val state = GrantedParseHtmlPermission(
                stateContext, inputUriString, mockUrlConverter, uri, positionFromUri
            )
            assertEquals(
                ReceivedUri(stateContext, inputUriString, mockUrlConverter, redirectUri, Permission.ALWAYS),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_htmlPatternDoesNotMatchHtmlAndHtmlRedirectPatternIsMissingMatchingGroup_returnsParseHtmlFailed() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val html = "<html></html>"
            val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
                on { find(anyOrNull()) } doReturn null
            }
            val mockHtmlRedirectPattern: ConversionFirstHtmlPattern<RedirectRegex> = mock {
                on { find(anyOrNull()) } doReturn listOf(object : RedirectRegex("mock") {
                    override fun matches(input: String) = true
                    override val url = null
                })
            }
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any())
                } doReturn html
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }

            class MockUrlConverter : UrlConverter.WithHtmlPattern {
                override val uriPattern: Pattern = Pattern.compile(".")
                override val documentation = Documentation(nameResId = -1, inputs = emptyList())
                override val conversionHtmlPattern = mockHtmlPattern
                override val conversionHtmlRedirectPattern = mockHtmlRedirectPattern
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }

            val mockUrlConverter = MockUrlConverter()
            val stateContext = mockStateContext(
                urlConverters = listOf(mockUrlConverter),
                networkTools = mockNetworkTools,
            )
            val state = GrantedParseHtmlPermission(
                stateContext, inputUriString, mockUrlConverter, uri, positionFromUri
            )
            assertEquals(
                ParseHtmlFailed(inputUriString, positionFromUri),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_htmlPatternDoesNotMatchHtmlAndHtmlRedirectPatternAlsoDoesNotMatchHtml_returnsParseHtmlFailed() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val positionFromUri = Position(q = "bar")
            val html = "<html></html>"
            val mockHtmlPattern: ConversionFirstHtmlPattern<PositionRegex> = mock {
                on { find(anyOrNull()) } doReturn null
            }
            val mockHtmlRedirectPattern: ConversionFirstHtmlPattern<RedirectRegex> = mock {
                on { find(anyOrNull()) } doReturn null
            }
            val mockNetworkTools: NetworkTools = mock {
                onBlocking {
                    requestLocationHeader(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
                onBlocking {
                    getText(url = argThat { toString() == inputUriString }, retry = anyOrNull(), dispatcher = any())
                } doReturn html
                onBlocking {
                    getRedirectUrlString(url = any(), retry = anyOrNull(), dispatcher = any())
                } doThrow NotImplementedError()
            }

            class MockUrlConverter : UrlConverter.WithHtmlPattern {
                override val uriPattern: Pattern = Pattern.compile(".")
                override val documentation = Documentation(nameResId = -1, inputs = emptyList())
                override val conversionHtmlPattern = mockHtmlPattern
                override val conversionHtmlRedirectPattern = mockHtmlRedirectPattern
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }

            val mockUrlConverter = MockUrlConverter()
            val stateContext = mockStateContext(
                urlConverters = listOf(mockUrlConverter),
                networkTools = mockNetworkTools,
            )
            val state = GrantedParseHtmlPermission(
                stateContext, inputUriString, mockUrlConverter, uri, positionFromUri
            )
            assertEquals(
                ParseHtmlFailed(inputUriString, positionFromUri),
                state.transition(),
            )
        }

    @Test
    fun parseHtmlFailed_positionFromUriHasPoint_returnsConversionSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val positionFromUri = Position(lat = "1", lon = "2")
        val state = ParseHtmlFailed(inputUriString, positionFromUri)
        assertEquals(
            ConversionSucceeded(inputUriString, positionFromUri),
            state.transition(),
        )
    }

    @Test
    fun parseHtmlFailed_positionFromUriHasQuery_returnsConversionSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val positionFromUri = Position(q = "foo")
        val state = ParseHtmlFailed(inputUriString, positionFromUri)
        assertEquals(
            ConversionSucceeded(inputUriString, positionFromUri),
            state.transition(),
        )
    }

    @Test
    fun parseHtmlFailed_positionFromUriIsEmpty_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val positionFromUri = Position()
        val state = ParseHtmlFailed(inputUriString, positionFromUri)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString),
            state.transition(),
        )
    }

    @Test
    fun parseHtmlFailed_positionFromUriIsNull_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val state = ParseHtmlFailed(inputUriString, null)
        assertEquals(
            ConversionFailed(R.string.conversion_failed_parse_html_error, inputUriString),
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
        val inputUriString = "https://maps.apple.com/foo"
        val state = ConversionFailed(R.string.conversion_failed_missing_url, inputUriString)
        assertNull(state.transition())
    }
}
