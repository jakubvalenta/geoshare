package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import page.ooooo.geoshare.data.DefaultInputRepository
import page.ooooo.geoshare.data.InputRepository
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InputRepositoryModule {

    @Singleton
    @Provides
    fun provideInputRepository(
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
    ): InputRepository =
        DefaultInputRepository(
            amapShortLinkInput,
            amapUriInput,
            appleMapsUriInput,
            baiduMapShortLinkInput,
            baiduMapUriInput,
            cartesIGNUriInput,
            coordinateInput,
            debugUriInput,
            geoUriInput,
            googleMapsShortLinkInput,
            googleMapsUriInput,
            hereWeGoUriInput,
            magicEarthUriInput,
            mapsMeUriInput,
            mapyComShortLinkInput,
            mapyComUriInput,
            openStreetMapUriInput,
            osmAndUriInput,
            plusCodeInput,
            urbiUriInput,
            wazeUriInput,
            yandexMapsShortLinkInput,
            yandexMapsUriInput,
        )
}

class FakeInputRepository(
    override val amapShortLinkInput: AmapShortLinkInput = AmapShortLinkInput(
        amapUriInput = { throw NotImplementedError() },
    ),
    override val amapUriInput: AmapUriInput = AmapUriInput(),
    override val appleMapsUriInput: AppleMapsUriInput = AppleMapsUriInput(
        appleMapsHtmlInput = { throw NotImplementedError() },
    ),
    override val baiduMapShortLinkInput: BaiduMapShortLinkInput = BaiduMapShortLinkInput(
        baiduMapUriInput = { throw NotImplementedError() },
    ),
    override val baiduMapUriInput: BaiduMapUriInput = BaiduMapUriInput(
        baiduMapWebViewInput = { throw NotImplementedError() },
    ),
    override val cartesIGNUriInput: CartesIGNUriInput = CartesIGNUriInput(),
    override val coordinateInput: CoordinateInput = CoordinateInput(),
    override val debugUriInput: DebugUriInput = DebugUriInput(
        debugWebViewInput = { throw NotImplementedError() },
    ),
    override val geoUriInput: GeoUriInput = GeoUriInput(),
    override val googleMapsShortLinkInput: GoogleMapsShortLinkInput = GoogleMapsShortLinkInput(
        googleMapsUriInput = { throw NotImplementedError() },
    ),
    override val googleMapsUriInput: GoogleMapsUriInput = GoogleMapsUriInput(
        googleMapsHtmlInput = { throw NotImplementedError() },
        googleMapsPlaceListInput = { throw NotImplementedError() },
    ),
    override val hereWeGoUriInput: HereWeGoUriInput = HereWeGoUriInput(),
    override val magicEarthUriInput: MagicEarthUriInput = MagicEarthUriInput(),
    override val mapsMeUriInput: MapsMeUriInput = MapsMeUriInput(),
    override val mapyComShortLinkInput: MapyComShortLinkInput = MapyComShortLinkInput(
        mapyComUriInput = { throw NotImplementedError() },
    ),
    override val mapyComUriInput: MapyComUriInput = MapyComUriInput(),
    override val openStreetMapUriInput: OpenStreetMapUriInput = OpenStreetMapUriInput(
        openStreetMapApiInput = { throw NotImplementedError() },
    ),
    override val osmAndUriInput: OsmAndUriInput = OsmAndUriInput(),
    override val plusCodeInput: PlusCodeInput = PlusCodeInput(),
    override val urbiUriInput: UrbiUriInput = UrbiUriInput(
        urbiHtmlInput = { throw NotImplementedError() },
    ),
    override val wazeUriInput: WazeUriInput = WazeUriInput(
        wazeHtmlInput = { throw NotImplementedError() },
    ),
    override val yandexMapsShortLinkInput: YandexMapsShortLinkInput = YandexMapsShortLinkInput(
        yandexMapsUriInput = { throw NotImplementedError() },
    ),
    override val yandexMapsUriInput: YandexMapsUriInput = YandexMapsUriInput(
        yandexMapsHtmlInput = { throw NotImplementedError() },
    ),
) : InputRepository
