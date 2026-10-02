package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.BD09MCPoint
import page.ooooo.geoshare.lib.geo.Source

class BaiduMapWebViewInputTest : InputTest {
    override val resources: Resources = mock()
    private val input = FakeInputRepository.baiduMapWebViewInput

    @Test
    fun parse_whenDataIsValidJsonObject_returnsPoint() = runTest {
        assertEquals(
            ParseResult.Success(
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
                // language=json
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
            ParseResult.Success(
                persistentListOf(
                    BD09MCPoint(
                        3315902.2199999997, 13502918.375,
                        source = Source.JAVASCRIPT,
                    )
                )
            ),
            input.parse(
                // language=json
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
            ParseResult.Success(
                persistentListOf(
                    BD09MCPoint(
                        3315902.2199999997,
                        source = Source.JAVASCRIPT,
                    )
                )
            ),
            input.parse(
                // language=json
                """
                    {
                        "lat": 3315902.2199999997
                    }
                """.trimIndent(),
                "https://map.baidu.com/original",
            ),
        )
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    BD09MCPoint(source = Source.JAVASCRIPT)
                )
            ),
            input.parse(
                // language=json
                "{}",
                "https://map.baidu.com/original",
            ),
        )
    }

    @Test
    fun parse_whenDataIsValidJsonButHasUnexpectedPropertyType_returnsNoPoints() = runTest {
        for (data in listOf(
            // language=json
            """{"lat":  "spam"}""",
            // language=json
            """[]""",
            // language=json
            """"spam"""",
            // language=json
            "0",
            // language=json
            "null",
        )) {
            assertEquals(
                ParseResult.Success(),
                input.parse(data, "https://map.baidu.com/original"),
            )
        }
        assertEquals(
            ParseResult.Success(),
            input.parse(
                // language=json
                """{"lat":  "spam"}""",
                "https://map.baidu.com/original",
            ),
        )
        assertEquals(
            ParseResult.Success(),
            input.parse(
                // language=json
                "[]",
                "https://map.baidu.com/original",
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
                ParseResult.Success(),
                input.parse(data, "https://map.baidu.com/original"),
            )
        }
    }
}
