package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.engine.mock.MockEngine
import page.ooooo.geoshare.data.DefaultInputRepository
import page.ooooo.geoshare.data.InputRepository
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.inputs.AmapShortLinkInput
import page.ooooo.geoshare.lib.inputs.AmapUriInput
import page.ooooo.geoshare.lib.inputs.AppleMapsHtmlInput
import page.ooooo.geoshare.lib.inputs.AppleMapsUriInput
import page.ooooo.geoshare.lib.inputs.BaiduMapShortLinkInput
import page.ooooo.geoshare.lib.inputs.BaiduMapUriInput
import page.ooooo.geoshare.lib.inputs.BaiduMapWebViewInput
import page.ooooo.geoshare.lib.inputs.CartesIGNUriInput
import page.ooooo.geoshare.lib.inputs.CoordinateInput
import page.ooooo.geoshare.lib.inputs.DebugUriInput
import page.ooooo.geoshare.lib.inputs.DebugWebViewInput
import page.ooooo.geoshare.lib.inputs.GeoUriInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsAddressApiInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsPlaceApiInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsPlaceListInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsShortLinkInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsUriInput
import page.ooooo.geoshare.lib.inputs.HereWeGoUriInput
import page.ooooo.geoshare.lib.inputs.MagicEarthUriInput
import page.ooooo.geoshare.lib.inputs.MapsMeUriInput
import page.ooooo.geoshare.lib.inputs.MapyComShortLinkInput
import page.ooooo.geoshare.lib.inputs.MapyComUriInput
import page.ooooo.geoshare.lib.inputs.OpenStreetMapApiInput
import page.ooooo.geoshare.lib.inputs.OpenStreetMapUriInput
import page.ooooo.geoshare.lib.inputs.OsmAndUriInput
import page.ooooo.geoshare.lib.inputs.PlusCodeInput
import page.ooooo.geoshare.lib.inputs.UrbiHtmlInput
import page.ooooo.geoshare.lib.inputs.UrbiUriInput
import page.ooooo.geoshare.lib.inputs.WazeHtmlInput
import page.ooooo.geoshare.lib.inputs.WazeUriInput
import page.ooooo.geoshare.lib.inputs.YandexMapsHtmlInput
import page.ooooo.geoshare.lib.inputs.YandexMapsShortLinkInput
import page.ooooo.geoshare.lib.inputs.YandexMapsUriInput
import page.ooooo.geoshare.lib.network.ApiService
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

object FakeInputRepository : InputRepository {
    private val engine = MockEngine { throw NotImplementedError() }
    private val log = FakeLog
    private val uriQuote = FakeUriQuote
    private val userPreferencesRepository = FakeUserPreferencesRepository()
    private val apiService = ApiService(
        engine = engine,
        log = log,
        userPreferencesRepository = userPreferencesRepository,
    )

    override val amapShortLinkInput = AmapShortLinkInput(
        amapUriInput = { amapUriInput },
        engine = engine,
        log = log,
        uriQuote = uriQuote,
    )
    override val amapUriInput = AmapUriInput(
        uriQuote = uriQuote,
    )
    override val appleMapsUriInput = AppleMapsUriInput(
        appleMapsHtmlInput = { appleMapsHtmlInput },
        uriQuote = uriQuote,
    )
    val appleMapsHtmlInput = AppleMapsHtmlInput(
        engine = engine,
        log = log,
        uriQuote = uriQuote,
    )
    override val baiduMapShortLinkInput = BaiduMapShortLinkInput(
        baiduMapUriInput = { baiduMapUriInput },
        engine = engine,
        log = log,
        uriQuote = uriQuote,
    )
    override val baiduMapUriInput = BaiduMapUriInput(
        baiduMapWebViewInput = { baiduMapWebViewInput },
        uriQuote = uriQuote,
    )
    val baiduMapWebViewInput = BaiduMapWebViewInput(
        log = FakeLog,
    )
    override val cartesIGNUriInput = CartesIGNUriInput(
        uriQuote = uriQuote,
    )
    override val coordinateInput = CoordinateInput()
    override val debugUriInput = DebugUriInput(
        debugWebViewInput = { debugWebViewInput },
        uriQuote = uriQuote,
    )
    val debugWebViewInput = DebugWebViewInput()
    override val geoUriInput = GeoUriInput(
        uriQuote = uriQuote,
    )
    override val googleMapsShortLinkInput = GoogleMapsShortLinkInput(
        googleMapsUriInput = { googleMapsUriInput },
        engine = engine,
        log = log,
        uriQuote = uriQuote,
    )
    override val googleMapsUriInput = GoogleMapsUriInput(
        googleMapsAddressApiInput = { googleMapsAddressApiInput },
        googleMapsPlaceApiInput = { googleMapsPlaceApiInput },
        googleMapsPlaceListInput = { googleMapsPlaceListInput },
        uriQuote = uriQuote,
    )
    val googleMapsAddressApiInput = GoogleMapsAddressApiInput(
        apiService = apiService,
        googleMapsHtmlInput = { googleMapsHtmlInput },
        userPreferencesRepository = userPreferencesRepository,
        uriQuote = uriQuote,
    )
    val googleMapsPlaceApiInput = GoogleMapsPlaceApiInput(
        apiService = apiService,
        googleMapsHtmlInput = { googleMapsHtmlInput },
        userPreferencesRepository = userPreferencesRepository,
        uriQuote = uriQuote,
    )
    val googleMapsHtmlInput = object : GoogleMapsHtmlInput {}
    val googleMapsPlaceListInput = object : GoogleMapsPlaceListInput {}
    override val hereWeGoUriInput = HereWeGoUriInput(
        uriQuote = uriQuote,
    )
    override val magicEarthUriInput = MagicEarthUriInput(
        uriQuote = uriQuote,
    )
    override val mapsMeUriInput = MapsMeUriInput(
        uriQuote = uriQuote,
    )
    override val mapyComShortLinkInput = MapyComShortLinkInput(
        mapyComUriInput = { mapyComUriInput },
        engine = engine,
        log = log,
        uriQuote = uriQuote,
    )
    override val mapyComUriInput = MapyComUriInput(
        uriQuote = uriQuote,
    )
    override val openStreetMapUriInput = OpenStreetMapUriInput(
        openStreetMapApiInput = { openStreetMapApiInput },
        uriQuote = uriQuote,
    )
    val openStreetMapApiInput = OpenStreetMapApiInput(
        engine = engine,
        log = log,
        uriQuote = uriQuote,
    )
    override val osmAndUriInput = OsmAndUriInput(
        uriQuote = uriQuote,
    )
    override val plusCodeInput = PlusCodeInput()
    override val urbiUriInput = UrbiUriInput(
        urbiHtmlInput = { urbiHtmlInput },
        uriQuote = uriQuote,
    )
    val urbiHtmlInput: UrbiHtmlInput = UrbiHtmlInput(
        urbiUriInput = { urbiUriInput },
        engine = engine,
        log = log,
        uriQuote = uriQuote,
    )
    override val wazeUriInput = WazeUriInput(
        wazeHtmlInput = { wazeHtmlInput },
        uriQuote = uriQuote
    )
    val wazeHtmlInput = WazeHtmlInput(
        engine = engine,
        log = log,
        uriQuote = uriQuote,
    )
    override val yandexMapsShortLinkInput = YandexMapsShortLinkInput(
        yandexMapsUriInput = { yandexMapsUriInput },
        engine = engine,
        log = log,
        uriQuote = uriQuote,
    )
    override val yandexMapsUriInput = YandexMapsUriInput(
        yandexMapsHtmlInput = { yandexMapsHtmlInput },
        uriQuote = uriQuote,
    )
    val yandexMapsHtmlInput = YandexMapsHtmlInput(
        engine = engine,
        log = log,
        uriQuote = uriQuote,
    )
}
