package page.ooooo.geoshare.data

import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.lib.inputs.AmapInput
import page.ooooo.geoshare.lib.inputs.AppleMapsInput
import page.ooooo.geoshare.lib.inputs.BaiduMapInput
import page.ooooo.geoshare.lib.inputs.CartesIGNInput
import page.ooooo.geoshare.lib.inputs.CoordinatesInput
import page.ooooo.geoshare.lib.inputs.DebugInput
import page.ooooo.geoshare.lib.inputs.GeoUriInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsInput
import page.ooooo.geoshare.lib.inputs.HereWeGoInput
import page.ooooo.geoshare.lib.inputs.MagicEarthInput
import page.ooooo.geoshare.lib.inputs.MapsMeInput
import page.ooooo.geoshare.lib.inputs.MapyComInput
import page.ooooo.geoshare.lib.inputs.PlusCodeInput
import page.ooooo.geoshare.lib.inputs.OpenStreetMapInput
import page.ooooo.geoshare.lib.inputs.OsmAndInput
import page.ooooo.geoshare.lib.inputs.UrbiInput
import page.ooooo.geoshare.lib.inputs.WazeInput
import page.ooooo.geoshare.lib.inputs.YandexMapsInput
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.plus

@Singleton
class InputRepository @Inject constructor() {
    /**
     * All [page.ooooo.geoshare.lib.inputs.Input] objects.
     *
     * Order matters, because [page.ooooo.geoshare.lib.conversion.ConversionState] will try the inputs in order when
     * parsing a URI.
     */
    val all = listOf(
        GeoUriInput,
        PlusCodeInput,
        GoogleMapsInput,
        AppleMapsInput,
        AmapInput,
        BaiduMapInput,
        CartesIGNInput,
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
}
