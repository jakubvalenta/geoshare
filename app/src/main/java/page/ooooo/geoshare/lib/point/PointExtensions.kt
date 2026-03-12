package page.ooooo.geoshare.lib.point

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

fun ImmutableList<Point>.toWGS84(): ImmutableList<WGS84Point> =
    this.map { it.toWGS84() }.toImmutableList()
