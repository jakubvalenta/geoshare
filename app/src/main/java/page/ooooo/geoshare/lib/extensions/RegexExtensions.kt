package page.ooooo.geoshare.lib.extensions

import kotlin.text.MatchResult
import kotlin.text.Regex

import page.ooooo.geoshare.lib.point.NaivePoint

fun MatchResult.groupOrNull(): String? =
    this.groups.firstOrNull()?.value

fun MatchResult.groupOrNull(name: String): String? =
    (this.groups as? MatchNamedGroupCollection)?.get(name)?.value

infix fun Regex.find(input: String?): MatchResult? = input?.let { input ->
    this.find(input)
}

infix fun Regex.findPoint(input: String?): NaivePoint? = input?.let { input ->
    (this find input)?.let { m ->
        m.toLatLon()?.let { (lat, lon) ->
            NaivePoint(lat, lon, m.toZ(), m.toName())
        }
    }
}

infix fun Regex.findAll(input: String?): Sequence<MatchResult> = input?.let { input ->
    this.findAll(input)
}.orEmpty()

infix fun Regex.findAllPoints(input: String?): Sequence<NaivePoint> = input?.let { input ->
    (this findAll input).mapNotNull { m -> m.toPoint() }
}.orEmpty()

infix fun Regex.match(input: String?): MatchResult? = input?.let { input ->
    this.matchEntire(input)
}

infix fun Regex.matchPoint(input: String?): NaivePoint? = input?.let { input ->
    (this match input)?.toPoint()
}

infix fun Regex.matchName(input: String?): String? = input?.let { input ->
    this.matchEntire(input)?.toName()
}

infix fun Regex.matchZ(input: String?): Double? = input?.let { input ->
    this.matchEntire(input)?.toZ()
}

infix fun Regex.matchHash(input: String?): String? = input?.let { input ->
    this.matchEntire(input)?.toHash()
}

infix fun String.find(input: String?): MatchResult? = input?.let { input ->
    Regex(this) find input
}

infix fun String.findPoint(input: String?): NaivePoint? = input?.let { input ->
    Regex(this) findPoint input
}

infix fun String.findAll(input: String?): Sequence<MatchResult> = input?.let { input ->
    Regex(this) findAll input
}.orEmpty()

infix fun String.findAllPoints(input: String?): Sequence<NaivePoint> = input?.let { input ->
    Regex(this) findAllPoints input
}.orEmpty()

infix fun String.match(input: String?): MatchResult? = input?.let { input ->
    Regex(this) match input
}

infix fun String.matchPoint(input: String?): NaivePoint? = input?.let { input ->
    Regex(this) matchPoint input
}

infix fun String.matchName(input: String?): String? = input?.let { input ->
    Regex(this) matchName input
}

infix fun String.matchZ(input: String?): Double? = input?.let { input ->
    Regex(this) matchZ input
}

infix fun String.matchHash(input: String?): String? = input?.let { input ->
    Regex(this) matchHash input
}

fun MatchResult.toLat(): Double? =
    this.groupOrNull("lat")?.toDoubleOrNull()

fun MatchResult.toLon(): Double? =
    this.groupOrNull("lon")?.toDoubleOrNull()

fun MatchResult.toLatLon(): Pair<Double, Double>? =
    this.toLat()?.let { lat ->
        this.toLon()?.let { lon ->
            lat to lon
        }
    }

fun MatchResult.toPoint(): NaivePoint? =
    this.toLatLon()?.let { (lat, lon) ->
        NaivePoint(lat, lon, this.toZ(), this.toName())
    }

fun MatchResult.toName(): String? =
    this.groupOrNull("name")

fun MatchResult.toZ(): Double? =
    this.groupOrNull("z")?.toDoubleOrNull()

fun MatchResult.toHash(): String? =
    this.groupOrNull("hash")
