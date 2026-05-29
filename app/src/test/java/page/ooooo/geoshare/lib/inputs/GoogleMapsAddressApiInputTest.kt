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
import page.ooooo.geoshare.data.di.FakeGeoShareServer
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.di.FakeKeyStoreService
import page.ooooo.geoshare.data.di.FakeServerRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.ServerHttpClientFactory
import page.ooooo.geoshare.lib.network.SocketTimeoutNetworkException
import page.ooooo.geoshare.lib.network.UnknownNetworkException
import java.net.SocketTimeoutException

class GoogleMapsAddressApiInputTest {
    private val server = FakeGeoShareServer
    private val query = "Cherbourg, France"
    private val engine = MockEngine { request ->
        when (request.url.toString()) {
            "${server.baseUrl}/v4/geocode/address/Cherbourg,%20France" -> respond(
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

            "${server.baseUrl}/v4/geocode/address/nothing" -> respond(
                // language=Json
                """
                    {
                        "results": []
                    }
                """.trimIndent(),
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )

            "${server.baseUrl}/v4/geocode/address/invalid" -> respond(
                // language=Json
                """
                    {
                        "results": "invalid"
                    }
                """.trimIndent(),
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )

            "${server.baseUrl}/v4/geocode/address/exception" -> throw SocketTimeoutException()

            "${server.baseUrl}/v4/geocode/address/not-found" -> respondError(HttpStatusCode.NotFound)

            else -> throw NotImplementedError()
        }
    }
    private val keyStoreService = FakeKeyStoreService()
    private val log = FakeLog
    private val uriQuote = FakeUriQuote
    private val userPreferencesRepository = FakeUserPreferencesRepository()

    @Test
    fun parse_whenApiIsNotConfigured_returnsNextStep() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelected() } doReturn null
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        val match = "https://maps.google.com/?q=$query"
        assertEquals(
            ParseResult(
                nextStep = NextStep(FakeInputRepository.googleMapsHtmlInput, match),
            ),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test
    fun parse_whenQueryIsInQueryParamAndApiReturnsResults_returnsHighestRankedPoint() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelected() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
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
    fun parse_whenQueryIsInPathAndApiReturnsResults_returnsHighestRankedPoint() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelected() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
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
            on { getSelected() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository),
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
            on { getSelected() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        val match = "https://www.google.com/?q="
        assertEquals(
            ParseResult(),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test
    fun parse_whenApiReturnsNoResults_returnsNoPoints() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelected() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        val match = "https://maps.google.com/?q=nothing"
        assertEquals(
            ParseResult(),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test(expected = UnknownNetworkException::class)
    fun parse_whenApiReturnsInvalidResponse_throwsException() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelected() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        val match = "https://maps.google.com/?q=invalid"
        input.fetch(match) { data -> input.parse(data, match) }
    }

    @Test(expected = SocketTimeoutNetworkException::class)
    fun parse_whenApiThrowsException_throwsException() = runTest {
        val serverRepository: FakeServerRepository = mock {
            on { getSelected() } doReturn server
        }
        val input = GoogleMapsAddressApiInput(
            serverRepository = serverRepository,
            serverHttpClientFactory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            uriQuote = uriQuote,
        )
        val match = "https://maps.google.com/?q=exception"
        input.fetch(match) { data -> input.parse(data, match) }
    }
}
