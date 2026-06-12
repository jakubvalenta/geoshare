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
import page.ooooo.geoshare.lib.inputs.GoogleNavigationUriInput
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

interface InputRepository {
    val amapShortLinkInput: AmapShortLinkInput
    val amapUriInput: AmapUriInput
    val appleMapsUriInput: AppleMapsUriInput
    val baiduMapShortLinkInput: BaiduMapShortLinkInput
    val baiduMapUriInput: BaiduMapUriInput
    val cartesIGNUriInput: CartesIGNUriInput
    val coordinateInput: CoordinateInput
    val debugUriInput: DebugUriInput
    val geoUriInput: GeoUriInput
    val googleMapsShortLinkInput: GoogleMapsShortLinkInput
    val googleMapsUriInput: GoogleMapsUriInput
    val googleNavigationUriInput: GoogleNavigationUriInput
    val hereWeGoUriInput: HereWeGoUriInput
    val magicEarthUriInput: MagicEarthUriInput
    val mapsMeUriInput: MapsMeUriInput
    val mapyComShortLinkInput: MapyComShortLinkInput
    val mapyComUriInput: MapyComUriInput
    val openStreetMapUriInput: OpenStreetMapUriInput
    val osmAndUriInput: OsmAndUriInput
    val plusCodeInput: PlusCodeInput
    val urbiUriInput: UrbiUriInput
    val wazeUriInput: WazeUriInput
    val yandexMapsShortLinkInput: YandexMapsShortLinkInput
    val yandexMapsUriInput: YandexMapsUriInput

    /**
     * All [Input] objects.
     *
     * Order matters, because [page.ooooo.geoshare.lib.conversion.ConversionState] will try the inputs in order when
     * parsing a URI.
     */
    val all: List<Input>
        get() = listOf(
            geoUriInput,
            plusCodeInput,
            googleMapsShortLinkInput,
            googleMapsUriInput,
            googleNavigationUriInput,
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

@Singleton
class DefaultInputRepository @Inject constructor(
    override val amapShortLinkInput: AmapShortLinkInput,
    override val amapUriInput: AmapUriInput,
    override val appleMapsUriInput: AppleMapsUriInput,
    override val baiduMapShortLinkInput: BaiduMapShortLinkInput,
    override val baiduMapUriInput: BaiduMapUriInput,
    override val cartesIGNUriInput: CartesIGNUriInput,
    override val coordinateInput: CoordinateInput,
    override val debugUriInput: DebugUriInput,
    override val geoUriInput: GeoUriInput,
    override val googleMapsShortLinkInput: GoogleMapsShortLinkInput,
    override val googleMapsUriInput: GoogleMapsUriInput,
    override val googleNavigationUriInput: GoogleNavigationUriInput,
    override val hereWeGoUriInput: HereWeGoUriInput,
    override val magicEarthUriInput: MagicEarthUriInput,
    override val mapsMeUriInput: MapsMeUriInput,
    override val mapyComShortLinkInput: MapyComShortLinkInput,
    override val mapyComUriInput: MapyComUriInput,
    override val openStreetMapUriInput: OpenStreetMapUriInput,
    override val osmAndUriInput: OsmAndUriInput,
    override val plusCodeInput: PlusCodeInput,
    override val urbiUriInput: UrbiUriInput,
    override val wazeUriInput: WazeUriInput,
    override val yandexMapsShortLinkInput: YandexMapsShortLinkInput,
    override val yandexMapsUriInput: YandexMapsUriInput,
) : InputRepository
