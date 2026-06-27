package page.ooooo.geoshare.lib.inputs

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeGeoShareGoogleMapsAddressServer
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.di.FakeKeyStoreTools
import page.ooooo.geoshare.data.di.FakeServerRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.ResponseNetworkException
import page.ooooo.geoshare.lib.network.ServerHttpClientFactory
import page.ooooo.geoshare.lib.network.SocketTimeoutNetworkException
import page.ooooo.geoshare.lib.network.UnknownNetworkException
import java.net.SocketTimeoutException

class GoogleMapsPlaceApiInputTest {
    private val server = FakeGeoShareGoogleMapsAddressServer
    private val placeId = "ChIJKxjxuaNqkFQR3CK6O1HNNqY"
    private val engine = MockEngine { request ->
        when (request.url.toString()) {
            server.getUrl(placeId, uriQuote) -> respond(
                // language=Json
                """
                    {
                        "place": "//places.googleapis.com/places/foo",
                        "location": {"latitude": 50.123456, "longitude": -120.123456}
                    }
                """.trimIndent(),
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )

            server.getUrl("empty-object", uriQuote) -> respond(
                // language=Json
                """{}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )

            server.getUrl("invalid", uriQuote) -> respond(
                // language=Json
                """{"location": "invalid"}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )

            server.getUrl("exception", uriQuote) -> throw SocketTimeoutException()

            server.getUrl("unknown-exception", uriQuote) -> throw IllegalArgumentException()

            server.getUrl("bad-request", uriQuote) -> respondError(HttpStatusCode.BadRequest)

            server.getUrl("not-found", uriQuote) -> respondError(HttpStatusCode.NotFound)

            server.getUrl("405", uriQuote) -> respondError(HttpStatusCode.MethodNotAllowed)

            else -> throw NotImplementedError()
        }
    }
    private val keyStoreTools = FakeKeyStoreTools()
    private val log = FakeLog
    private val serverRepository: FakeServerRepository = mock {
        on { getSelectedGoogleMapsPlace() } doReturn server
    }
    private val uriQuote = FakeUriQuote
    private val userPreferencesRepository = FakeUserPreferencesRepository()
    private val input = GoogleMapsPlaceApiInput(
        serverRepository = serverRepository,
        serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
        googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
        uriQuote = uriQuote,
    )

    @Test
    fun parse_whenServerIsNotConfigured_returnsPointsFromUriAndNextStep() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsPlace() } doReturn null
        }
        val input = GoogleMapsPlaceApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(placeId = placeId, source = Source.URI)),
                next = MatchedInput(
                    FakeInputRepository.googleMapsHtmlInput,
                    "https://www.google.com/maps/search/?query_place_id=$placeId"
                ),
            ),
            input.fetchAndParse("https://www.google.com/maps/search/?query_place_id=$placeId"),
        )
    }

    @Test
    fun parse_whenPlaceIdIsFoundInUriAndApiReturnsResult_returnsPointsWithResultAsLastPointCoordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        50.123456, -120.123456,
                        placeId = placeId, source = Source.API,
                    )
                )
            ),
            input.fetchAndParse("https://www.google.com/maps/search/?query_place_id=$placeId"),
        )
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        50.123456,
                        -120.123456,
                        name = "foo", placeId = placeId, source = Source.API
                    )
                )
            ),
            input.fetchAndParse("https://www.google.com/maps/search/?query_place_id=$placeId&query=foo"),
        )
    }

    @Test
    fun parse_whenQueryWithCoordinatesIsFoundInUri_doesNotCallTheApi() = runTest {
        val engine = MockEngine { throw NotImplementedError() }
        val input = GoogleMapsPlaceApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        47.5951518, -122.3316393,
                        source = Source.URI,
                    )
                )
            ),
            input.fetchAndParse("https://www.google.com/maps/search/?query_place_id=$placeId&query=47.5951518,-122.3316393&api=1"),
        )
    }

    @Test
    fun parse_whenPlaceIdIsNotFoundInUri_returnsNoPoints() = runTest {
        assertEquals(
            ParseResult(),
            input.fetchAndParse("https://www.google.com/spam"),
        )
    }

    @Test
    fun parse_whenPlaceIdIsEmpty_returnsNoPoints() = runTest {
        assertEquals(
            ParseResult(),
            input.fetchAndParse("https://www.google.com/maps/search/?query_place_id="),
        )
    }

    @Test(expected = UnknownNetworkException::class)
    fun parse_whenApiReturnsEmptyObject_throwsException() = runTest {
        input.fetchAndParse("https://www.google.com/maps/search/?query_place_id=empty-object")
    }

    @Test(expected = UnknownNetworkException::class)
    fun parse_whenApiReturnsInvalidResponse_throwsException() = runTest {
        input.fetchAndParse("https://www.google.com/maps/search/?query_place_id=invalid")
    }

    @Test
    fun parse_whenApiReturns400_returnsPointsWithoutCoordinates() = runTest {
        assertEquals(
            ParseResult(persistentListOf(GCJ02MainlandChinaPoint(placeId = "bad-request", source = Source.URI))),
            input.fetchAndParse("https://www.google.com/maps/search/?query_place_id=bad-request"),
        )
    }

    @Test
    fun parse_whenApiReturns404_returnsPointsWithoutCoordinates() = runTest {
        assertEquals(
            ParseResult(persistentListOf(GCJ02MainlandChinaPoint(placeId = "not-found", source = Source.URI))),
            input.fetchAndParse("https://www.google.com/maps/search/?query_place_id=not-found"),
        )
    }

    @Test(expected = ResponseNetworkException::class)
    fun parse_whenApiReturnsOther4xx_throwsException() = runTest {
        input.fetchAndParse("https://www.google.com/maps/search/?query_place_id=405")
    }

    @Test(expected = SocketTimeoutNetworkException::class)
    fun parse_whenApiThrowsKnownException_throwsNetworkException() = runTest {
        input.fetchAndParse("https://www.google.com/maps/search/?query_place_id=exception")
    }

    @Test(expected = UnknownNetworkException::class)
    fun parse_whenApiThrowsUnknownException_throwsUnknownNetworkException() = runTest {
        input.fetchAndParse("https://www.google.com/maps/search/?query_place_id=uknown-exception")
    }

    private suspend fun GoogleMapsPlaceApiInput.fetchAndParse(match: String): ParseResult =
        fetch(match) { data -> parse(data, match) }
}
