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
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.ApiConfig
import page.ooooo.geoshare.data.local.preferences.GoogleMapsApiPreference
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.ApiService
import page.ooooo.geoshare.lib.network.SocketTimeoutNetworkException
import page.ooooo.geoshare.lib.network.UnknownNetworkException
import java.net.SocketTimeoutException

class GoogleMapsPlaceApiInputTest {
    private val baseUrl = "https://geocode.example.com"
    private val placeId = @Suppress("SpellCheckingInspection") "ChIJKxjxuaNqkFQR3CK6O1HNNqY"
    private val engine = MockEngine { request ->
        when (request.url.toString()) {
            "$baseUrl/v1/google-maps/geocode/place/$placeId" -> respond(
                // language=Json
                """
                    {"location": {"latitude": 50.123456, "longitude": -11.123456}}
                """.trimIndent(),
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )

            "$baseUrl/v1/google-maps/geocode/place/invalid" -> respond(
                // language=Json
                """
                    {"location": "invalid"}
                """.trimIndent(),
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )

            "$baseUrl/v1/google-maps/geocode/place/exception" -> throw SocketTimeoutException()

            "$baseUrl/v1/google-maps/geocode/place/not-found" -> respondError(HttpStatusCode.NotFound)

            else -> throw NotImplementedError()
        }
    }
    private val log = FakeLog
    private val uriQuote = FakeUriQuote

    @Test
    fun parse_whenApiIsNotConfigured_returnsNextStep() = runTest {
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(GoogleMapsApiPreference) } doReturn null
        }
        val input = GoogleMapsPlaceApiInput(
            apiService = ApiService(engine, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            userPreferencesRepository = userPreferencesRepository,
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
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(GoogleMapsApiPreference) } doReturn ApiConfig.WithAttestationAuth(baseUrl)
        }
        val input = GoogleMapsPlaceApiInput(
            apiService = ApiService(engine, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            userPreferencesRepository = userPreferencesRepository,
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
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(GoogleMapsApiPreference) } doReturn ApiConfig.WithAttestationAuth(baseUrl)
        }
        val input = GoogleMapsPlaceApiInput(
            apiService = ApiService(engine, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            userPreferencesRepository = userPreferencesRepository,
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
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(GoogleMapsApiPreference) } doReturn ApiConfig.WithAttestationAuth(baseUrl)
        }
        val input = GoogleMapsPlaceApiInput(
            apiService = ApiService(engine, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            userPreferencesRepository = userPreferencesRepository,
            uriQuote = uriQuote,
        )
        val match = "https://www.google.com/maps/search/?query_place_id="
        assertEquals(
            ParseResult(),
            input.fetch(match) { data -> input.parse(data, match) },
        )
    }

    @Test(expected = UnknownNetworkException::class)
    fun parse_whenApiReturnsInvalidResponse_throwsException() = runTest {
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(GoogleMapsApiPreference) } doReturn ApiConfig.WithAttestationAuth(baseUrl)
        }
        val input = GoogleMapsPlaceApiInput(
            apiService = ApiService(engine, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            userPreferencesRepository = userPreferencesRepository,
            uriQuote = uriQuote,
        )
        val match = "https://www.google.com/maps/search/?query_place_id=invalid"
        input.fetch(match) { data -> input.parse(data, match) }
    }

    @Test(expected = SocketTimeoutNetworkException::class)
    fun parse_whenApiThrowsException_throwsException() = runTest {
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(GoogleMapsApiPreference) } doReturn ApiConfig.WithAttestationAuth(baseUrl)
        }
        val input = GoogleMapsPlaceApiInput(
            apiService = ApiService(engine, log, userPreferencesRepository),
            googleMapsHtmlInput = { FakeInputRepository.googleMapsHtmlInput },
            userPreferencesRepository = userPreferencesRepository,
            uriQuote = uriQuote,
        )
        val match = "https://www.google.com/maps/search/?query_place_id=exception"
        input.fetch(match) { data -> input.parse(data, match) }
    }

}
