package page.ooooo.geoshare.lib.extensions

import kotlin.text.MatchResult
import kotlin.text.Regex

import page.ooooo.geoshare.lib.point.NaivePoint

infix fun Regex.find(input: String?): MatchResult? = input?.let { this.find(it) }

infix fun Regex.findAll(input: String?): Sequence<MatchResult> = input?.let { this.findAll(it) }.orEmpty()

infix fun Regex.match(input: String?): MatchResult? = input?.let { this.matchEntire(it) }

fun MatchResult.groupOrNull(index: Int = 1): String? = this.groupValues[index].takeIf { it.isNotEmpty() }

fun MatchResult.doubleGroupOrNull(index: Int = 1): Double? = this.groupValues[index].toDoubleOrNull()

fun MatchResult.toLatLonPoint(): NaivePoint? =
    this.doubleGroupOrNull(1)?.let { lat ->
        this.doubleGroupOrNull(2)?.let { lon ->
            NaivePoint(lat, lon)
        }
    }

fun MatchResult.toLatLonZPoint(): NaivePoint? =
    this.doubleGroupOrNull(1)?.let { lat ->
        this.doubleGroupOrNull(2)?.let { lon ->
            NaivePoint(lat, lon, z = this.doubleGroupOrNull(3))
        }
    }

fun MatchResult.toLatLonNamePoint(): NaivePoint? =
    this.doubleGroupOrNull(1)?.let { lat ->
        this.doubleGroupOrNull(2)?.let { lon ->
            NaivePoint(lat, lon, name = this.groupOrNull(3))
        }
    }

fun MatchResult.toZLatLonPoint(): NaivePoint? =
    this.doubleGroupOrNull(2)?.let { lat ->
        this.doubleGroupOrNull(3)?.let { lon ->
            NaivePoint(lat, lon, z = this.doubleGroupOrNull(1))
        }
    }

fun MatchResult.toLonLatPoint(): NaivePoint? =
    this.doubleGroupOrNull(1)?.let { lon ->
        this.doubleGroupOrNull(2)?.let { lat ->
            NaivePoint(lat, lon)
        }
    }

fun MatchResult.toLonLatZPoint(): NaivePoint? =
    this.doubleGroupOrNull(1)?.let { lon ->
        this.doubleGroupOrNull(2)?.let { lat ->
            NaivePoint(lat, lon, this.doubleGroupOrNull(3))
        }
    }

fun MatchResult.toLonLatNamePoint(): NaivePoint? =
    this.doubleGroupOrNull(1)?.let { lon ->
        this.doubleGroupOrNull(2)?.let { lat ->
            NaivePoint(lat, lon, name = this.groupOrNull(3))
        }
    }

fun MatchResult.toZLonLatPoint(): NaivePoint? =
    this.doubleGroupOrNull(2)?.let { lon ->
        this.doubleGroupOrNull(3)?.let { lat ->
            NaivePoint(lat, lon, z = this.doubleGroupOrNull(1))
        }
    }
