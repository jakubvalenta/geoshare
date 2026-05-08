package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class GrantedPermissionBasicInputTest {
    @Test
    fun unshortenedUrl_parseUriReturnsPointWithCoordinates_returnsConversionSucceeded() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val mockInput: GoogleMapsUriInput = mock {
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
        val mockInput: GoogleMapsUriInput = mock {
            on { parseUri(any(), any()) } doReturn ParseUriResult()
        }
        val stateContext = mockStateContext(inputs = listOf(mockInput))
        val state = UnshortenedUrl(
            stateContext, inputUriString, GoogleMapsUriInput, uri, Permission.ALWAYS
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
                override val pattern = Regex(".")
                override val documentation =
                    InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())

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
            val mockInput: GoogleMapsUriInput = mock {
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
            val mockInput: GoogleMapsUriInput = mock {
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
            val mockInput: GoogleMapsUriInput = mock {
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
            val mockInput: GoogleMapsUriInput = mock {
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
            val mockInput: GoogleMapsUriInput = mock {
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
            val mockInput: GoogleMapsUriInput = mock {
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
                override val pattern = Regex(".")
                override val documentation =
                    InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())

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
            val mockInput: GoogleMapsUriInput = mock {
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
            val mockInput: GoogleMapsUriInput = mock {
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
            val mockInput: GoogleMapsUriInput = mock {
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
            val mockInput: GoogleMapsUriInput = mock {
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
            val mockInput: GoogleMapsUriInput = mock {
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
            val mockInput: GoogleMapsUriInput = mock {
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
            val mockInput: GoogleMapsUriInput = mock {
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
            val mockInput: GoogleMapsUriInput = mock {
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
    fun grantedParseHtmlPermission_parseHtmlReturnsEmptyPointsAndRedirectUriWithAbsoluteUrl_returnsReceivedUriWithTheUrlAndPermissionAlways() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val redirectUriString = "$inputUriString/redirect"
            val redirectUri = Uri.parse(redirectUriString, uriQuote)
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
                FoundInput(stateContext, inputUriString, mockInput, redirectUri, Permission.ALWAYS),
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
                FoundInput(stateContext, inputUriString, mockInput, redirectUri, Permission.ALWAYS),
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
            val mockInput = object : BodyAsChannelInput, WebViewInput {
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
    fun grantedParseHtmlPermission_parseHtmlReturnsEmptyPoint_returnsConversionFailed() =
        runTest {
            val inputUriString = "https://maps.apple.com/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val html = "<html></html>"
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "https://api.apple.com/foo.json"
            val pointsFromHtml = persistentListOf(WGS84Point(source = Source.GENERATED))
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
    fun grantedParseHtmlPermission_getLargeLoadingIndicator_lastAttemptIsNull_returnsIndicatorWithoutDescription() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val stateContext = mockStateContext()
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsUriInput,
                uri,
                pointsFromUri,
                htmlUriString,
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
    fun grantedParseHtmlPermission_getLargeLoadingIndicator_lastAttemptNumberIsTwo_returnsIndicatorWithDescription() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
            val htmlUriString = "$inputUriString/html"
            val stateContext = mockStateContext()
            val state = GrantedParseHtmlPermission(
                stateContext,
                inputUriString,
                GoogleMapsUriInput,
                uri,
                pointsFromUri,
                htmlUriString,
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

}
