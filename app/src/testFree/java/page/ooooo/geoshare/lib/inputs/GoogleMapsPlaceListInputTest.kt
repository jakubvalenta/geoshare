package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source

class GoogleMapsPlaceListInputTest : InputTest {
    private val input = GoogleMapsPlaceListInputImpl(
        log = FakeLog,
    )

    @Test
    fun parse_whenDataIsValidJsonObject_returnsPoints() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(59.1293656, 11.4585672, source = Source.JAVASCRIPT),
                    GCJ02MainlandChinaPoint(59.4154007, 11.659710599999999, source = Source.JAVASCRIPT),
                    GCJ02MainlandChinaPoint(59.3443991, 11.672637, source = Source.JAVASCRIPT),
                )
            ),
            input.parse(
                // language=Json
                """
                    [
                        {"lat": 59.1293656, "lon": 11.4585672},
                        {"lat": 59.4154007, "lon": 11.659710599999999},
                        {"lat": 59.3443991, "lon": 11.672637}
                    ]
                """.trimIndent(),
                "https://maps.google.com/original",
            ),
        )
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(59.1293656, source = Source.JAVASCRIPT),
                )
            ),
            input.parse(
                // language=Json
                """
                    [
                        {"lat": 59.1293656}
                    ]
                """.trimIndent(),
                "https://maps.google.com/original",
            ),
        )
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(source = Source.JAVASCRIPT),
                )
            ),
            input.parse(
                // language=Json
                "[{}]",
                "https://maps.google.com/original",
            ),
        )
        assertEquals(
            ParseResult(),
            input.parse(
                // language=Json
                "[]",
                "https://maps.google.com/original",
            ),
        )
    }

    @Test
    fun parse_whenDataIsValidJsonButHasUnexpectedPropertyType_returnsNoPoints() = runTest {
        for (data in listOf(
            // language=Json
            """["spam"]""",
            // language=Json
            "{}",
            // language=Json
            """"spam"""",
            // language=Json
            "0",
            // language=Json
            "null",
        )) {
            assertEquals(
                ParseResult(),
                input.parse(data, "https://maps.google.com/original"),
            )
        }
        assertEquals(
            ParseResult(),
            input.parse(
                // language=Json
                """{"lat":  "spam"}""",
                "https://maps.google.com/original",
            ),
        )
        assertEquals(
            ParseResult(),
            input.parse(
                // language=Json
                "[]",
                "https://maps.google.com/original",
            ),
        )
    }

    @Test
    fun parse_whenDataIsInvalidJson_returnsNoPoints() = runTest {
        for (data in listOf(
            "{",
            """{"trailingComma": 0,}""",
            "",
        )) {
            assertEquals(
                ParseResult(),
                input.parse(data, "https://maps.google.com/original"),
            )
        }
    }
}
