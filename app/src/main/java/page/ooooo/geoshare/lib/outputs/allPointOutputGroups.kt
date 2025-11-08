package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.position.Point

val allPointOutputGroups: List<OutputGroup<Point>> = listOf(
    CoordinatesPointOutputGroup,
    GeoUriPointOutputGroup,
    GoogleMapsPointOutputGroup,
    AppleMapsPointOutputGroup,
    MagicEarthPointOutputGroup,
)
