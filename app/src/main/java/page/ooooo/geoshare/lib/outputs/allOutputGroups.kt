package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.position.Position

val allOutputGroups: List<OutputGroup<Position>> = listOf(
    CoordinatesOutputGroup,
    GeoUriOutputGroup,
    GoogleMapsOutputGroup,
    AppleMapsOutputGroup,
    MagicEarthOutputGroup,
    GpxOutputGroup,
)
