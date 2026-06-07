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

class GoogleMapsAddressApiInputTest {
    private val server = FakeGeoShareGoogleMapsAddressServer
    private val query = "Cherbourg, France"
    private val engine = MockEngine { request ->
        when (request.url.toString()) {
            server.getUrl("Cherbourg, France", uriQuote) -> respond(
                // language=Json
                """
                    {
                        "results": [
                            {"place": "//places.googleapis.com/places/foo", "location": {"latitude": 50.123456, "longitude": -11.123456}},
                            {"place": "//places.googleapis.com/places/bar", "location": {"latitude": 9, "longitude": -120}}
                        ]
                    }
                """.trimIndent(),
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )

            server.getUrl("empty-results", uriQuote) -> respond(
                // language=Json
                """{"results": []}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )

            server.getUrl("empty-object", uriQuote) -> respond(
                // language=Json
                """{}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )

            server.getUrl("invalid", uriQuote) -> respond(
                // language=Json
                """{"results": "invalid"}""",
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
    private val uriQuote = FakeUriQuote
    private val userPreferencesRepository = FakeUserPreferencesRepository()

    @Test
    fun parse_whenServerIsNotConfigured_returnsMatchedInput() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsAddress() } doReturn null
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            log = log,
            uriQuote = uriQuote,
        )
        val match = "https://maps.google.com/?q=$query"
        assertEquals(
            ParseResult(
                next = MatchedInput(FakeInputRepository.googleMapsHtmlInput, match),
            ),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test
    fun parse_whenQueryIsInQueryParamAndApiReturnsResults_returnsHighestRankedPoint() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsAddress() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            log = log,
            uriQuote = uriQuote,
        )
        for (match in listOf(
            @Suppress("SpellCheckingInspection") "https://www.google.com/maps/dir/?api=1&origin=Paris,France&destination=$query&travelmode=driving&waypoints=Versailles,France%7CChartres,France%7CLe%2BMans,France%7CCaen,France",
            "https://maps.google.com/maps?f=d&daddr=$query",
            @Suppress("SpellCheckingInspection") "https://maps.google.com?q=$query&ftid=0x47b8ac99b0a68bdd:0x8024629be3e9996&entry=gps&lucs=,94224825,94227247,94227248,47071704,47069508,94218641,94233073,94203019,47084304,94208458,94208447",
            "https://www.google.com/maps/search/?api=1&query=$query",
        )) {
            assertEquals(
                ParseResult(
                    persistentListOf(
                        GCJ02MainlandChinaPoint(50.123456, -11.123456, name = query, source = Source.API)
                    )
                ),
                input.fetch(match) { data -> input.parse(data, match) },
            )
        }
    }

    @Test
    fun parse_whenQueryHasAppendedCoordinates_removesTheCoordinatesAndReturnsHighestRankedPoint() = runTest {
        val query = "2088 Albion Rd+@43.7481,-79.6332"
        val cleanQuery = "2088 Albion Rd"
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                server.getUrl(cleanQuery, uriQuote) -> respond(
                    // language=Json
                    """
                        {
                            "results": [
                                {"place": "//places.googleapis.com/places/foo", "location": {"latitude": 50.123456, "longitude": -11.123456}}
                            ]
                        }
                    """.trimIndent(),
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )

                else -> throw NotImplementedError()
            }
        }
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsAddress() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            log = log,
            uriQuote = uriQuote,
        )
        val match = "https://maps.google.com/maps?f=d&daddr=${uriQuote.encode(query)}"
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(50.123456, -11.123456, name = cleanQuery, source = Source.API)
                )
            ),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test
    fun parse_whenQueryIsInPathAndApiReturnsResults_returnsHighestRankedPoint() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsAddress() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            log = log,
            uriQuote = uriQuote,
        )
        for (match in listOf(
            "https://www.google.com/maps/dir/New+York,+NY/Philadelphia,+PA/$query",
            @Suppress("SpellCheckingInspection") "https://www.google.com/maps/dir/Hermannstra%C3%9Fe+1,+12049+Berlin,+Germany/Weserstr.+1,+12047+Berlin,+Germany/$query/@52.4844406,13.4217121,16z/data=!3m1!4b1!4m20!4m19!1m5!1m1!1s0x47a84fb831937021:0x28d6914e5ca0f9f5!2m2!1d13.4236883!2d52.4858222!1m5!1m1!1s0x47a84fb7098f1d89:0x74c8a84ad2981e9f!2m2!1d13.4255518!2d52.4881038!1m5!1m1!1s0x47a84fbb7c0791d7:0xf6e39aaedab8b2d9!2m2!1d13.4300356!2d52.4807739!3e2",
            "https://www.google.com/maps/place/$query/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910",
            "https://www.google.com/maps/search/$query",
        )) {
            assertEquals(
                ParseResult(
                    persistentListOf(
                        GCJ02MainlandChinaPoint(50.123456, -11.123456, name = query, source = Source.API)
                    )
                ),
                input.fetch(match) { data -> input.parse(data, match) },
            )
        }
    }

    @Test
    fun parse_whenQueryIsNotFoundInUri_returnsNoPoints() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsAddress() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            log = log,
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
            on { getSelectedGoogleMapsAddress() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            log = log,
            uriQuote = uriQuote,
        )
        val match = "https://www.google.com/?q="
        assertEquals(
            ParseResult(),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test
    fun parse_whenApiReturnsEmptyResults_returnsNoPoints() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsAddress() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            log = log,
            uriQuote = uriQuote,
        )
        val match = "https://maps.google.com/?q=empty-results"
        assertEquals(
            ParseResult(),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test
    fun parse_whenApiReturnsEmptyObject_returnsNoPoints() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsAddress() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            log = log,
            uriQuote = uriQuote,
        )
        val match = "https://maps.google.com/?q=empty-object"
        assertEquals(
            ParseResult(),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test
    fun parse_whenApiReturnsInvalidResponse_returnsNoPoints() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsAddress() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            log = log,
            uriQuote = uriQuote,
        )
        val match = "https://maps.google.com/?q=invalid"
        assertEquals(
            ParseResult(),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test
    fun parse_whenApiReturns400_returnsNoPoints() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsAddress() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            log = log,
            uriQuote = uriQuote,
        )
        val match = "https://maps.google.com/?q=bad-request"
        assertEquals(
            ParseResult(),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test
    fun parse_whenApiReturns404_returnsNoPoints() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsAddress() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            log = log,
            uriQuote = uriQuote,
        )
        val match = "https://maps.google.com/?q=not-found"
        assertEquals(
            ParseResult(),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test(expected = ResponseNetworkException::class)
    fun parse_whenApiReturnsOther4xx_throwsException() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsAddress() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            log = log,
            uriQuote = uriQuote,
        )
        val match = "https://maps.google.com/?q=405"
        input.fetch(match) { data -> input.parse(data, match) }
    }

    @Test(expected = SocketTimeoutNetworkException::class)
    fun parse_whenApiThrowsKnownException_throwsNetworkException() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsAddress() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            log = log,
            uriQuote = uriQuote,
        )
        val match = "https://maps.google.com/?q=exception"
        input.fetch(match) { data -> input.parse(data, match) }
    }

    @Test(expected = UnknownNetworkException::class)
    fun parse_whenApiThrowsUnknownException_throwsUnknownNetworkException() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelectedGoogleMapsAddress() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            log = log,
            uriQuote = uriQuote,
        )
        val match = "https://maps.google.com/?q=unknown-exception"
        input.fetch(match) { data -> input.parse(data, match) }
    }
}
