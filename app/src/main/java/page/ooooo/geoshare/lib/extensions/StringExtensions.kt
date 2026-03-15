package page.ooooo.geoshare.lib.extensions

fun String.truncateMiddle(maxLength: Int = 60, ellipsis: String = "\u2026"): String =
    if (this.length > maxLength) {
        val partLength: Int = maxLength / 2
        "${this.take(partLength)}${ellipsis}${this.substring(this.length - partLength)}"
    } else {
        this
    }
