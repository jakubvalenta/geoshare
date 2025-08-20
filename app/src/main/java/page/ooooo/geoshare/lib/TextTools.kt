package page.ooooo.geoshare.lib

fun truncateMiddle(
    s: String,
    maxLength: Int = 60,
    ellipsis: String = "\u2026",
): String = if (s.length > maxLength) {
    val partLength: Int = maxLength / 2
    "${
        s.take(partLength)
    }${ellipsis}${
        s.substring(s.length - partLength)
    }"
} else {
    s
}
