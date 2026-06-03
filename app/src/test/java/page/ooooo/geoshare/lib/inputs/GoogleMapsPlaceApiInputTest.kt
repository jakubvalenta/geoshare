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
    private val placeId = @Suppress("SpellCheckingInspection") "ChIJKxjxuaNqkFQR3CK6O1HNNqY"
    private val engine = MockEngine { request ->
        when (request.url.toString()) {
            server.getUrl(placeId, uriQuote) -> respond(
                // language=Json
                """
                    {
                        "place": "//places.googleapis.com/places/foo",
                        "location": {"latitude": 50.123456, "longitude": -11.123456}
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

            else -> throw NotImplementedError()
        }
    }
    private val keyStoreTools = FakeKeyStoreTools()
    private val log = FakeLog
    private val uriQuote = FakeUriQuote
    private val userPreferencesRepository = FakeUserPreferencesRepository()

    @Test
    fun parse_whenServerIsNotConfigured_returnsNextStep() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsPlace() } doReturn null
        }
        val input = GoogleMapsPlaceApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        val match = "https://www.google.com/maps/search/?query_place_id=$placeId"
        assertEquals(
            ParseResult(
                nextStep = NextStep(FakeInputRepository.googleMapsHtmlInput, match),
            ),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test
    fun parse_whenPlaceIdIsInQueryParamAndApiReturnsResult_returnsPoint() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsPlace() } doReturn server
        }
        val input = GoogleMapsPlaceApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        val match = "https://www.google.com/maps/search/?query_place_id=$placeId&query=47.5951518,-122.3316393&api=1"
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(50.123456, -11.123456, source = Source.API)
                )
            ),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test
    fun parse_whenQueryIsNotFoundInUri_returnsNoPoints() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsPlace() } doReturn server
        }
        val input = GoogleMapsPlaceApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        val match = "https://www.google.com/spam"
        assertEquals(
            ParseResult(),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test
    fun parse_whenQueryIsEmpty_returnsNoPoints() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsPlace() } doReturn server
        }
        val input = GoogleMapsPlaceApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        val match = "https://www.google.com/maps/search/?query_place_id="
        assertEquals(
            ParseResult(),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test(expected = UnknownNetworkException::class)
    fun parse_whenApiReturnsEmptyObject_throwsException() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsPlace() } doReturn server
        }
        val input = GoogleMapsPlaceApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        val match = "https://www.google.com/maps/search/?query_place_id=empty-object"
        input.fetch(match) { data -> input.parse(data, match) }
    }

    @Test(expected = UnknownNetworkException::class)
    fun parse_whenApiReturnsInvalidResponse_throwsException() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsPlace() } doReturn server
        }
        val input = GoogleMapsPlaceApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        val match = "https://www.google.com/maps/search/?query_place_id=invalid"
        input.fetch(match) { data -> input.parse(data, match) }
    }

    @Test
    fun parse_whenApiReturns400_returnsNoPoints() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsPlace() } doReturn server
        }
        val input = GoogleMapsPlaceApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        val match = "https://www.google.com/maps/search/?query_place_id=bad-request"
        assertEquals(
            ParseResult(),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test(expected = ResponseNetworkException::class)
    fun parse_whenApiReturns404_throwsException() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsPlace() } doReturn server
        }
        val input = GoogleMapsPlaceApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        val match = "https://www.google.com/maps/search/?query_place_id=not-found"
        input.fetch(match) { data -> input.parse(data, match) }
    }

    @Test(expected = SocketTimeoutNetworkException::class)
    fun parse_whenApiThrowsKnownException_throwsNetworkException() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsPlace() } doReturn server
        }
        val input = GoogleMapsPlaceApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        val match = "https://www.google.com/maps/search/?query_place_id=exception"
        input.fetch(match) { data -> input.parse(data, match) }
    }

    @Test(expected = UnknownNetworkException::class)
    fun parse_whenApiThrowsUnknownException_throwsUnknownNetworkException() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsPlace() } doReturn server
        }
        val input = GoogleMapsPlaceApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        val match = "https://www.google.com/maps/search/?query_place_id=uknown-exception"
        input.fetch(match) { data -> input.parse(data, match) }
    }
}
