package page.ooooo.geoshare.lib.conversion

import android.content.Context
import android.content.res.Resources
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
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
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.di.FakeGoogleMapsDisplayLink
import page.ooooo.geoshare.data.di.FakeLinkRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.preferences.AutomationDelayPreference
import page.ooooo.geoshare.data.local.preferences.AutomationPreference
import page.ooooo.geoshare.data.local.preferences.BillingCachedProductIdPreference
import page.ooooo.geoshare.data.local.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.CopyCoordsDecAutomation
import page.ooooo.geoshare.data.local.preferences.NoopAutomation
import page.ooooo.geoshare.data.local.preferences.OpenDisplayGeoUriAutomation
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.local.preferences.ShareLinkUriAutomation
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.TOMTOM_PACKAGE_NAME
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.Billing
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.CustomLinkFeature
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.formatters.GeoUriFormatter
import page.ooooo.geoshare.lib.formatters.GoogleMapsUriFormatter
import page.ooooo.geoshare.lib.formatters.GpxFormatter
import page.ooooo.geoshare.lib.formatters.MagicEarthUriFormatter
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.inputs.GeoUriInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsInput
import page.ooooo.geoshare.lib.inputs.HtmlInput
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.inputs.InputDocumentation
import page.ooooo.geoshare.lib.inputs.ParseHtmlResult
import page.ooooo.geoshare.lib.inputs.ParseUriResult
import page.ooooo.geoshare.lib.inputs.ShortUriInput
import page.ooooo.geoshare.lib.inputs.WebInput
import page.ooooo.geoshare.lib.network.NetworkTools
import page.ooooo.geoshare.lib.network.RecoverableNetworkException
import page.ooooo.geoshare.lib.network.UnrecoverableNetworkException
import page.ooooo.geoshare.lib.outputs.CopyCoordsDecOutput
import page.ooooo.geoshare.lib.outputs.NoopAction
import page.ooooo.geoshare.lib.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput
import page.ooooo.geoshare.lib.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareLinkUriOutput
import page.ooooo.geoshare.lib.outputs.SharePointsGpxOutput
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import java.io.EOFException
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

@OptIn(ExperimentalCoroutinesApi::class)
class ConversionStateTest {

    private open class MockNetworkTools : NetworkTools() {
        override suspend fun httpHeadLocationHeader(
            url: URL,
            retry: Retry?,
            dispatcher: CoroutineDispatcher,
        ): String? = onRequestLocationHeader(url)

        open fun onRequestLocationHeader(url: URL): String? {
            throw NotImplementedError()
        }

        override suspend fun httpGetRedirectedUrlString(
            url: URL,
            retry: Retry?,
            dispatcher: CoroutineDispatcher,
        ): String = onGetRedirectUrlString(url)

        open fun onGetRedirectUrlString(url: URL): String {
            throw NotImplementedError()
        }

        override suspend fun <T> httpGetBodyAsByteReadChannel(
            url: URL,
            retry: Retry?,
            dispatcher: CoroutineDispatcher,
            block: suspend (source: ByteReadChannel) -> T,
        ): T = block(onGetSource(url).byteInputStream().toByteReadChannel())

        open fun onGetSource(url: URL): String {
            throw NotImplementedError()
        }
    }

    private val mockResources: Resources = mock {
        on { getString(R.string.conversion_failed_cancelled) } doReturn "Cancelled"
        on { getString(R.string.conversion_failed_connection_permission_denied) } doReturn "This link is not supported without connecting to the map service"
        on { getString(R.string.conversion_failed_missing_url) } doReturn "Missing URL"
        on {
            getString(R.string.conversion_failed_parse_html_error_with_reason, "invalid URL")
        } doReturn "Failed to process web page due to: invalid URL"
        on {
            getString(R.string.conversion_failed_parse_html_error_with_reason, "no points")
        } doReturn "Failed to process web page due to: no points"
        on {
            getString(R.string.conversion_failed_parse_html_error_with_reason, "server response error")
        } doReturn "Failed to process web page due to: server response error"
        on {
            getString(R.string.conversion_failed_parse_html_error_with_reason, "timeout")
        } doReturn "Failed to process web page due to: timeout"
        on { getString(R.string.conversion_failed_parse_url_error) } doReturn "Failed to process map link"
        on { getString(R.string.conversion_failed_reason_invalid_url) } doReturn "invalid URL"
        on { getString(R.string.conversion_failed_reason_missing_header) } doReturn "missing HTTP header"
        on { getString(R.string.conversion_failed_reason_no_points) } doReturn "no points"
        on { getString(R.string.conversion_failed_reason_timeout) } doReturn "timeout"
        on {
            getString(R.string.conversion_failed_unshorten_error_with_reason, "invalid URL")
        } doReturn "Failed to resolve short link due to: invalid URL"
        on {
            getString(R.string.conversion_failed_unshorten_error_with_reason, "missing HTTP header")
        } doReturn "Failed to resolve short link due to: missing HTTP header"
        on {
            getString(R.string.conversion_failed_unshorten_error_with_reason, "server response error")
        } doReturn "Failed to resolve short link due to: server response error"
        on { getString(R.string.conversion_failed_unsupported_service) } doReturn "Unsupported map service"
        on { getString(R.string.converter_google_maps_loading_indicator_title) } doReturn "Connecting to Google..."
        on {
            getString(R.string.conversion_loading_indicator_description, 2, 10, "connection closed")
        } doReturn "Attempt 2 out of 10 due to connection closed."
        on { getString(R.string.conversion_succeeded_location_loading_indicator_title) } doReturn "Finding your location..."
        on { getString(R.string.network_exception_eof) } doReturn "connection closed"
        on { getString(R.string.network_exception_server_response_error) } doReturn "server response error"
    }
    private val mockContext: Context = mock {}
    private val geometries = Geometries(mockContext)
    private val coordinateConverter = CoordinateConverter(geometries)
    private val coordinateFormatter = CoordinateFormatter(coordinateConverter)
    private val geoUriFormatter = GeoUriFormatter(coordinateConverter)
    private val googleMapsUriFormatter = GoogleMapsUriFormatter(coordinateConverter)
    private val gpxFormatter = GpxFormatter(coordinateConverter)
    private val magicEarthUriFormatter = MagicEarthUriFormatter(coordinateConverter)
    private val uriFormatter = UriFormatter(coordinateConverter)
    private val outputRepository = OutputRepository(
        coordinateFormatter = coordinateFormatter,
        geoUriFormatter = geoUriFormatter,
        googleMapsUriFormatter = googleMapsUriFormatter,
        gpxFormatter = gpxFormatter,
        magicEarthUriFormatter = magicEarthUriFormatter,
        uriFormatter = uriFormatter,
    )
    private val linkRepository: LinkRepository = FakeLinkRepository()
    private val userPreferencesRepository: UserPreferencesRepository = FakeUserPreferencesRepository()
    private val uriQuote = FakeUriQuote

    private fun mockStateContext(
        inputs: List<Input> = listOf(GeoUriInput(geoUriFormatter, uriFormatter), GoogleMapsInput(uriFormatter)),
        networkTools: NetworkTools = MockNetworkTools(),
        linkRepository: LinkRepository = this@ConversionStateTest.linkRepository,
        userPreferencesRepository: UserPreferencesRepository = this@ConversionStateTest.userPreferencesRepository,
        billing: Billing = mock {},
        log: ILog = FakeLog,
        uriQuote: UriQuote = this@ConversionStateTest.uriQuote,
    ) = ConversionStateContext(
        inputs = inputs,
        networkTools = networkTools,
        linkRepository = linkRepository,
        outputRepository = outputRepository,
        resources = mockResources,
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
            ConversionFailed(mockResources.getString(R.string.conversion_failed_missing_url), inputUriString),
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
            ReceivedUri(stateContext, inputUriString, GeoUriInput(geoUriFormatter, uriFormatter), uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringHasUriInTheMiddle_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "FOO\nhttps://maps.google.com/foo\nBAR"
        val matchedInputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(matchedInputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ReceivedUri(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, null),
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
            ReceivedUri(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringIsNotValidUrl_returnsConversionFailed() = runTest {
        val inputUriString = "https://[invalid:ipv6]/"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(mockResources.getString(R.string.conversion_failed_unsupported_service), inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringDoesNotHaveScheme_returnsConversionFailed() = runTest {
        val inputUriString = "maps.google.com/"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(mockResources.getString(R.string.conversion_failed_unsupported_service), inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringHasRelativeScheme_returnsConversionFailed() = runTest {
        val inputUriString = "//maps.google.com/"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(mockResources.getString(R.string.conversion_failed_unsupported_service), inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringDoesNotHaveHttpsScheme_returnsConversionFailed() = runTest {
        val inputUriString = "ftp://maps.google.com/"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(mockResources.getString(R.string.conversion_failed_unsupported_service), inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringDoesNotMatchAnyInput_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.example.com/foo"
        val stateContext = mockStateContext()
        val state = ReceivedUriString(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(mockResources.getString(R.string.conversion_failed_unsupported_service), inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_inputSupportsShortUriAndItDoesNotMatchTheUri_returnsUnshortenedUrlAndPassesPermission() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = ReceivedUri(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, Permission.NEVER)
        assertEquals(
            UnshortenedUrl(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, Permission.NEVER),
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
                InputDocumentation(
                    id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                    nameResId = -1,
                    items = emptyList()
                )

            override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = ParseUriResult(persistentListOf())
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
                stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, Permission.ALWAYS
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri),
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
            val state = ReceivedUri(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, Permission.ASK)
            assertEquals(
                RequestedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri),
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
            val state = ReceivedUri(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, Permission.NEVER)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsInput(uriFormatter)),
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
            val state = ReceivedUri(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, null)
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri),
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
            val state = ReceivedUri(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, null)
            assertEquals(
                RequestedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri),
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
            val state = ReceivedUri(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, null)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsInput(uriFormatter)),
                state.transition(),
            )
        }

    @Test
    fun requestedUnshortenPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = RequestedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri)
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
                stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri),
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
                stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri),
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
                stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri
            )
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsInput(uriFormatter)),
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
        val state = RequestedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri)
        assertEquals(
            DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsInput(uriFormatter)),
            state.deny(true),
        )
        verify(mockUserPreferencesRepository).setValue(
            ConnectionPermissionPreference,
            Permission.NEVER,
        )
    }

    @Test
    fun grantedUnshortenPermission_inputUriStringIsInvalidURL_returnsConversionFailed() =
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
                stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri
            )
            assertEquals(
                ConversionFailed(
                    mockResources.getString(
                        R.string.conversion_failed_unshorten_error_with_reason,
                        mockResources.getString(R.string.conversion_failed_reason_invalid_url),
                    ),
                    inputUriString,
                ),
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
                stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri
            )
            assertEquals(
                ConversionFailed(mockResources.getString(R.string.conversion_failed_cancelled), inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderThrowsSocketTimeoutException_returnsGrantedUnshortenPermissionWithRetry() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val tr = RecoverableNetworkException(R.string.network_exception_socket_timeout, SocketTimeoutException())
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
                stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, NetworkTools.Retry(1, tr),
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderThrowsSocketTimeoutExceptionAndRetryIsOne_returnsGrantedUnshortenPermissionWithRetryWithIncreasedCount() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val tr = RecoverableNetworkException(R.string.network_exception_socket_timeout, SocketTimeoutException())
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
                GoogleMapsInput(uriFormatter),
                uri,
                retry = NetworkTools.Retry(
                    1,
                    RecoverableNetworkException(
                        R.string.network_exception_socket_timeout,
                        SocketTimeoutException()
                    ),
                )
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, NetworkTools.Retry(2, tr),
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderThrowsUnexpectedResponseCodeException_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onRequestLocationHeader(url: URL): String? =
                        if (url.toString() == inputUriString) {
                            throw UnrecoverableNetworkException(
                                R.string.network_exception_server_response_error, Exception()
                            )
                        } else {
                            super.onRequestLocationHeader(url)
                        }
                },
            )
            val state = GrantedUnshortenPermission(
                stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri
            )
            assertEquals(
                ConversionFailed(
                    mockResources.getString(
                        R.string.conversion_failed_unshorten_error_with_reason,
                        mockResources.getString(R.string.network_exception_server_response_error),
                    ),
                    inputUriString,
                ),
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
                            throw UnrecoverableNetworkException(
                                R.string.network_exception_server_response_error, SocketTimeoutException()
                            )
                        } else {
                            super.onRequestLocationHeader(url)
                        }
                },
            )
            val state = GrantedUnshortenPermission(
                stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri
            )
            assertEquals(
                ConversionFailed(
                    mockResources.getString(
                        R.string.conversion_failed_unshorten_error_with_reason,
                        mockResources.getString(R.string.network_exception_server_response_error),
                    ),
                    inputUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderReturnsNull_returnsConversionFailed() =
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
                stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri
            )
            assertEquals(
                ConversionFailed(
                    mockResources.getString(
                        R.string.conversion_failed_unshorten_error_with_reason,
                        mockResources.getString(R.string.conversion_failed_reason_missing_header),
                    ),
                    inputUriString,
                ),
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
                stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri
            )
            assertEquals(
                UnshortenedUrl(
                    stateContext, inputUriString, GoogleMapsInput(uriFormatter), redirectUri, Permission.ALWAYS
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
        val state = GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri)
        assertEquals(
            UnshortenedUrl(
                stateContext, inputUriString, GoogleMapsInput(uriFormatter), redirectUri, Permission.ALWAYS
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
        val state = GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri)
        assertEquals(
            UnshortenedUrl(
                stateContext, inputUriString, GoogleMapsInput(uriFormatter), redirectUri, Permission.ALWAYS
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
        val mockInput = object : ShortUriInput {
            override val uriPattern = Regex(".")
            override val documentation =
                InputDocumentation(
                    id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                    nameResId = -1,
                    items = emptyList()
                )
            override val shortUriPattern = Regex(".")
            override val shortUriMethod = ShortUriInput.Method.GET
            override val permissionTitleResId = -1
            override val loadingIndicatorTitleResId = -1
            override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = ParseUriResult(persistentListOf())
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
    fun grantedUnshortenPermission_getLargeLoadingIndicator_retryIsNull_returnsIndicatorWithoutDescription() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = GrantedUnshortenPermission(
            stateContext,
            inputUriString,
            GoogleMapsInput(uriFormatter),
            uri,
            retry = null,
        )
        assertEquals(
            LoadingIndicator.Large(
                title = "Connecting to Google...",
            ),
            state.getLargeLoadingIndicator(),
        )
    }

    @Test
    fun grantedUnshortenPermission_getLargeLoadingIndicator_retryIsOne_returnsIndicatorWithDescription() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = GrantedUnshortenPermission(
            stateContext,
            inputUriString,
            GoogleMapsInput(uriFormatter),
            uri,
            retry = NetworkTools.Retry(
                1,
                RecoverableNetworkException(R.string.network_exception_eof, EOFException()),
            ),
        )
        assertEquals(
            LoadingIndicator.Large(
                title = "Connecting to Google...",
                description = "Attempt 2 out of 10 due to connection closed.",
            ),
            state.getLargeLoadingIndicator(),
        )
    }

    @Test
    fun deniedConnectionPermission_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val stateContext = mockStateContext()
        val state = DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsInput(uriFormatter))
        assertEquals(
            ConversionFailed(
                mockResources.getString(R.string.conversion_failed_connection_permission_denied),
                inputUriString
            ),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parseUriReturnsPointWithCoordinates_returnsConversionSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val mockInput: GoogleMapsInput = mock {
            on { parseUri(any(), any()) } doReturn ParseUriResult(points)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(ConnectionPermissionPreference) } doThrow NotImplementedError()
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            inputs = listOf(mockInput),
        )
        val state = UnshortenedUrl(stateContext, inputUriString, mockInput, uri, null)
        assertEquals(
            ConversionSucceeded(stateContext, inputUriString, points),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parseUriReturnsEmptyPoints_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val mockInput: GoogleMapsInput = mock {
            on { parseUri(any(), any()) } doReturn ParseUriResult()
        }
        val stateContext = mockStateContext(inputs = listOf(mockInput))
        val state = UnshortenedUrl(
            stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, Permission.ALWAYS
        )
        assertEquals(
            ConversionFailed(mockResources.getString(R.string.conversion_failed_parse_url_error), inputUriString),
            state.transition(),
        )
    }

    @Test
    fun unshortenedUrl_parseUriReturnsEmptyPointsAndHtmlUriButInputDoesNotSupportHtmlParsing_returnsDeniedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf<WGS84Point>()
            val htmlUriString = "$inputUriString/html"
            val mockInput = object : Input {
                override val uriPattern = Regex(".")
                override val documentation =
                    InputDocumentation(
                        id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                        nameResId = -1,
                        items = emptyList()
                    )

                override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = ParseUriResult(points, htmlUriString)
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
    fun unshortenedUrl_parseUriReturnsPointWithNameOnlyAndHtmlUriAndPermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val mockInput: GoogleMapsInput = mock {
                on { parseUri(any(), any()) } doReturn ParseUriResult(points, htmlUriString)
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = UnshortenedUrl(
                stateContext, inputUriString, mockInput, uri, Permission.ALWAYS
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    mockInput,
                    uri,
                    points,
                    htmlUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPointWithNameOnlyAndHtmlUriAndPermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val mockInput: GoogleMapsInput = mock {
                on { parseUri(any(), any()) } doReturn ParseUriResult(points, htmlUriString)
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = UnshortenedUrl(
                stateContext, inputUriString, mockInput, uri, Permission.ASK
            )
            assertEquals(
                RequestedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    mockInput,
                    uri,
                    points,
                    htmlUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPointWithNameOnlyAndHtmlUriAndPermissionIsNever_returnsDeniedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val mockInput: GoogleMapsInput = mock {
                on { parseUri(any(), any()) } doReturn ParseUriResult(points, htmlUriString)
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = UnshortenedUrl(
                stateContext, inputUriString, mockInput, uri, Permission.NEVER
            )
            assertEquals(
                DeniedParseHtmlPermission(stateContext, inputUriString, points),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPointWithNameOnlyAndHtmlUriAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val mockInput: GoogleMapsInput = mock {
                on { parseUri(any(), any()) } doReturn ParseUriResult(points, htmlUriString)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(ConnectionPermissionPreference) } doReturn Permission.ALWAYS
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                inputs = listOf(mockInput),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockInput, uri, null)
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    mockInput,
                    uri,
                    points,
                    htmlUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPointWithNameOnlyAndHtmlUriAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val mockInput: GoogleMapsInput = mock {
                on { parseUri(any(), any()) } doReturn ParseUriResult(points, htmlUriString)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(ConnectionPermissionPreference) } doReturn Permission.ASK
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                inputs = listOf(mockInput),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockInput, uri, null)
            assertEquals(
                RequestedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    mockInput,
                    uri,
                    points,
                    htmlUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPointWithNameOnlyAndHtmlUriAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val mockInput: GoogleMapsInput = mock {
                on { parseUri(any(), any()) } doReturn ParseUriResult(points, htmlUriString)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(ConnectionPermissionPreference) } doReturn Permission.NEVER
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                inputs = listOf(mockInput),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockInput, uri, null)
            assertEquals(
                DeniedParseHtmlPermission(stateContext, inputUriString, points),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPointWithNameOnlyAndWebUriButInputDoesNotSupportWebParsing_returnsDeniedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf<WGS84Point>()
            val webUriString = "$inputUriString/web"
            val mockInput = object : Input {
                override val uriPattern = Regex(".")
                override val documentation =
                    InputDocumentation(
                        id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                        nameResId = -1,
                        items = emptyList()
                    )

                override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) =
                    ParseUriResult(points, webUriString = webUriString)
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
    fun unshortenedUrl_parseUriReturnsPointWithNameOnlyAndWebUriAndPermissionIsAlways_returnsGrantedParseWebPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val webUriString = "$inputUriString/web"
            val mockInput: GoogleMapsInput = mock {
                on { parseUri(any(), any()) } doReturn ParseUriResult(points, webUriString = webUriString)
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = UnshortenedUrl(
                stateContext, inputUriString, mockInput, uri, Permission.ALWAYS
            )
            assertEquals(
                GrantedParseWebPermission(
                    stateContext,
                    inputUriString,
                    mockInput,
                    uri,
                    points,
                    webUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPointWithNameOnlyAndWebUriAndPermissionIsAsk_returnsRequestedParseWebPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val webUriString = "$inputUriString/web"
            val mockInput: GoogleMapsInput = mock {
                on { parseUri(any(), any()) } doReturn ParseUriResult(points, webUriString = webUriString)
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = UnshortenedUrl(
                stateContext, inputUriString, mockInput, uri, Permission.ASK
            )
            assertEquals(
                RequestedParseWebPermission(
                    stateContext,
                    inputUriString,
                    mockInput,
                    uri,
                    points,
                    webUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPointWithNameOnlyAndWebUriAndPermissionIsNever_returnsDeniedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val webUriString = "$inputUriString/web"
            val mockInput: GoogleMapsInput = mock {
                on { parseUri(any(), any()) } doReturn ParseUriResult(points, webUriString = webUriString)
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = UnshortenedUrl(
                stateContext, inputUriString, mockInput, uri, Permission.NEVER
            )
            assertEquals(
                DeniedParseHtmlPermission(stateContext, inputUriString, points),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPointWithNameOnlyAndWebUriAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseWebPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val webUriString = "$inputUriString/web"
            val mockInput: GoogleMapsInput = mock {
                on { parseUri(any(), any()) } doReturn ParseUriResult(points, webUriString = webUriString)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(ConnectionPermissionPreference) } doReturn Permission.ALWAYS
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                inputs = listOf(mockInput),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockInput, uri, null)
            assertEquals(
                GrantedParseWebPermission(
                    stateContext,
                    inputUriString,
                    mockInput,
                    uri,
                    points,
                    webUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPointWithNameOnlyAndWebUriAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedParseWebPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val webUriString = "$inputUriString/web"
            val mockInput: GoogleMapsInput = mock {
                on { parseUri(any(), any()) } doReturn ParseUriResult(points, webUriString = webUriString)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(ConnectionPermissionPreference) } doReturn Permission.ASK
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                inputs = listOf(mockInput),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockInput, uri, null)
            assertEquals(
                RequestedParseWebPermission(
                    stateContext,
                    inputUriString,
                    mockInput,
                    uri,
                    points,
                    webUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPointWithNameOnlyAndWebUriAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val webUriString = "$inputUriString/web"
            val mockInput: GoogleMapsInput = mock {
                on { parseUri(any(), any()) } doReturn ParseUriResult(points, webUriString = webUriString)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(ConnectionPermissionPreference) } doReturn Permission.NEVER
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                inputs = listOf(mockInput),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockInput, uri, null)
            assertEquals(
                DeniedParseHtmlPermission(stateContext, inputUriString, points),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsPointWithNameOnly_returnsConversionSucceeded() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(
                WGS84Point(1.0, 2.0, source = Source.GENERATED),
                WGS84Point(name = "foo bar", source = Source.GENERATED),
            )
            val mockInput: GoogleMapsInput = mock {
                on { parseUri(any(), any()) } doReturn ParseUriResult(points)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(ConnectionPermissionPreference) } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                inputs = listOf(mockInput),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockInput, uri, null)
            assertEquals(
                ConversionSucceeded(stateContext, inputUriString, points),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parseUriReturnsEmptyPoint_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://maps.google.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val points = persistentListOf(WGS84Point(source = Source.GENERATED))
            val mockInput: GoogleMapsInput = mock {
                on { parseUri(any(), any()) } doReturn ParseUriResult(points)
            }
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(ConnectionPermissionPreference) } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(
                userPreferencesRepository = mockUserPreferencesRepository,
                inputs = listOf(mockInput),
            )
            val state = UnshortenedUrl(stateContext, inputUriString, mockInput, uri, null)
            assertEquals(
                ConversionFailed(mockResources.getString(R.string.conversion_failed_parse_url_error), inputUriString),
                state.transition(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val htmlUriString = "$inputUriString/html"
        val stateContext = mockStateContext()
        val state = RequestedParseHtmlPermission(
            stateContext,
            inputUriString,
            GoogleMapsInput(uriFormatter),
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
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput(uriFormatter),
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsInput(uriFormatter),
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
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput(uriFormatter),
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsInput(uriFormatter),
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
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput(uriFormatter),
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
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput(uriFormatter),
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
    fun grantedParseHtmlPermission_parseHtmlReturnsEmptyPoints_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val htmlUriString = "$inputUriString/html"
        val html = "<html></html>"
        val mockInput = object : HtmlInput {
            override val uriPattern = Regex(".")
            override val documentation =
                InputDocumentation(
                    id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                    nameResId = -1,
                    items = emptyList()
                )
            override val permissionTitleResId = -1
            override val loadingIndicatorTitleResId = -1
            override suspend fun parseUri(uri: Uri, uriQuote: UriQuote): ParseUriResult {
                throw NotImplementedError()
            }

            override suspend fun parseHtml(
                htmlUrlString: String,
                channel: ByteReadChannel,
                pointsFromUri: Points,
                uriQuote: UriQuote,
                log: ILog,
            ) = ParseHtmlResult()
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
            ConversionFailed(
                mockResources.getString(
                    R.string.conversion_failed_parse_html_error_with_reason,
                    mockResources.getString(R.string.conversion_failed_reason_no_points),
                ),
                inputUriString,
            ),
            state.transition(),
        )
    }

    @Test
    fun grantedParseHtmlPermission_inputUriStringIsInvalidURL_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://[invalid:ipv6]/"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
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
                GoogleMapsInput(uriFormatter),
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                ConversionFailed(
                    mockResources.getString(
                        R.string.conversion_failed_parse_html_error_with_reason,
                        mockResources.getString(R.string.conversion_failed_reason_invalid_url),
                    ),
                    inputUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getSourceThrowsCancellationException_returnsConversionFailedWithCancelledMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
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
                GoogleMapsInput(uriFormatter),
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                ConversionFailed(mockResources.getString(R.string.conversion_failed_cancelled), inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getSourceThrowsSocketTimeoutException_returnsGrantedParseHtmlPermissionWithRetry() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val tr = RecoverableNetworkException(R.string.network_exception_socket_timeout, SocketTimeoutException())
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
                GoogleMapsInput(uriFormatter),
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsInput(uriFormatter),
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
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val tr = RecoverableNetworkException(R.string.network_exception_socket_timeout, SocketTimeoutException())
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
                GoogleMapsInput(uriFormatter),
                uri,
                pointsFromUri,
                htmlUriString,
                retry = NetworkTools.Retry(1, tr),
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsInput(uriFormatter),
                    uri,
                    pointsFromUri,
                    htmlUriString,
                    NetworkTools.Retry(2, tr),
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getSourceThrowsUnexpectedResponseCodeException_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                        throw UnrecoverableNetworkException(
                            R.string.network_exception_server_response_error, Exception()
                        )
                    } else {
                        super.onGetSource(url)
                    }
                },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput(uriFormatter),
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                ConversionFailed(
                    mockResources.getString(
                        R.string.conversion_failed_parse_html_error_with_reason,
                        mockResources.getString(R.string.network_exception_server_response_error),
                    ),
                    inputUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getSourceThrowsUnexpectedResponseCodeExceptionWithSocketTimeoutExceptionCause_returnsConversionFailedWithConnectionErrorMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                        throw UnrecoverableNetworkException(
                            R.string.network_exception_server_response_error, SocketTimeoutException()
                        )
                    } else {
                        super.onGetSource(url)
                    }
                },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput(uriFormatter),
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                ConversionFailed(
                    mockResources.getString(
                        R.string.conversion_failed_parse_html_error_with_reason,
                        mockResources.getString(R.string.network_exception_server_response_error),
                    ),
                    inputUriString
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_htmlUriStringHasNoScheme_callsGetTextWithUrlWithHttpsScheme() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val html = "<html></html>"
        val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val htmlUriString = "maps.apple.com/foo"
        val pointsFromHtml = persistentListOf(WGS84Point(1.0, 2.0, name = "fromHtml", source = Source.GENERATED))
        val mockInput = object : HtmlInput {
            override val uriPattern = Regex(".")
            override val documentation =
                InputDocumentation(
                    id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                    nameResId = -1,
                    items = emptyList()
                )
            override val permissionTitleResId = -1
            override val loadingIndicatorTitleResId = -1
            override suspend fun parseUri(uri: Uri, uriQuote: UriQuote): ParseUriResult {
                throw NotImplementedError()
            }

            override suspend fun parseHtml(
                htmlUrlString: String,
                channel: ByteReadChannel,
                pointsFromUri: Points,
                uriQuote: UriQuote,
                log: ILog,
            ) = ParseHtmlResult(pointsFromHtml)
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
    fun grantedParseHtmlPermission_parseHtmlReturnsPointWithCoordinates_returnsConversionSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val html = "<html></html>"
        val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val htmlUriString = "https://api.apple.com/foo.json"
        val pointsFromHtml = persistentListOf(WGS84Point(1.0, 2.0, name = "fromHtml", source = Source.GENERATED))
        val mockInput = object : HtmlInput {
            override val uriPattern = Regex(".")
            override val documentation =
                InputDocumentation(
                    id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                    nameResId = -1,
                    items = emptyList()
                )
            override val permissionTitleResId = -1
            override val loadingIndicatorTitleResId = -1
            override suspend fun parseUri(uri: Uri, uriQuote: UriQuote): ParseUriResult {
                throw NotImplementedError()
            }

            override suspend fun parseHtml(
                htmlUrlString: String,
                channel: ByteReadChannel,
                pointsFromUri: Points,
                uriQuote: UriQuote,
                log: ILog,
            ) = ParseHtmlResult(pointsFromHtml)
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
    fun grantedParseHtmlPermission_parseHtmlReturnsEmptyPointsAndRedirectUriWithAbsoluteUrl_returnsReceivedUriWithTheUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val redirectUriString = "$inputUriString/redirect"
            val redirectUri = Uri.parse(redirectUriString, uriQuote)
            val mockInput = object : HtmlInput {
                override val uriPattern = Regex(".")
                override val documentation =
                    InputDocumentation(
                        id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                        nameResId = -1,
                        items = emptyList()
                    )
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
                override suspend fun parseUri(uri: Uri, uriQuote: UriQuote): ParseUriResult {
                    throw NotImplementedError()
                }

                override suspend fun parseHtml(
                    htmlUrlString: String,
                    channel: ByteReadChannel,
                    pointsFromUri: Points,
                    uriQuote: UriQuote,
                    log: ILog,
                ) = ParseHtmlResult(redirectUriString = redirectUriString)
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
    fun grantedParseHtmlPermission_parseHtmlReturnsEmptyPointsAndRedirectUriWithRelativeUrl_returnsReceivedUriWithTheUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val redirectUriString = "redirect"
            val redirectUri = Uri.parse("$inputUriString/$redirectUriString", uriQuote)
            val html = "<html></html>"
            val mockInput = object : HtmlInput {
                override val uriPattern = Regex(".")
                override val documentation =
                    InputDocumentation(
                        id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                        nameResId = -1,
                        items = emptyList()
                    )
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
                override suspend fun parseUri(uri: Uri, uriQuote: UriQuote): ParseUriResult {
                    throw NotImplementedError()
                }

                override suspend fun parseHtml(
                    htmlUrlString: String,
                    channel: ByteReadChannel,
                    pointsFromUri: Points,
                    uriQuote: UriQuote,
                    log: ILog,
                ) = ParseHtmlResult(redirectUriString = redirectUriString)
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
    fun grantedParseHtmlPermission_parseHtmlReturnsEmptyPointsAndWebUriAndInputSupportsWebParsing_returnsGrantedParseWebPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val webUriString = "$inputUriString/web"
            val html = "<html></html>"
            val mockInput = object : HtmlInput, WebInput {
                override val uriPattern = Regex(".")
                override val documentation =
                    InputDocumentation(
                        id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                        nameResId = -1,
                        items = emptyList()
                    )
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
                override suspend fun parseUri(uri: Uri, uriQuote: UriQuote): ParseUriResult {
                    throw NotImplementedError()
                }

                override suspend fun parseHtml(
                    htmlUrlString: String,
                    channel: ByteReadChannel,
                    pointsFromUri: Points,
                    uriQuote: UriQuote,
                    log: ILog,
                ) = ParseHtmlResult(webUriString = webUriString)
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
                GrantedParseWebPermission(stateContext, inputUriString, mockInput, uri, pointsFromUri, webUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsEmptyPointsAndWebUriAndInputDoesNotSupportWebParsing_returnsDeniedParseHtmlPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val webUriString = "$inputUriString/web"
            val html = "<html></html>"
            val mockInput = object : HtmlInput {
                override val uriPattern = Regex(".")
                override val documentation =
                    InputDocumentation(
                        id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                        nameResId = -1,
                        items = emptyList()
                    )
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
                override suspend fun parseUri(uri: Uri, uriQuote: UriQuote): ParseUriResult {
                    throw NotImplementedError()
                }

                override suspend fun parseHtml(
                    htmlUrlString: String,
                    channel: ByteReadChannel,
                    pointsFromUri: Points,
                    uriQuote: UriQuote,
                    log: ILog,
                ) = ParseHtmlResult(webUriString = webUriString)
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
                DeniedParseHtmlPermission(stateContext, inputUriString, pointsFromUri),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parseHtmlReturnsPointWithNameOnly_returnsConversionSucceeded() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val html = "<html></html>"
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "https://api.apple.com/foo.json"
            val pointsFromHtml = persistentListOf(
                WGS84Point(1.0, 2.0, source = Source.GENERATED),
                WGS84Point(name = "foo bar", source = Source.GENERATED),
            )
            val mockInput = object : HtmlInput {
                override val uriPattern = Regex(".")
                override val documentation =
                    InputDocumentation(
                        id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                        nameResId = -1,
                        items = emptyList()
                    )
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
                override suspend fun parseUri(uri: Uri, uriQuote: UriQuote): ParseUriResult {
                    throw NotImplementedError()
                }

                override suspend fun parseHtml(
                    htmlUrlString: String,
                    channel: ByteReadChannel,
                    pointsFromUri: Points,
                    uriQuote: UriQuote,
                    log: ILog,
                ) = ParseHtmlResult(pointsFromHtml)
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
    fun grantedParseHtmlPermission_parseHtmlReturnsEmptyPoint_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val html = "<html></html>"
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "https://api.apple.com/foo.json"
            val pointsFromHtml = persistentListOf(WGS84Point(source = Source.GENERATED))
            val mockInput = object : HtmlInput {
                override val uriPattern = Regex(".")
                override val documentation =
                    InputDocumentation(
                        id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                        nameResId = -1,
                        items = emptyList()
                    )
                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
                override suspend fun parseUri(uri: Uri, uriQuote: UriQuote): ParseUriResult {
                    throw NotImplementedError()
                }

                override suspend fun parseHtml(
                    htmlUrlString: String,
                    channel: ByteReadChannel,
                    pointsFromUri: Points,
                    uriQuote: UriQuote,
                    log: ILog,
                ) = ParseHtmlResult(pointsFromHtml)
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
                ConversionFailed(
                    mockResources.getString(
                        R.string.conversion_failed_parse_html_error_with_reason,
                        mockResources.getString(R.string.conversion_failed_reason_no_points),
                    ),
                    inputUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getLargeLoadingIndicator_retryIsNull_returnsIndicatorWithoutDescription() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val htmlUriString = "$inputUriString/html"
        val stateContext = mockStateContext()
        val state = GrantedParseHtmlPermission(
            stateContext,
            inputUriString,
            GoogleMapsInput(uriFormatter),
            uri,
            pointsFromUri,
            htmlUriString,
            retry = null,
        )
        assertEquals(
            LoadingIndicator.Large(
                title = "Connecting to Google...",
            ),
            state.getLargeLoadingIndicator(),
        )
    }

    @Test
    fun grantedParseHtmlPermission_getLargeLoadingIndicator_retryIsOne_returnsIndicatorWithDescription() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val htmlUriString = "$inputUriString/html"
        val stateContext = mockStateContext()
        val state = GrantedParseHtmlPermission(
            stateContext,
            inputUriString,
            GoogleMapsInput(uriFormatter),
            uri,
            pointsFromUri,
            htmlUriString,
            retry = NetworkTools.Retry(
                1,
                RecoverableNetworkException(R.string.network_exception_eof, EOFException()),
            ),
        )
        assertEquals(
            LoadingIndicator.Large(
                title = "Connecting to Google...",
                description = "Attempt 2 out of 10 due to connection closed.",
            ),
            state.getLargeLoadingIndicator(),
        )
    }

    @Test
    fun deniedParseHtmlPermission_lastPointHasCoords_returnsConversionSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
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
        val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val stateContext = mockStateContext()
        val state = DeniedParseHtmlPermission(stateContext, inputUriString, points)
        assertEquals(
            ConversionSucceeded(stateContext, inputUriString, points),
            state.transition(),
        )
    }

    @Test
    fun deniedParseHtmlPermission_lastPointIsEmpty_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val points = persistentListOf(WGS84Point(source = Source.GENERATED))
        val stateContext = mockStateContext()
        val state = DeniedParseHtmlPermission(stateContext, inputUriString, points)
        assertEquals(
            ConversionFailed(
                mockResources.getString(R.string.conversion_failed_connection_permission_denied),
                inputUriString,
            ),
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
            ConversionFailed(
                mockResources.getString(R.string.conversion_failed_connection_permission_denied),
                inputUriString,
            ),
            state.transition(),
        )
    }

    @Test
    fun requestedParseWebPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val webUriString = "$inputUriString/web"
        val stateContext = mockStateContext()
        val state = RequestedParseWebPermission(
            stateContext,
            inputUriString,
            GoogleMapsInput(uriFormatter),
            uri,
            points,
            webUriString,
        )
        assertNull(state.transition())
    }

    @Test
    fun requestedParseWebPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedParseWebPermission() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val webUriString = "$inputUriString/web"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseWebPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput(uriFormatter),
                uri,
                pointsFromUri,
                webUriString,
            )
            assertEquals(
                GrantedParseWebPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsInput(uriFormatter),
                    uri,
                    pointsFromUri,
                    webUriString,
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
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val webUriString = "$inputUriString/web"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseWebPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput(uriFormatter),
                uri,
                pointsFromUri,
                webUriString,
            )
            assertEquals(
                GrantedParseWebPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsInput(uriFormatter),
                    uri,
                    pointsFromUri,
                    webUriString,
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
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val webUriString = "$inputUriString/web"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseWebPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput(uriFormatter),
                uri,
                pointsFromUri,
                webUriString,
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
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val webUriString = "$inputUriString/web"
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = RequestedParseWebPermission(
                stateContext,
                inputUriString,
                GoogleMapsInput(uriFormatter),
                uri,
                pointsFromUri,
                webUriString,
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
    fun grantedParseWebPermission_isCancelled_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val webUriString = "$inputUriString/web"
        val stateContext = mockStateContext()
        val state = GrantedParseWebPermission(
            stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, pointsFromUri, webUriString
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
        assertEquals(
            ConversionFailed(mockResources.getString(R.string.conversion_failed_cancelled), inputUriString),
            res,
        )
    }

    @Test
    fun grantedParseWebPermission_isNotCancelledAndOnUrlChangeIsNotCalledWithinTimeout_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val webUriString = "$inputUriString/web"
            val timeout = 7.seconds
            val stateContext = mockStateContext()
            val state = GrantedParseWebPermission(
                stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, pointsFromUri, webUriString, timeout
            )
            val workDuration = testScheduler.timeSource.measureTime {
                assertEquals(
                    ConversionFailed(
                        mockResources.getString(
                            R.string.conversion_failed_parse_html_error_with_reason,
                            mockResources.getString(R.string.conversion_failed_reason_timeout),
                        ),
                        inputUriString,
                    ),
                    state.transition(),
                )
            }
            assertEquals(timeout, workDuration)
        }

    @Test
    fun grantedParseWebPermission_urlChangeIsCalledAndUriPatternMatchesAndParseUriReturnsPoints_returnsConversionSucceeded() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val resPoints = persistentListOf(WGS84Point(3.0, 4.0, source = Source.GENERATED))
            val webUriString = "$inputUriString/web"
            val urlString = "$webUriString/current"
            val mockInput = object : WebInput {
                override val uriPattern = Regex("""^https://maps\.apple\.com/\S*""")
                override val documentation =
                    InputDocumentation(
                        id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                        nameResId = -1,
                        items = emptyList()
                    )

                override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) =
                    if (uri.toString() == urlString) {
                        ParseUriResult(resPoints)
                    } else {
                        throw NotImplementedError()
                    }

                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = GrantedParseWebPermission(
                stateContext, inputUriString, mockInput, uri, pointsFromUri, webUriString
            )
            var res: State? = null
            launch {
                res = state.transition()
            }
            state.onUrlChange(urlString)
            advanceUntilIdle()
            assertEquals(
                ConversionSucceeded(stateContext, inputUriString, resPoints),
                res,
            )
        }

    @Test
    fun grantedParseWebPermission_urlChangeIsCalledAndUriPatternMatchesAndParseUriReturnsEmptyPoints_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val webUriString = "$inputUriString/web"
            val urlString = "$webUriString/current"
            val mockInput = object : WebInput {
                override val uriPattern = Regex("""^https://maps\.apple\.com/\S*""")
                override val documentation =
                    InputDocumentation(
                        id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                        nameResId = -1,
                        items = emptyList()
                    )

                override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) =
                    if (uri.toString() == urlString) {
                        ParseUriResult()
                    } else {
                        throw NotImplementedError()
                    }

                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = GrantedParseWebPermission(
                stateContext, inputUriString, mockInput, uri, pointsFromUri, webUriString
            )
            var res: State? = null
            launch {
                res = state.transition()
            }
            state.onUrlChange(urlString)
            advanceUntilIdle()
            assertEquals(
                ConversionFailed(
                    mockResources.getString(
                        R.string.conversion_failed_parse_html_error_with_reason,
                        mockResources.getString(R.string.conversion_failed_reason_no_points),
                    ),
                    inputUriString,
                ),
                res,
            )
        }

    @Test
    fun grantedParseWebPermission_urlChangeIsCalledAndUriPatternDoesNotMatch_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://spam.apple.com/"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val resPoints = persistentListOf(WGS84Point(3.0, 4.0, source = Source.GENERATED))
            val webUriString = "$inputUriString/web"
            val urlString = "$webUriString/current"
            val mockInput = object : WebInput {
                override val uriPattern = Regex("""^https://maps\.apple\.com/\S*""")
                override val documentation =
                    InputDocumentation(
                        id = GeoUriInput(geoUriFormatter, uriFormatter).documentation.id,
                        nameResId = -1,
                        items = emptyList()
                    )

                override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) =
                    if (uri.toString() == urlString) {
                        ParseUriResult(resPoints)
                    } else {
                        throw NotImplementedError()
                    }

                override val permissionTitleResId = -1
                override val loadingIndicatorTitleResId = -1
            }
            val stateContext = mockStateContext(inputs = listOf(mockInput))
            val state = GrantedParseWebPermission(
                stateContext, inputUriString, mockInput, uri, pointsFromUri, webUriString
            )
            var res: State? = null
            launch {
                res = state.transition()
            }
            state.onUrlChange(urlString)
            advanceUntilIdle()
            assertEquals(
                ConversionFailed(
                    mockResources.getString(
                        R.string.conversion_failed_parse_html_error_with_reason,
                        mockResources.getString(R.string.conversion_failed_reason_no_points),
                    ),
                    inputUriString,
                ),
                res,
            )
        }

    @Test
    fun grantedParseWebPermission_getLargeLoadingIndicator_returnsIndicatorWithoutDescription() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val webUriString = "$inputUriString/web"
        val stateContext = mockStateContext()
        val state = GrantedParseWebPermission(
            stateContext, inputUriString, GoogleMapsInput(uriFormatter), uri, pointsFromUri, webUriString
        )
        assertEquals(
            LoadingIndicator.Large(
                title = "Connecting to Google...",
            ),
            state.getLargeLoadingIndicator(),
        )
    }

    @Test
    fun conversionSucceeded_pointsAreEmpty_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf<Point>()
        val automation = CopyCoordsDecAutomation
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertNull(state.transition())
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsNoop_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = NoopAutomation
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertNull(state.transition())
    }

    @Test
    fun conversionSucceeded_billingStatusIsLoadingAndCachedProductIdIsNotSet_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(BillingStatus.Loading())
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
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
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(BillingStatus.Loading())
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
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
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateFormatter).toAction(points.last())
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(BillingStatus.Loading())
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(BillingCachedProductIdPreference) } doReturn "test"
            on { setValue(eq(BillingCachedProductIdPreference), any()) } doReturn Unit
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionReady(inputUriString, points, action, isAutomation = true),
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
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf()
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
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
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateFormatter).toAction(points.last())
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionReady(inputUriString, points, action, isAutomation = true),
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
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateFormatter).toAction(points.last())
        val mockStatus = MutableStateFlow<BillingStatus>(BillingStatus.Loading())
        val mockBilling: Billing = mock {
            on { status } doReturn mockStatus
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
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
            expired = false,
            refundable = true,
        )
        advanceUntilIdle()
        assertEquals(
            ActionReady(inputUriString, points, action, isAutomation = true),
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
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val mockStatus = MutableStateFlow<BillingStatus>(BillingStatus.NotPurchased(pending = false))
        val mockBilling: Billing = mock {
            on { status } doReturn mockStatus
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
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
            expired = false,
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
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val mockStatus = MutableStateFlow<BillingStatus>(BillingStatus.Loading())
        val mockBilling: Billing = mock {
            on { status } doReturn mockStatus
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
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
            expired = false,
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
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = CopyCoordsDecAutomation
        val action = CopyCoordsDecOutput(coordinateFormatter).toAction(points.last())
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionReady(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsOpenApp_returnsActionWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = OpenDisplayGeoUriAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val action = OpenDisplayGeoUriOutput(GOOGLE_MAPS_PACKAGE_NAME, geoUriFormatter).toAction(points.last())
        val delay = 2.seconds
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionWaiting(stateContext, inputUriString, points, action, isAutomation = true, delay = delay),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsOpenLink_returnsActionWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = ShareLinkUriAutomation(FakeGoogleMapsDisplayLink.uuid)
        val action = ShareLinkUriOutput(FakeGoogleMapsDisplayLink, uriFormatter).toAction(points.last())
        val delay = 2.seconds
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionWaiting(stateContext, inputUriString, points, action, isAutomation = true, delay = delay),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsOpenLinkAndLinkIsUnknown_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = ShareLinkUriAutomation(Link(name = "Link that is not in repository").uuid)
        val delay = 2.seconds
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertNull(state.transition())
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsSaveGpx_returnsActionWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = SavePointsGpxAutomation
        val action = SavePointsGpxOutput(gpxFormatter).toAction(points)
        val delay = 2.seconds
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionWaiting(stateContext, inputUriString, points, action, isAutomation = true, delay = delay),
            state.transition(),
        )
    }

    @Test
    fun conversionSucceeded_userPreferenceAutomationIsShare_returnsActionWaiting() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val automation = OpenDisplayGeoUriAutomation(GOOGLE_MAPS_PACKAGE_NAME)
        val action = OpenDisplayGeoUriOutput(GOOGLE_MAPS_PACKAGE_NAME, geoUriFormatter).toAction(points.last())
        val delay = 2.seconds
        val mockBilling: Billing = mock {
            on { status } doReturn MutableStateFlow(
                BillingStatus.Purchased(
                    product = BillingProduct("test", BillingProduct.Type.ONE_TIME),
                    expired = false,
                    refundable = true,
                )
            )
            on { products } doReturn persistentListOf(BillingProduct("test", BillingProduct.Type.ONE_TIME))
            on { features } doReturn persistentListOf(AutomationFeature, CustomLinkFeature)
        }
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(AutomationPreference) } doReturn automation
            on { getValue(AutomationDelayPreference) } doReturn delay
        }
        val stateContext = mockStateContext(
            userPreferencesRepository = mockUserPreferencesRepository,
            billing = mockBilling,
        )
        val state = ConversionSucceeded(stateContext, inputUriString, points)
        assertEquals(
            ActionWaiting(stateContext, inputUriString, points, action, isAutomation = true, delay = delay),
            state.transition(),
        )
    }

    @Test
    fun conversionFailed_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val state = ConversionFailed(mockResources.getString(R.string.conversion_failed_missing_url), inputUriString)
        assertNull(state.transition())
    }

    @Test
    fun actionWaiting_executionIsNotCancelled_waitsAndReturnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = SavePointsGpxOutput(gpxFormatter).toAction(points)
        val stateContext = mockStateContext()
        val state = ActionWaiting(stateContext, inputUriString, points, action, isAutomation = true, 3.seconds)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionReady(inputUriString, points, action, isAutomation = true),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun actionWaiting_delayIsNotPositive_doesNotWaitAndReturnsActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = SavePointsGpxOutput(gpxFormatter).toAction(points)
        val stateContext = mockStateContext()
        val state = ActionWaiting(stateContext, inputUriString, points, action, isAutomation = true, (-1).seconds)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionReady(inputUriString, points, action, isAutomation = true),
                state.transition(),
            )
        }
        assertEquals(0.seconds, workDuration)
    }

    @Test
    fun actionWaiting_executionIsCancelled_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = SavePointsGpxOutput(gpxFormatter).toAction(points)
        val stateContext = mockStateContext()
        val state = ActionWaiting(stateContext, inputUriString, points, action, isAutomation = true, 3.seconds)
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
            ActionFinished(inputUriString, points, action, isAutomation = true),
        )
    }

    @Test
    fun actionReady_actionIsCopyAutomation_returnsBasicActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = CopyCoordsDecOutput(coordinateFormatter).toAction(points.last())
        val state = ActionReady(inputUriString, points, action, isAutomation = false)
        assertEquals(
            BasicActionReady(inputUriString, points, action, isAutomation = false),
            state.transition(),
        )
    }

    @Test
    fun actionReady_actionIsCopyAction_returnsBasicActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = CopyCoordsDecOutput(coordinateFormatter).toAction(points.last())
        val state = ActionReady(inputUriString, points, action, isAutomation = true)
        assertEquals(
            BasicActionReady(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun actionReady_actionIsSaveGpxPoints_returnsFileUriRequested() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = SavePointsGpxOutput(gpxFormatter).toAction(points)
        val state = ActionReady(inputUriString, points, action, isAutomation = true)
        assertEquals(
            FileUriRequested(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun actionReady_actionIsShareGpxRouteAutomation_returnsLocationRationaleRequested() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(GOOGLE_MAPS_PACKAGE_NAME, gpxFormatter).toAction(points.last())
        val state = ActionReady(inputUriString, points, action, isAutomation = true)
        assertEquals(
            LocationRationaleRequested(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun actionReady_actionIsShareGpxRouteAction_returnsLocationRationaleRequested() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(GOOGLE_MAPS_PACKAGE_NAME, gpxFormatter).toAction(points.last())
        val state = ActionReady(inputUriString, points, action, isAutomation = false)
        assertEquals(
            LocationRationaleRequested(inputUriString, points, action, isAutomation = false),
            state.transition(),
        )
    }

    @Test
    fun basicActionReady_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = SharePointsGpxOutput(gpxFormatter).toAction(points)
        val state = BasicActionReady(inputUriString, points, action, isAutomation = true)
        assertNull(state.transition())
    }

    @Test
    fun fileActionReady_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = SavePointsGpxOutput(gpxFormatter).toAction(points)
        val state = FileActionReady(inputUriString, points, action, isAutomation = true, mock())
        assertNull(state.transition())
    }

    @Test
    fun locationActionReady_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(GOOGLE_MAPS_PACKAGE_NAME, gpxFormatter).toAction(points.last())
        val state = LocationActionReady(
            inputUriString,
            points,
            action,
            isAutomation = false,
            WGS84Point(3.0, 4.0, source = Source.GENERATED)
        )
        assertNull(state.transition())
    }

    @Test
    fun actionRan_successIsTrue_returnsActionSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = NoopAction
        val state = ActionRan(inputUriString, points, action, isAutomation = true, success = true)
        assertEquals(
            ActionSucceeded(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun actionRan_successIsFalse_returnsActionFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = NoopAction
        val state = ActionRan(inputUriString, points, action, isAutomation = true, success = false)
        assertEquals(
            ActionFailed(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun actionRan_successIsNull_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = NoopAction
        val state = ActionRan(inputUriString, points, action, isAutomation = true, success = null)
        assertEquals(
            ActionFinished(inputUriString, points, action, isAutomation = true),
            state.transition(),
        )
    }

    @Test
    fun actionSucceeded_executionIsNotCancelled_waitsAndReturnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenDisplayGeoUriOutput(GOOGLE_MAPS_PACKAGE_NAME, geoUriFormatter).toAction(points.last())
        val state = ActionSucceeded(inputUriString, points, action, isAutomation = true)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionFinished(inputUriString, points, action, isAutomation = true),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun actionSucceeded_executionIsCancelled_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenDisplayGeoUriOutput(GOOGLE_MAPS_PACKAGE_NAME, geoUriFormatter).toAction(points.last())
        val state = ActionSucceeded(inputUriString, points, action, isAutomation = true)
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
            ActionFinished(inputUriString, points, action, isAutomation = true),
        )
    }

    @Test
    fun actionFailed_executionIsNotCancelled_waitsAndReturnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenDisplayGeoUriOutput(GOOGLE_MAPS_PACKAGE_NAME, geoUriFormatter).toAction(points.last())
        val state = ActionFailed(inputUriString, points, action, isAutomation = true)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionFinished(inputUriString, points, action, isAutomation = true),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun actionFailed_executionIsCancelled_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenDisplayGeoUriOutput(GOOGLE_MAPS_PACKAGE_NAME, geoUriFormatter).toAction(points.last())
        val state = ActionFailed(inputUriString, points, action, isAutomation = true)
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
            ActionFinished(inputUriString, points, action, isAutomation = true),
        )
    }

    @Test
    fun actionFinished_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenDisplayGeoUriOutput(GOOGLE_MAPS_PACKAGE_NAME, geoUriFormatter).toAction(points.last())
        val state = ActionFinished(inputUriString, points, action, isAutomation = true)
        assertNull(state.transition())
    }

    @Test
    fun fileUriRequested_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = SavePointsGpxOutput(gpxFormatter).toAction(points)
        val state = FileUriRequested(inputUriString, points, action, isAutomation = false)
        assertNull(state.transition())
    }

    @Test
    fun locationRationaleRequested_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME, gpxFormatter).toAction(points.last())
        val state = LocationRationaleRequested(inputUriString, points, action, isAutomation = false)
        assertNull(state.transition())
    }

    @Test
    fun locationRationaleShown_grant_returnsLocationRationaleConfirmed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME, gpxFormatter).toAction(points.last())
        val state = LocationRationaleShown(inputUriString, points, action, isAutomation = false)
        assertEquals(
            LocationRationaleConfirmed(inputUriString, points, action, isAutomation = false),
            state.grant(false),
        )
    }

    @Test
    fun locationRationaleShown_deny_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME, gpxFormatter).toAction(points.last())
        val state = LocationRationaleShown(inputUriString, points, action, isAutomation = false)
        assertEquals(
            ActionFinished(inputUriString, points, action, isAutomation = false),
            state.deny(false),
        )
    }

    @Test
    fun locationRationaleConfirmed_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME, gpxFormatter).toAction(points.last())
        val state = LocationRationaleConfirmed(inputUriString, points, action, isAutomation = false)
        assertNull(state.transition())
    }

    @Test
    fun locationPermissionReceived_returnsNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME, gpxFormatter).toAction(points.last())
        val stateContext = mockStateContext()
        val state = LocationPermissionReceived(stateContext, inputUriString, points, action, isAutomation = false)
        assertNull(state.transition())
    }

    @Test
    fun locationPermissionReceived_getSmallLoadingIndicator_returnsLoadingIndicator() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME, gpxFormatter).toAction(points.last())
        val stateContext = mockStateContext()
        val state = LocationPermissionReceived(stateContext, inputUriString, points, action, isAutomation = false)
        assertEquals(
            LoadingIndicator.Small("Finding your location..."),
            state.getSmallLoadingIndicator(),
        )
    }

    @Test
    fun locationReceived_locationIsNull_returnsLocationFindingFailed() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME, gpxFormatter).toAction(points.last())
        val state = LocationReceived(inputUriString, points, action, isAutomation = false, location = null)
        assertEquals(
            LocationFindingFailed(inputUriString, points, action, isAutomation = false),
            state.transition(),
        )
    }

    @Test
    fun locationReceived_locationIsNotNull_returnsLocationActionReady() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME, gpxFormatter).toAction(points.last())
        val location = WGS84Point(3.0, 4.0, source = Source.GENERATED)
        val state = LocationReceived(inputUriString, points, action, isAutomation = false, location)
        assertEquals(
            LocationActionReady(inputUriString, points, action, isAutomation = false, location),
            state.transition(),
        )
    }

    @Test
    fun locationFindingFailed_executionIsNotCancelled_waitsAndReturnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME, gpxFormatter).toAction(points.last())
        val state = LocationFindingFailed(inputUriString, points, action, isAutomation = false)
        val workDuration = testScheduler.timeSource.measureTime {
            assertEquals(
                ActionFinished(inputUriString, points, action, isAutomation = false),
                state.transition(),
            )
        }
        assertEquals(3.seconds, workDuration)
    }

    @Test
    fun locationFindingFailed_executionIsCancelled_returnsActionFinished() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val action = OpenRouteOnePointGpxOutput(TOMTOM_PACKAGE_NAME, gpxFormatter).toAction(points.last())
        val state = LocationFindingFailed(inputUriString, points, action, isAutomation = false)
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
            ActionFinished(inputUriString, points, action, isAutomation = false),
        )
    }
}
