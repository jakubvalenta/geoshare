package page.ooooo.geoshare.lib.position

import androidx.compose.runtime.Immutable

@Immutable
data class LatLonZName(val lat: Double, val lon: Double, val z: Double? = null, val name: String? = null)
