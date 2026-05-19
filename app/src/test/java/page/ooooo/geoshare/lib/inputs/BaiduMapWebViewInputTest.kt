package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.BD09MCPoint
import page.ooooo.geoshare.lib.geo.Source

class BaiduMapWebViewInputTest : InputTest {
    private val input = FakeInputRepository.baiduMapWebViewInput

    @Test
    fun parse_whenDataIsValidJsonObject_returnsPoint() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    BD09MCPoint(
                        3315902.2199999997, 13502918.375,
                        3.14,
                        name = "黄岩客运中心",
                        source = Source.JAVASCRIPT,
                    )
                )
            ),
            input.parse(
                // language=Json
                """
                    {
                        "lat": 3315902.2199999997,
                        "lon": 13502918.375,
                        "z": 3.14,
                        "name": "黄岩客运中心"
                    }
                """.trimIndent(),
                "https://map.baidu.com/original",
            ),
        )
        assertEquals(
            ParseResult(
                persistentListOf(
                    BD09MCPoint(
                        3315902.2199999997, 13502918.375,
                        source = Source.JAVASCRIPT,
                    )
                )
            ),
            input.parse(
                // language=Json
                """
                    {
                        "lat": 3315902.2199999997,
                        "lon": 13502918.375
                    }
                """.trimIndent(),
                "https://map.baidu.com/original",
            ),
        )
        assertEquals(
            ParseResult(
                persistentListOf(
                    BD09MCPoint(
                        3315902.2199999997,
                        source = Source.JAVASCRIPT,
                    )
                )
            ),
            input.parse(
                // language=Json
                """
                    {
                        "lat": 3315902.2199999997
                    }
                """.trimIndent(),
                "https://map.baidu.com/original",
            ),
        )
        assertEquals(
            ParseResult(
                persistentListOf(
                    BD09MCPoint(source = Source.JAVASCRIPT)
                )
            ),
            input.parse(
                // language=Json
                "{}",
                "https://map.baidu.com/original",
            ),
        )
    }

    @Test
    fun parse_whenDataIsValidJsonButHasUnexpectedPropertyType_returnsEmptyResult() = runTest {
        for (data in listOf(
            // language=Json
            """{"lat":  "spam"}""",
            // language=Json
            """[]""",
            // language=Json
            """"spam"""",
            // language=Json
            "0",
            // language=Json
            "null",
        )) {
            assertEquals(
                ParseResult(),
                input.parse(data, "https://map.baidu.com/original"),
            )
        }
        assertEquals(
            ParseResult(),
            input.parse(
                // language=Json
                """{"lat":  "spam"}""",
                "https://map.baidu.com/original",
            ),
        )
        assertEquals(
            ParseResult(),
            input.parse(
                // language=Json
                "[]",
                "https://map.baidu.com/original",
            ),
        )
    }

    @Test
    fun parse_whenDataIsInvalidJson_returnsEmptyResult() = runTest {
        for (data in listOf(
            "{",
            """{"trailingComma": 0,}""",
            "",
        )) {
            assertEquals(
                ParseResult(),
                input.parse(data, "https://map.baidu.com/original"),
            )
        }
    }
}
