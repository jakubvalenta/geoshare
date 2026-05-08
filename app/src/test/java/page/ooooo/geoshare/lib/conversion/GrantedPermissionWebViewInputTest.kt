package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class GrantedPermissionWebViewInputTest {
    @Test
    fun grantedParseWebPermission_isCancelled_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val pointsFromUri = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val webUriString = "$inputUriString/web"
        val stateContext = mockStateContext()
        val state = GrantedParseWebPermission(
            stateContext, inputUriString, GoogleMapsUriInput, uri, pointsFromUri, webUriString
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
                stateContext, inputUriString, GoogleMapsUriInput, uri, pointsFromUri, webUriString, timeout
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
            val mockInput = object : WebViewInput {
                override val pattern = Regex("""^https://maps\.apple\.com/\S*""")
                override val documentation =
                    InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())

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
            val mockInput = object : WebViewInput {
                override val pattern = Regex("""^https://maps\.apple\.com/\S*""")
                override val documentation =
                    InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())

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
            val mockInput = object : WebViewInput {
                override val pattern = Regex("""^https://maps\.apple\.com/\S*""")
                override val documentation =
                    InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())

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
            stateContext, inputUriString, GoogleMapsUriInput, uri, pointsFromUri, webUriString
        )
        assertEquals(
            LoadingIndicator.Large(
                title = "Connecting to Google...",
            ),
            state.getLargeLoadingIndicator(),
        )
    }

}
