package page.ooooo.geoshare.lib

typealias SegmentedString = List<Segment>

sealed interface Segment {
    data class Text(val text: String) : Segment
    data class Arg(val index: Int) : Segment
}

/**
 * Parses a format string such as `"Foo %1$s bar %2$s baz"` into a [SegmentedString] such as
 * `listOf(Segment.Text("Foo"), Segment.Arg(1), Segment.Text("bar"), Segment.Arg(2), Segment.Text("baz")`.
 */
@Suppress("RedundantInterpolationPrefix")
fun parseFormatString(input: String): SegmentedString =
    Regex($$"""([^%]+)|%(\d+)\$s""")
        .findAll(input)
        .mapNotNull { m ->
            // If the first regex group matches, the match is a text
            m.groupValues[1].takeIf { it.isNotEmpty() }?.let { Segment.Text(it) }
            // If the second regex group matches, the match is an argument
                ?: m.groupValues[2].toIntOrNull()?.let { Segment.Arg(it - 1) }
        }
        .toList()
