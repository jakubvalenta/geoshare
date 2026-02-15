package page.ooooo.geoshare.lib.point

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

fun ImmutableList<NaivePoint>.asWGS84(): ImmutableList<WGS84Point> =
    this.map { it.asWGS84() }.toImmutableList()

fun ImmutableList<NaivePoint>.asGCJ02(): ImmutableList<GCJ02Point> =
    this.map { it.asGCJ02() }.toImmutableList()

fun ImmutableList<NaivePoint>.asBD09MC(): ImmutableList<BD09MCPoint> =
    this.map { it.asBD09MC() }.toImmutableList()

fun ImmutableList<Point>.toWGS84(): ImmutableList<WGS84Point> =
    this.map { it.toWGS84() }.toImmutableList()
