package page.ooooo.geoshare.lib.geo

import com.google.common.geometry.S2CellId
import page.ooooo.geoshare.lib.point.NaivePoint

fun decodeS2CellId(id: ULong): NaivePoint =
    S2CellId(id.toLong()).toLatLng().let { s2LatLng ->
        NaivePoint(s2LatLng.latDegrees(), s2LatLng.lngDegrees())
    }
