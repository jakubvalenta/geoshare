package page.ooooo.geoshare.data

import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.lib.inputs.AmapShortLinkInput
import page.ooooo.geoshare.lib.inputs.AmapUriInput
import page.ooooo.geoshare.lib.inputs.AppleMapsUriInput
import page.ooooo.geoshare.lib.inputs.BaiduMapShortLinkInput
import page.ooooo.geoshare.lib.inputs.BaiduMapUriInput
import page.ooooo.geoshare.lib.inputs.CartesIGNUriInput
import page.ooooo.geoshare.lib.inputs.CoordinateInput
import page.ooooo.geoshare.lib.inputs.DebugInput
import page.ooooo.geoshare.lib.inputs.GeoUriInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsShortLinkInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsUriInput
import page.ooooo.geoshare.lib.inputs.HereWeGoUriInput
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.inputs.MagicEarthUriInput
import page.ooooo.geoshare.lib.inputs.MapsMeUriInput
import page.ooooo.geoshare.lib.inputs.MapyComShortLinkInput
import page.ooooo.geoshare.lib.inputs.MapyComUriInput
import page.ooooo.geoshare.lib.inputs.OpenStreetMapUriInput
import page.ooooo.geoshare.lib.inputs.OsmAndUriInput
import page.ooooo.geoshare.lib.inputs.PlusCodeInput
import page.ooooo.geoshare.lib.inputs.UrbiUriInput
import page.ooooo.geoshare.lib.inputs.WazeUriInput
import page.ooooo.geoshare.lib.inputs.YandexMapsShortLinkInput
import page.ooooo.geoshare.lib.inputs.YandexMapsUriInput
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
    val all = listOf<Input<*>>(
        GeoUriInput,
        PlusCodeInput,
        GoogleMapsShortLinkInput,
        GoogleMapsUriInput,
        AppleMapsUriInput,
        AmapShortLinkInput,
        AmapUriInput,
        BaiduMapShortLinkInput,
        BaiduMapUriInput,
        CartesIGNUriInput,
        HereWeGoUriInput,
        MagicEarthUriInput,
        MapsMeUriInput,
        MapyComShortLinkInput,
        MapyComUriInput,
        OpenStreetMapUriInput,
        OsmAndUriInput,
        UrbiUriInput,
        WazeUriInput,
        YandexMapsShortLinkInput,
        YandexMapsUriInput,
        CoordinateInput,
    ).run {
        if (BuildConfig.DEBUG) {
            this + DebugInput
        } else {
            this
        }
    }
}
