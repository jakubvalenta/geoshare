package page.ooooo.geoshare.data

import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.lib.inputs.AmapShortLinkInput
import page.ooooo.geoshare.lib.inputs.AmapUriInput
import page.ooooo.geoshare.lib.inputs.AppleMapsUriInput
import page.ooooo.geoshare.lib.inputs.BaiduMapShortLinkInput
import page.ooooo.geoshare.lib.inputs.BaiduMapUriInput
import page.ooooo.geoshare.lib.inputs.CartesIGNUriInput
import page.ooooo.geoshare.lib.inputs.CoordinateInput
import page.ooooo.geoshare.lib.inputs.DebugUriInput
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
class InputRepository @Inject constructor(
    amapShortLinkInput: AmapShortLinkInput,
    amapUriInput: AmapUriInput,
    appleMapsUriInput: AppleMapsUriInput,
    baiduMapShortLinkInput: BaiduMapShortLinkInput,
    baiduMapUriInput: BaiduMapUriInput,
    cartesIGNUriInput: CartesIGNUriInput,
    coordinateInput: CoordinateInput,
    debugUriInput: DebugUriInput,
    geoUriInput: GeoUriInput,
    googleMapsShortLinkInput: GoogleMapsShortLinkInput,
    googleMapsUriInput: GoogleMapsUriInput,
    hereWeGoUriInput: HereWeGoUriInput,
    magicEarthUriInput: MagicEarthUriInput,
    mapsMeUriInput: MapsMeUriInput,
    mapyComShortLinkInput: MapyComShortLinkInput,
    mapyComUriInput: MapyComUriInput,
    openStreetMapUriInput: OpenStreetMapUriInput,
    osmAndUriInput: OsmAndUriInput,
    plusCodeInput: PlusCodeInput,
    urbiUriInput: UrbiUriInput,
    wazeUriInput: WazeUriInput,
    yandexMapsShortLinkInput: YandexMapsShortLinkInput,
    yandexMapsUriInput: YandexMapsUriInput,
) {
    /**
     * All [page.ooooo.geoshare.lib.inputs.Input] objects.
     *
     * Order matters, because [page.ooooo.geoshare.lib.conversion.ConversionState] will try the inputs in order when
     * parsing a URI.
     */
    val all = listOf<Input<*>>(
        geoUriInput,
        plusCodeInput,
        googleMapsShortLinkInput,
        googleMapsUriInput,
        appleMapsUriInput,
        amapShortLinkInput,
        amapUriInput,
        baiduMapShortLinkInput,
        baiduMapUriInput,
        cartesIGNUriInput,
        hereWeGoUriInput,
        magicEarthUriInput,
        mapsMeUriInput,
        mapyComShortLinkInput,
        mapyComUriInput,
        openStreetMapUriInput,
        osmAndUriInput,
        urbiUriInput,
        wazeUriInput,
        yandexMapsShortLinkInput,
        yandexMapsUriInput,
        coordinateInput,
        debugUriInput,
    ).run {
        if (BuildConfig.DEBUG) {
            this + debugUriInput
        } else {
            this
        }
    }
}
