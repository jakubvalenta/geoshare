package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.*

val allOutputGroups: List<OutputGroup<Position>> = listOf(
    CoordinatesOutputGroup,
    GeoUriOutputGroup,
    GoogleMapsOutputGroup,
    AppleMapsOutputGroup,
    MagicEarthOutputGroup,
    GpxOutputGroup,
)
