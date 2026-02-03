package page.ooooo.geoshare.lib.inputs

import page.ooooo.geoshare.BuildConfig

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
    CoordinatesInput,
).run {
    if (BuildConfig.DEBUG) {
        this + DebugInput
    } else {
        this
    }
}
