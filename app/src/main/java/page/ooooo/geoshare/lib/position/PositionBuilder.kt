package page.ooooo.geoshare.lib.position

import com.google.re2j.Matcher
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.extensions.groupOrNull
import java.net.URL
import kotlin.math.max
import kotlin.math.min
import kotlin.sequences.mapNotNull

class PositionBuilder(val srs: Srs) {
    var points: MutableList<Point> = mutableListOf()
    var defaultPoint: Point? = null
    var q: String? = null
    var z: Double? = null
    var url: URL? = null

    val position: Position
        get() = Position(
            points.takeIf { it.isNotEmpty() }?.toImmutableList()
                ?: defaultPoint?.let { persistentListOf(it) },
            q = q,
            z = z,
        )

    fun setPointFromMatcher(block: () -> Matcher?) {
        if (points.isEmpty()) {
            block()?.toPoint(srs)?.let {
                points.add(it)
            }
        }
    }

    fun setPointAndZoomFromMatcher(block: () -> Matcher?) {
        if (points.isEmpty()) {
            block()?.toPointAndZ(srs)?.let { (point, newZ) ->
                points.add(point)
                z = newZ
            }
        }
    }

    fun setLatLonZoom(block: () -> Triple<Double, Double, Double>?) {
        if (points.isEmpty()) {
            block()?.let { (lat, lon, newZ) ->
                points.add(Point(srs, lat, lon))
                z = newZ
            }
        }
    }

    fun addPointsFromSequenceOfMatchers(block: () -> Sequence<Matcher>) {
        points.addAll(block().mapNotNull { m -> m.toPoint(srs) })
    }

    fun setDefaultPointFromMatcher(block: () -> Matcher?) {
        if (defaultPoint == null) {
            block()?.toPoint(srs)?.let {
                defaultPoint = it
            }
        }
    }

    fun setQueryFromMatcher(block: () -> Matcher?) {
        if (q == null) {
            q = block()?.toQ()
        }
    }

    fun setZoomFromMatcher(block: () -> Matcher?) {
        if (z == null) {
            z = block()?.toZ()
        }
    }

    fun setUrl(block: () -> URL?) {
        if (url == null) {
            url = block()
        }
    }

    fun setUrlFromMatcher(block: () -> Matcher?) {
        if (url == null) {
            block()?.toUrl()?.let {
                url = it
            }
        }
    }
}

fun Matcher.toPoint(srs: Srs): Point? =
    this.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
        this.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
            Point(srs, lat, lon)
        }
    }

fun Matcher.toPointAndZ(srs: Srs): Pair<Point, Double>? =
    this.toPoint(srs)?.let { point ->
        this.toZ()?.let { z ->
            point to z
        }
    }

fun Matcher.toQ(): String? =
    this.groupOrNull("q")

fun Matcher.toZ(): Double? =
    this.groupOrNull("z")?.toDoubleOrNull()?.let { z ->
        max(1.0, min(21.0, z))
    }

fun Matcher.toUrl(): URL? =
    this.groupOrNull("url")?.let { URL(it) }
