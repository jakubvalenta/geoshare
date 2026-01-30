package page.ooooo.geoshare.lib.point

import io.ktor.util.escapeHTML
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.inputs.ParseHtmlResult
import page.ooooo.geoshare.lib.inputs.ParseUriResult

fun ImmutableList<Point>.getOrNull(i: Int?): Point? =
    if (i == null) {
        this.lastOrNull()
    } else {
        try {
            this[i]
        } catch (_: IndexOutOfBoundsException) {
            null
        }
    }

fun ImmutableList<Point>.toParseUriResult(htmlUriString: String? = null): ParseUriResult? =
    if (this.lastOrNull()?.hasCoordinates() == true) {
        ParseUriResult.Succeeded(this)
    } else if (htmlUriString != null) {
        ParseUriResult.SucceededAndSupportsHtmlParsing(this, htmlUriString)
    } else if (this.lastOrNull()?.hasName() == true) {
        ParseUriResult.Succeeded(this)
    } else {
        null
    }

fun ImmutableList<Point>.toParseHtmlResult(redirectUriString: String? = null): ParseHtmlResult? =
    if (this.lastOrNull()?.hasCoordinates() == true) {
        ParseHtmlResult.Succeeded(this)
    } else if (redirectUriString != null) {
        ParseHtmlResult.RequiresRedirect(redirectUriString)
    } else if (this.lastOrNull()?.hasName() == true) {
        ParseHtmlResult.Succeeded(this)
    } else {
        null
    }

fun ImmutableList<NaivePoint>.asWGS84(): ImmutableList<WGS84Point> =
    this.map { it.asWGS84() }.toImmutableList()

fun ImmutableList<NaivePoint>.asGCJ02(): ImmutableList<GCJ02Point> =
    this.map { it.asGCJ02() }.toImmutableList()

fun ImmutableList<NaivePoint>.asBD09MC(): ImmutableList<BD09MCPoint> =
    this.map { it.asBD09MC() }.toImmutableList()

fun ImmutableList<Point>.toWGS84(): ImmutableList<WGS84Point> =
    this.map { it.toWGS84() }.toImmutableList()

fun List<WGS84Point>.writeGpxPoints(writer: Appendable) = writeGpx(writer) {
    this@writeGpxPoints.filter { it.lat != null && it.lon != null }.forEach { point ->
        point.toWGS84().run {
            append("<wpt lat=\"$latStr\" lon=\"$lonStr\"")
        }
        if (point.name != null) {
            append(">\n")
            append("    <name>${point.name.escapeHTML()}</name>\n")
            append("</wpt>\n")
        } else {
            append(" />\n")
        }
    }
}

fun List<WGS84Point>.writeGpxRoute(writer: Appendable) = writeGpx(writer) {
    append("<rte>\n")
    this@writeGpxRoute.filter { it.lat != null && it.lon != null }.forEach { point ->
        point.toWGS84().run {
            @Suppress("SpellCheckingInspection")
            append("<rtept lat=\"$latStr\" lon=\"$lonStr\"")
        }
        if (point.name != null) {
            append(">\n")
            append("    <name>${point.name.escapeHTML()}</name>\n")
            @Suppress("SpellCheckingInspection")
            append("</rtept>\n")
        } else {
            append(" />\n")
        }
    }
    append("</rte>\n")
}

private fun writeGpx(writer: Appendable, block: Appendable.() -> Unit) = writer.apply {
    append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n")
    append("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\"\n")
    append("     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n")
    append("     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n")
    writer.block()
    append("</gpx>\n")
}
