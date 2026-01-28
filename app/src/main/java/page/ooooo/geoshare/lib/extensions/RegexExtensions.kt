package page.ooooo.geoshare.lib.extensions

import com.google.re2j.Matcher
import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.position.LatLonZName

fun Matcher.groupOrNull(): String? = try {
    this.group()
} catch (_: IllegalArgumentException) {
    null
}

fun Matcher.groupOrNull(name: String): String? = try {
    this.group(name)
} catch (_: IllegalArgumentException) {
    null
}

infix fun Pattern.find(input: String?): Matcher? = input?.let { input ->
    this.matcher(input).takeIf { it.find() }
}

infix fun Pattern.findLatLonZName(input: String?): LatLonZName? = input?.let { input ->
    (this find input)?.let { m ->
        m.toLatLon()?.let { (lat, lon) ->
            LatLonZName(lat, lon, m.toZ(), m.toName())
        }
    }
}

infix fun Pattern.findUriString(input: String?): String? = input?.let { input ->
    (this find input)?.toUriString()
}

infix fun Pattern.findAll(input: String?): Sequence<Matcher> = input?.let { input ->
    this.matcher(input).let { m -> generateSequence { m.takeIf { it.find() } } }
}.orEmpty()

infix fun Pattern.findAllLatLonZName(input: String?): Sequence<LatLonZName> = input?.let { input ->
    (this findAll input).mapNotNull { m -> m.toLatLonZName() }
}.orEmpty()

infix fun Pattern.match(input: String?): Matcher? = input?.let { input ->
    this.matcher(input).takeIf { it.matches() }
}

infix fun Pattern.matchLatLonZName(input: String?): LatLonZName? = input?.let { input ->
    (this match input)?.toLatLonZName()
}

infix fun Pattern.matchQ(input: String?): String? = input?.let { input ->
    this.matcher(input).takeIf { it.matches() }?.toQ()
}

infix fun Pattern.matchZ(input: String?): Double? = input?.let { input ->
    this.matcher(input).takeIf { it.matches() }?.toZ()
}

infix fun Pattern.matchHash(input: String?): String? = input?.let { input ->
    this.matcher(input).takeIf { it.matches() }?.toHash()
}

infix fun String.find(input: String?): Matcher? = input?.let { input ->
    Pattern.compile(this) find input
}

infix fun String.findLatLonZName(input: String?): LatLonZName? = input?.let { input ->
    Pattern.compile(this) findLatLonZName input
}

infix fun String.findAll(input: String?): Sequence<Matcher> = input?.let { input ->
    Pattern.compile(this) findAll input
}.orEmpty()

infix fun String.findAllLatLonZName(input: String?): Sequence<LatLonZName> = input?.let { input ->
    Pattern.compile(this) findAllLatLonZName input
}.orEmpty()

infix fun String.match(input: String?): Matcher? = input?.let { input ->
    Pattern.compile(this) match input
}

infix fun String.matchLatLonZName(input: String?): LatLonZName? = input?.let { input ->
    Pattern.compile(this) matchLatLonZName input
}

infix fun String.matchZ(input: String?): Double? = input?.let { input ->
    Pattern.compile(this) matchZ input
}

infix fun String.matchHash(input: String?): String? = input?.let { input ->
    Pattern.compile(this) matchHash input
}

fun Matcher.toLat(): Double? =
    this.groupOrNull("lat")?.toDoubleOrNull()

fun Matcher.toLon(): Double? =
    this.groupOrNull("lon")?.toDoubleOrNull()

fun Matcher.toLatLon(): Pair<Double, Double>? =
    this.toLat()?.let { lat ->
        this.toLon()?.let { lon ->
            lat to lon
        }
    }

fun Matcher.toLatLonZName(): LatLonZName? =
    this.toLatLon()?.let { (lat, lon) ->
        LatLonZName(lat, lon, this.toZ(), this.toName())
    }

fun Matcher.toName(): String? =
    this.groupOrNull("name")

fun Matcher.toQ(): String? =
    this.groupOrNull("q")

fun Matcher.toZ(): Double? =
    this.groupOrNull("z")?.toDoubleOrNull()

fun Matcher.toUriString(): String? =
    this.groupOrNull("url")

fun Matcher.toHash(): String? =
    this.groupOrNull("hash")
