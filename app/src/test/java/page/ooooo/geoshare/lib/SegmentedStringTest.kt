package page.ooooo.geoshare.lib

import org.junit.Assert.assertEquals
import org.junit.Test

class SegmentedStringTest {
    @Test
    fun parseFormatString_returnsSegmentedString() {
        val input = $$"Kotlin is a %1$s but %3$s programming language designed to %2$s."
        assertEquals(
            listOf(
                Segment.Text("Kotlin is a "),
                Segment.Arg(0),
                Segment.Text(" but "),
                Segment.Arg(2),
                Segment.Text(" programming language designed to "),
                Segment.Arg(1),
                Segment.Text("."),
            ),
            parseFormatString(input)
        )
    }

    @Test
    fun parseFormatString_whenArgumentsAreAtTheStartAndEnd_returnsSegmentedString() {
        val input = $$"%1$s foo %2$s"
        assertEquals(
            listOf(
                Segment.Arg(0),
                Segment.Text(" foo "),
                Segment.Arg(1),
            ),
            parseFormatString(input)
        )
    }
}
