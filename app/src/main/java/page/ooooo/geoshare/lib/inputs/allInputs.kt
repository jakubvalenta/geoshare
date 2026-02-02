package page.ooooo.geoshare.lib.inputs

val allInputs: List<Input> = listOf(
    GeoUriInput,
    GoogleMapsInput,
    AppleMapsInput,
    AmapInput,
    BaiduMapInput,
    HereWeGoInput,
    MagicEarthInput,
    MapyComInput,
    OpenStreetMapInput,
    MapsMeInput,
    OsmAndInput,
    UrbiInput,
    WazeInput,
    YandexMapsInput,
    ExampleInput, // TODO Move to last position when CoordinatesInput is fixed and doesn't match a dot
    CoordinatesInput,
)
