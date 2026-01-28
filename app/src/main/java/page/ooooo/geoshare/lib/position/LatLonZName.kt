package page.ooooo.geoshare.lib.position

import androidx.compose.runtime.Immutable

// TODO Replace LatLonZName with Point
@Immutable
data class LatLonZName(val lat: Double, val lon: Double, val z: Double? = null, val name: String? = null)
