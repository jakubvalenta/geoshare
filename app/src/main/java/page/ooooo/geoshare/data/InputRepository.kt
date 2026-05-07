package page.ooooo.geoshare.data

import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.lib.inputs.DebugInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsShortLinkInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsUriInput
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InputRepository @Inject constructor() {
    /**
     * All [page.ooooo.geoshare.lib.inputs.Input] objects.
     *
     * Order matters, because [page.ooooo.geoshare.lib.conversion.ConversionState] will try the inputs in order when
     * parsing a URI.
     */
    val all = listOf(
        // TODO
        // GeoUriInput,
        // PlusCodeInput,
        GoogleMapsShortLinkInput,
        GoogleMapsUriInput,
        // AppleMapsInput,
        // AmapInput,
        // BaiduMapInput,
        // CartesIGNInput,
        // HereWeGoInput,
        // MagicEarthInput,
        // MapyComInput,
        // OpenStreetMapInput,
        // MapsMeInput,
        // OsmAndInput,
        // UrbiInput,
        // WazeInput,
        // YandexMapsInput,
        // CoordinatesInput,
    ).run {
        if (BuildConfig.DEBUG) {
            this + DebugInput
        } else {
            this
        }
    }
}
