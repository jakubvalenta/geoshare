package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class AppleMapsHtmlInputTest : InputTest {
    private val input = AppleMapsHtmlInput

    @Test
    fun parse_success() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(52.4735927, 13.4050798, source = Source.HTML))),
            @Suppress("SpellCheckingInspection")
            input.parse(
                """<html>
<head>
  <title>Tempelhofer Feld</title>
  <meta property="place:location:latitude" content="52.4735927" />
  <meta property="place:location:longitude" content="13.4050798" />
</head>
<body></body>
</html>
"""
            ),
        )
    }

    @Test
    fun parse_failure() = runTest {
        assertEquals(ParseResult(), input.parse("spam"))
    }
}
