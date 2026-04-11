package page.ooooo.geoshare.lib.extensions

import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Source
import kotlin.text.MatchResult
import kotlin.text.Regex

fun Regex.find(input: CharSequence?): MatchResult? = input?.let { this.find(it) }

fun Regex.findAll(input: CharSequence?): Sequence<MatchResult> = input?.let { this.findAll(it) }.orEmpty()

fun Regex.matchEntire(input: CharSequence?): MatchResult? = input?.let { this.matchEntire(it) }

fun MatchResult.groupOrNull(index: Int = 1): String? = this.groupValues[index].takeIf { it.isNotEmpty() }

fun MatchResult.doubleGroupOrNull(index: Int = 1): Double? = this.groupValues[index].toDoubleOrNull()

fun MatchResult.toLatLonPoint(source: Source): NaivePoint? =
    this.doubleGroupOrNull(1)?.let { lat ->
        this.doubleGroupOrNull(2)?.let { lon ->
            NaivePoint(lat, lon, source = source)
        }
    }

fun MatchResult.toLatLonZPoint(source: Source): NaivePoint? =
    this.doubleGroupOrNull(1)?.let { lat ->
        this.doubleGroupOrNull(2)?.let { lon ->
            NaivePoint(lat, lon, z = this.doubleGroupOrNull(3), source = source)
        }
    }

fun MatchResult.toLatLonNamePoint(source: Source): NaivePoint? =
    this.doubleGroupOrNull(1)?.let { lat ->
        this.doubleGroupOrNull(2)?.let { lon ->
            NaivePoint(lat, lon, name = this.groupOrNull(3), source = source)
        }
    }

fun MatchResult.toZLatLonPoint(source: Source): NaivePoint? =
    this.doubleGroupOrNull(2)?.let { lat ->
        this.doubleGroupOrNull(3)?.let { lon ->
            NaivePoint(lat, lon, z = this.doubleGroupOrNull(1), source = source)
        }
    }

fun MatchResult.toLonLatPoint(source: Source): NaivePoint? =
    this.doubleGroupOrNull(1)?.let { lon ->
        this.doubleGroupOrNull(2)?.let { lat ->
            NaivePoint(lat, lon, source = source)
        }
    }

fun MatchResult.toLonLatZPoint(source: Source): NaivePoint? =
    this.doubleGroupOrNull(1)?.let { lon ->
        this.doubleGroupOrNull(2)?.let { lat ->
            NaivePoint(lat, lon, this.doubleGroupOrNull(3), source = source)
        }
    }

fun MatchResult.toLonLatNamePoint(source: Source): NaivePoint? =
    this.doubleGroupOrNull(1)?.let { lon ->
        this.doubleGroupOrNull(2)?.let { lat ->
            NaivePoint(lat, lon, name = this.groupOrNull(3), source = source)
        }
    }
