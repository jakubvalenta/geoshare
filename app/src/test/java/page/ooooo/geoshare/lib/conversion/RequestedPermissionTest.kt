package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class RequestedPermissionTest {
    @Test
    fun requestedUnshortenPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = RequestedUnshortenPermission(stateContext, inputUriString, GoogleMapsUriInput, uri)
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
                stateContext, inputUriString, GoogleMapsUriInput, uri
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsUriInput, uri),
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
                stateContext, inputUriString, GoogleMapsUriInput, uri
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsUriInput, uri),
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
                stateContext, inputUriString, GoogleMapsUriInput, uri
            )
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsUriInput),
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
        val state = RequestedUnshortenPermission(stateContext, inputUriString, GoogleMapsUriInput, uri)
        assertEquals(
            DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsUriInput),
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
                stateContext, inputUriString, GoogleMapsUriInput, uri
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
                stateContext, inputUriString, GoogleMapsUriInput, uri
            )
            assertEquals(
                ConversionFailed(mockResources.getString(R.string.conversion_failed_cancelled), inputUriString),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderThrowsSocketTimeoutException_returnsGrantedUnshortenPermissionWithLastAttempt() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val cause = SocketTimeoutNetworkException(SocketTimeoutException())
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onRequestLocationHeader(url: URL): String? =
                        if (url.toString() == inputUriString) {
                            throw cause
                        } else {
                            super.onRequestLocationHeader(url)
                        }
                },
            )
            val state = GrantedUnshortenPermission(
                stateContext, inputUriString, GoogleMapsUriInput, uri
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsUriInput,
                    uri,
                    NetworkTools.Attempt(1, cause),
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderThrowsSocketTimeoutExceptionAndLastAttemptNumberIsOne_returnsGrantedUnshortenPermissionWithLastAttemptWithIncreasedCount() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val cause = SocketTimeoutNetworkException(SocketTimeoutException())
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onRequestLocationHeader(url: URL): String? =
                        if (url.toString() == inputUriString) {
                            throw cause
                        } else {
                            super.onRequestLocationHeader(url)
                        }
                },
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                GoogleMapsUriInput,
                uri,
                lastAttempt = NetworkTools.Attempt(1, cause),
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsUriInput,
                    uri,
                    NetworkTools.Attempt(2, cause),
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedUnshortenPermission_requestLocationHeaderThrowsResponseException_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onRequestLocationHeader(url: URL): String? =
                        if (url.toString() == inputUriString) {
                            throw ResponseNetworkException(HttpStatusCode.NotFound, Exception())
                        } else {
                            super.onRequestLocationHeader(url)
                        }
                },
            )
            val state = GrantedUnshortenPermission(
                stateContext, inputUriString, GoogleMapsUriInput, uri
            )
            assertEquals(
                ConversionFailed(
                    mockResources.getString(
                        R.string.conversion_failed_unshorten_error_with_reason,
                        mockResources.getString(
                            R.string.network_exception_response_error,
                            HttpStatusCode.NotFound.value
                        ),
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
                stateContext, inputUriString, GoogleMapsUriInput, uri
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
                stateContext, inputUriString, GoogleMapsUriInput, uri
            )
            assertEquals(
                UnshortenedUrl(
                    stateContext, inputUriString, GoogleMapsUriInput, redirectUri, Permission.ALWAYS
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
        val state = GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsUriInput, uri)
        assertEquals(
            UnshortenedUrl(
                stateContext, inputUriString, GoogleMapsUriInput, redirectUri, Permission.ALWAYS
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
        val state = GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsUriInput, uri)
        assertEquals(
            UnshortenedUrl(
                stateContext, inputUriString, GoogleMapsUriInput, redirectUri, Permission.ALWAYS
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
            override val pattern = Regex(".")
            override val documentation =
                InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())
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
    fun grantedUnshortenPermission_getLargeLoadingIndicator_lastAttemptIsNull_returnsIndicatorWithoutDescription() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val stateContext = mockStateContext()
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                GoogleMapsUriInput,
                uri,
                lastAttempt = null,
            )
            assertEquals(
                LoadingIndicator.Large(
                    title = "Connecting to Google...",
                ),
                state.getLargeLoadingIndicator(),
            )
        }

    @Test
    fun grantedUnshortenPermission_getLargeLoadingIndicator_lastAttemptNumberIsTwo_returnsIndicatorWithDescription() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val stateContext = mockStateContext()
            val state = GrantedUnshortenPermission(
                stateContext,
                inputUriString,
                GoogleMapsUriInput,
                uri,
                lastAttempt = NetworkTools.Attempt(
                    2,
                    ConnectionClosedNetworkException(EOFException()),
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
    fun requestedParseHtmlPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val htmlUriString = "$inputUriString/html"
        val stateContext = mockStateContext()
        val state = RequestedParseHtmlPermission(
            stateContext,
            inputUriString,
            GoogleMapsUriInput,
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
                GoogleMapsUriInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsUriInput,
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
                GoogleMapsUriInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsUriInput,
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
                GoogleMapsUriInput,
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
                GoogleMapsUriInput,
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
        val mockInput = object : BodyAsChannelInput {
            override val pattern = Regex(".")
            override val documentation =
                InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())
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
                GoogleMapsUriInput,
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
                GoogleMapsUriInput,
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
    fun grantedParseHtmlPermission_getSourceThrowsSocketTimeoutException_returnsGrantedParseHtmlPermissionWithLastAttempt() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val cause = SocketTimeoutNetworkException(SocketTimeoutException())
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                        throw cause
                    } else {
                        super.onGetSource(url)
                    }
                },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsUriInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsUriInput,
                    uri,
                    pointsFromUri,
                    htmlUriString,
                    NetworkTools.Attempt(1, cause),
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getSourceThrowsSocketTimeoutExceptionAndLastAttemptNumberIsOne_returnsGrantedParseHtmlPermissionWithLastAttemptTwo() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val cause = SocketTimeoutNetworkException(SocketTimeoutException())
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                        throw cause
                    } else {
                        super.onGetSource(url)
                    }
                },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsUriInput,
                uri,
                pointsFromUri,
                htmlUriString,
                lastAttempt = NetworkTools.Attempt(1, cause),
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsUriInput,
                    uri,
                    pointsFromUri,
                    htmlUriString,
                    NetworkTools.Attempt(2, cause),
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getSourceThrowsResponseException_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                        throw ResponseNetworkException(HttpStatusCode.NotFound, Exception())
                    } else {
                        super.onGetSource(url)
                    }
                },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsUriInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                ConversionFailed(
                    mockResources.getString(
                        R.string.conversion_failed_parse_html_error_with_reason,
                        mockResources.getString(
                            R.string.network_exception_response_error,
                            HttpStatusCode.NotFound.value,
                        ),
                    ),
                    inputUriString,
                ),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_getSourceThrowsResponseExceptionWithSocketTimeoutExceptionCause_returnsConversionFailedWithConnectionErrorMessage() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val stateContext = mockStateContext(
                networkTools = object : MockNetworkTools() {
                    override fun onGetSource(url: URL): String = if (url.toString() == htmlUriString) {
                        throw ResponseNetworkException(HttpStatusCode.NotFound, Exception())
                    } else {
                        super.onGetSource(url)
                    }
                },
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsUriInput,
                uri,
                pointsFromUri,
                htmlUriString,
            )
            assertEquals(
                ConversionFailed(
                    mockResources.getString(
                        R.string.conversion_failed_parse_html_error_with_reason,
                        mockResources.getString(
                            R.string.network_exception_response_error,
                            HttpStatusCode.NotFound.value,
                        ),
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
        val mockInput = object : BodyAsChannelInput {
            override val pattern = Regex(".")
            override val documentation =
                InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())
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
        val mockInput = object : BodyAsChannelInput {
            override val pattern = Regex(".")
            override val documentation =
                InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())
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
    fun requestedParseWebPermission_transition_returnsNull() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val webUriString = "$inputUriString/web"
        val stateContext = mockStateContext()
        val state = RequestedParseWebPermission(
            stateContext,
            inputUriString,
            GoogleMapsUriInput,
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
                GoogleMapsUriInput,
                uri,
                pointsFromUri,
                webUriString,
            )
            assertEquals(
                GrantedParseWebPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsUriInput,
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
                GoogleMapsUriInput,
                uri,
                pointsFromUri,
                webUriString,
            )
            assertEquals(
                GrantedParseWebPermission(
                    stateContext,
                    inputUriString,
                    GoogleMapsUriInput,
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
                GoogleMapsUriInput,
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
                GoogleMapsUriInput,
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

}
