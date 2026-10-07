package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
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
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.network.ContentConvertNetworkException
import page.ooooo.geoshare.lib.network.UnrecoverableNetworkException
import kotlin.time.Clock
import kotlin.time.Instant

class WazeApiInputTest : InputTest {
    override val resources: Resources = mock {
        on { getString(R.string.input_waze_warning_route_empty) } doReturn
            "Route is empty. Try to return to Waze, wait for GPS, and share the route again."
        on { getString(R.string.input_waze_warning_route_expired) } doReturn "Route has expired."
    }
    private val epochMilliseconds = 1791298809703L
    private val clock: Clock = object : Clock {
        override fun now() = Instant.fromEpochMilliseconds(epochMilliseconds)
    }
    private val log = FakeLog

    @Test
    fun parse_whenTokenIsSetUsingParameterSDAndResponseIsValid_returnsPointsAndRemovesDuplicates() = runTest {
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "https://www.waze.com/row-rtserver/web/PickUpGetDriverInfo?token=test-token&getUserInfo=true&_=$epochMilliseconds" ->
                    respond(
                        @Suppress("GrazieInspectionRunner")
                        // language=json
                        """
{
  "temporary": true,
  "mood": 1,
  "lonlatTimestamp": 1791299098349,
  "lon": 13.429745416671135,
  "GPSspeed": 0,
  "onSegmentY": 52.47010050758833,
  "onSegmentX": 13.428169059507576,
  "calculatedLocation": {
    "city": "Berlin",
    "street": "Emser Straße (Neukölln)",
    "latitude": 52.469131999999995,
    "longitude": 13.43647
  },
  "speed": 10.8,
  "frequency": 60000,
  "route": [
    13.429986306480764,
    52.4698464491807,
    13.429986306480764,
    52.4698464491807,
    13.43037930793268,
    52.46911003202121,
    13.43037930793268,
    52.46911003202121,
    13.430744161825805,
    52.468465652750425,
    13.430744161825805,
    52.468465652750425,
    13.432835774413983,
    52.468740485761735,
    13.432835774413983,
    52.468740485761735,
    13.435707865843954,
    52.46909682544837,
    13.435707865843954,
    52.46909682544837,
    13.43645789248042,
    52.46919711515342
  ],
  "eta": 113,
  "onSegmentIndex": 0,
  "routeTimestamp": 1791299098510,
  "GPSazymuth": 0,
  "lat": 52.4702910091521,
  "status": "ok"
}
                        """.trimIndent(),
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )

                else -> throw NotImplementedError()
            }
        }
        val input = WazeApiInput(clock = clock, engine = engine, log = log)
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(52.4702910091521, 13.429745416671135, source = Source.API),
                    WGS84Point(52.4698464491807, 13.429986306480764, source = Source.API),
                    WGS84Point(52.46911003202121, 13.43037930793268, source = Source.API),
                    WGS84Point(52.468465652750425, 13.430744161825805, source = Source.API),
                    WGS84Point(52.468740485761735, 13.432835774413983, source = Source.API),
                    WGS84Point(52.46909682544837, 13.435707865843954, source = Source.API),
                    WGS84Point(52.46919711515342, 13.43645789248042, source = Source.API),
                    WGS84Point(
                        52.469131999999995, 13.43647,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "Emser Straße (Neukölln), Berlin",
                        source = Source.API,
                    ),
                )
            ),
            input.parse("test-token")
        )
    }

    @Test
    fun parse_whenTokenIsSetUsingParameterTokenAndResponseIsValid_returnsPoints() = runTest {
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "https://www.waze.com/row-rtserver/web/PickUpGetDriverInfo?token=test-token&getUserInfo=true&_=$epochMilliseconds" ->
                    respond(
                        // language=json
                        """
{
  "lon": 13.429745416671135,
  "calculatedLocation": {
    "latitude": 52.469131999999995,
    "longitude": 13.43647
  },
  "route": [],
  "lat": 52.4702910091521,
  "status": "ok"
}
                        """.trimIndent(),
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )

                else -> throw NotImplementedError()
            }
        }
        val input = WazeApiInput(clock = clock, engine = engine, log = log)
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(52.4702910091521, 13.429745416671135, source = Source.API),
                    WGS84Point(52.469131999999995, 13.43647, source = Source.API),
                )
            ),
            input.parse("test-token")
        )
    }

    @Test
    fun parse_whenResponseIsInvalidToken_returnsWarning() = runTest {
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "https://www.waze.com/row-rtserver/web/PickUpGetDriverInfo?token=test-token&getUserInfo=true&_=$epochMilliseconds" ->
                    respond(
                        // language=json
                        """
{
  "message":"invalid token test-token",
  "status":"error"
}
                        """.trimIndent(),
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )

                else -> throw NotImplementedError()
            }
        }
        val input = WazeApiInput(clock = clock, engine = engine, log = log)
        assertEquals(
            ParseResult.Warning(resources.getString(R.string.input_waze_warning_route_expired)),
            input.parse("test-token")
        )
    }

    @Test(expected = UnrecoverableNetworkException::class)
    fun parse_whenResponseIs404_throwsException() = runTest {
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "https://www.waze.com/row-rtserver/web/PickUpGetDriverInfo?token=test-token&getUserInfo=true&_=$epochMilliseconds" ->
                    respondError(HttpStatusCode.NotFound)

                else -> throw NotImplementedError()
            }
        }
        val input = WazeApiInput(clock = clock, engine = engine, log = log)
        assertEquals(
            ParseResult.Warning(resources.getString(R.string.input_waze_warning_route_expired)),
            input.parse("test-token")
        )
    }

    @Test
    fun parse_whenResponseIsEmpty_returnsWarning() = runTest {
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "https://www.waze.com/row-rtserver/web/PickUpGetDriverInfo?token=test-token&getUserInfo=true&_=$epochMilliseconds" ->
                    respond(
                        // language=json
                        """{"status": "ok"}""",
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )

                else -> throw NotImplementedError()
            }
        }
        val input = WazeApiInput(clock = clock, engine = engine, log = log)
        assertEquals(
            ParseResult.Warning(resources.getString(R.string.input_waze_warning_route_empty)),
            input.parse("test-token"),
        )
    }

    @Test
    fun parse_whenResponseIsUnknownError_returnsNoPoints() = runTest {
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "https://www.waze.com/row-rtserver/web/PickUpGetDriverInfo?token=test-token&getUserInfo=true&_=$epochMilliseconds" ->
                    respond(
                        // language=json
                        """{"status": "error"}""",
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )

                else -> throw NotImplementedError()
            }
        }
        val input = WazeApiInput(clock = clock, engine = engine, log = log)
        assertEquals(
            ParseResult.Success(),
            input.parse("test-token"),
        )
    }

    @Test(expected = ContentConvertNetworkException::class)
    fun parse_whenResponseIsMissingFields_throwsException() = runTest {
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "https://www.waze.com/row-rtserver/web/PickUpGetDriverInfo?token=test-token&getUserInfo=true&_=$epochMilliseconds" ->
                    respond(
                        // language=json
                        """{"spam": "spam"}""",
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )

                else -> throw NotImplementedError()
            }
        }
        val input = WazeApiInput(clock = clock, engine = engine, log = log)
        input.parse("test-token")
    }
}
