package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.engine.mock.MockEngine
import page.ooooo.geoshare.data.DefaultInputRepository
import page.ooooo.geoshare.data.InputRepository
import page.ooooo.geoshare.data.inputs.AmapShortLinkInput
import page.ooooo.geoshare.data.inputs.AmapUriInput
import page.ooooo.geoshare.data.inputs.AppleMapsHtmlInput
import page.ooooo.geoshare.data.inputs.AppleMapsUriInput
import page.ooooo.geoshare.data.inputs.BaiduMapShortLinkInput
import page.ooooo.geoshare.data.inputs.BaiduMapUriInput
import page.ooooo.geoshare.data.inputs.BaiduMapWebViewInput
import page.ooooo.geoshare.data.inputs.CartesIGNUriInput
import page.ooooo.geoshare.data.inputs.CoordinateInput
import page.ooooo.geoshare.data.inputs.DebugUriInput
import page.ooooo.geoshare.data.inputs.DebugWebViewInput
import page.ooooo.geoshare.data.inputs.GeoUriInput
import page.ooooo.geoshare.data.inputs.GoogleMapsAddressApiInput
import page.ooooo.geoshare.data.inputs.GoogleMapsHtmlInput
import page.ooooo.geoshare.data.inputs.GoogleMapsPlaceApiInput
import page.ooooo.geoshare.data.inputs.GoogleMapsPlaceListInput
import page.ooooo.geoshare.data.inputs.GoogleMapsShortLinkInput
import page.ooooo.geoshare.data.inputs.GoogleMapsUriInput
import page.ooooo.geoshare.data.inputs.GoogleNavigationUriInput
import page.ooooo.geoshare.data.inputs.GoogleSearchUriInput
import page.ooooo.geoshare.data.inputs.HereWeGoUriInput
import page.ooooo.geoshare.data.inputs.InputGroup
import page.ooooo.geoshare.data.inputs.KagiMapsUriInput
import page.ooooo.geoshare.data.inputs.MagicEarthUriInput
import page.ooooo.geoshare.data.inputs.MapQuestHtmlInput
import page.ooooo.geoshare.data.inputs.MapQuestUriInput
import page.ooooo.geoshare.data.inputs.MapsMeUriInput
import page.ooooo.geoshare.data.inputs.MapyComShortLinkInput
import page.ooooo.geoshare.data.inputs.MapyComUriInput
import page.ooooo.geoshare.data.inputs.OpenStreetMapApiInput
import page.ooooo.geoshare.data.inputs.OpenStreetMapUriInput
import page.ooooo.geoshare.data.inputs.OsmAndUriInput
import page.ooooo.geoshare.data.inputs.PlusCodeTextInput
import page.ooooo.geoshare.data.inputs.PlusCodeUriInput
import page.ooooo.geoshare.data.inputs.UrbiHtmlInput
import page.ooooo.geoshare.data.inputs.UrbiUriInput
import page.ooooo.geoshare.data.inputs.WazeApiInput
import page.ooooo.geoshare.data.inputs.WazeHtmlInput
import page.ooooo.geoshare.data.inputs.WazeUriInput
import page.ooooo.geoshare.data.inputs.YahooMapsShortLinkInput
import page.ooooo.geoshare.data.inputs.YahooMapsUriInput
import page.ooooo.geoshare.data.inputs.YahooMapsWebViewInput
import page.ooooo.geoshare.data.inputs.YandexMapsHtmlInput
import page.ooooo.geoshare.data.inputs.YandexMapsShortLinkInput
import page.ooooo.geoshare.data.inputs.YandexMapsUriInput
import page.ooooo.geoshare.data.log.FakeLog
import page.ooooo.geoshare.data.network.ServerHttpClientFactory
import page.ooooo.geoshare.data.uri.FakeUriQuote
import javax.inject.Singleton
import kotlin.time.Clock

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
        geoUriInput: GeoUriInput,
        googleMapsShortLinkInput: GoogleMapsShortLinkInput,
        googleMapsUriInput: GoogleMapsUriInput,
        googleNavigationUriInput: GoogleNavigationUriInput,
        kagiMapsUriInput: KagiMapsUriInput,
        hereWeGoUriInput: HereWeGoUriInput,
        magicEarthUriInput: MagicEarthUriInput,
        mapQuestHtmlInput: MapQuestHtmlInput,
        mapQuestUriInput: MapQuestUriInput,
        mapsMeUriInput: MapsMeUriInput,
        mapyComShortLinkInput: MapyComShortLinkInput,
        mapyComUriInput: MapyComUriInput,
        openStreetMapUriInput: OpenStreetMapUriInput,
        osmAndUriInput: OsmAndUriInput,
        plusCodeUriInput: PlusCodeUriInput,
        urbiUriInput: UrbiUriInput,
        wazeUriInput: WazeUriInput,
        yahooMapsShortLinkInput: YahooMapsShortLinkInput,
        yahooMapsUriInput: YahooMapsUriInput,
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
            CoordinateInput,
            DebugUriInput,
            geoUriInput,
            googleMapsShortLinkInput,
            googleMapsUriInput,
            googleNavigationUriInput,
            GoogleSearchUriInput,
            kagiMapsUriInput,
            hereWeGoUriInput,
            magicEarthUriInput,
            mapQuestHtmlInput,
            mapQuestUriInput,
            mapsMeUriInput,
            mapyComShortLinkInput,
            mapyComUriInput,
            openStreetMapUriInput,
            osmAndUriInput,
            PlusCodeTextInput,
            plusCodeUriInput,
            urbiUriInput,
            wazeUriInput,
            yahooMapsShortLinkInput,
            yahooMapsUriInput,
            yandexMapsShortLinkInput,
            yandexMapsUriInput,
        )
}

object FakeInputRepository : InputRepository {
    private val engine = MockEngine { throw NotImplementedError() }
    private val log = FakeLog
    private val serverRepository = FakeServerRepository()
    private val uriQuote = FakeUriQuote
    private val userPreferencesRepository = FakeUserPreferencesRepository()
    private val serverHttpClientFactory = ServerHttpClientFactory(
        engine = engine,
        keyStoreTools = FakeKeyStoreTools(),
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
        log = log,
    )
    override val cartesIGNUriInput = CartesIGNUriInput(
        uriQuote = uriQuote,
    )
    override val coordinateInput = CoordinateInput
    override val debugUriInput = DebugUriInput
    val debugWebViewInput = DebugWebViewInput
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
        googleMapsHtmlInput = { googleMapsHtmlInput },
        googleMapsPlaceApiInput = { googleMapsPlaceApiInput },
        googleMapsPlaceListInput = { googleMapsPlaceListInput },
        uriQuote = uriQuote,
    )
    val googleMapsAddressApiInput = GoogleMapsAddressApiInput(
        serverHttpClientFactory = serverHttpClientFactory,
        googleMapsHtmlInput = { googleMapsHtmlInput },
        serverRepository = serverRepository,
        uriQuote = uriQuote,
    )
    val googleMapsPlaceApiInput = GoogleMapsPlaceApiInput(
        serverHttpClientFactory = serverHttpClientFactory,
        googleMapsHtmlInput = { googleMapsHtmlInput },
        serverRepository = serverRepository,
        uriQuote = uriQuote,
    )
    val googleMapsHtmlInput = object : GoogleMapsHtmlInput {
        override val group = InputGroup.GOOGLE_MAPS
    }
    val googleMapsPlaceListInput = object : GoogleMapsPlaceListInput {
        override val group = InputGroup.GOOGLE_MAPS
    }
    override val googleNavigationUriInput = GoogleNavigationUriInput(
        googleMapsAddressApiInput = { googleMapsAddressApiInput },
        uriQuote = uriQuote,
    )
    override val googleSearchUriInput = GoogleSearchUriInput
    override val hereWeGoUriInput = HereWeGoUriInput(
        uriQuote = uriQuote,
    )
    override val magicEarthUriInput = MagicEarthUriInput(
        uriQuote = uriQuote,
    )
    override val mapQuestHtmlInput = MapQuestHtmlInput(
        engine = engine,
        log = log,
        uriQuote = uriQuote,
    )
    override val mapQuestUriInput = MapQuestUriInput(
        mapQuestHtmlInput = { mapQuestHtmlInput },
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
    override val kagiMapsUriInput = KagiMapsUriInput(
        openStreetMapApiInput = { openStreetMapApiInput },
        uriQuote = uriQuote,
    )
    override val osmAndUriInput = OsmAndUriInput(
        uriQuote = uriQuote,
    )
    override val plusCodeTextInput = PlusCodeTextInput
    override val plusCodeUriInput = PlusCodeUriInput(
        uriQuote = uriQuote,
    )
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
    val wazeApiInput = WazeApiInput(
        clock = Clock.System,
        engine = engine,
        log = log,
    )
    override val wazeUriInput = WazeUriInput(
        wazeApiInput = { wazeApiInput },
        wazeHtmlInput = { wazeHtmlInput },
        uriQuote = uriQuote
    )
    val wazeHtmlInput = WazeHtmlInput(
        engine = engine,
        log = log,
        uriQuote = uriQuote,
    )
    override val yahooMapsShortLinkInput = YahooMapsShortLinkInput(
        yahooMapsUriInput = { yahooMapsUriInput },
        engine = engine,
        log = log,
        uriQuote = uriQuote,
    )
    override val yahooMapsUriInput: YahooMapsUriInput = YahooMapsUriInput(
        yahooMapsWebViewInput = { yahooMapsWebViewInput },
        uriQuote = uriQuote,
    )
    val yahooMapsWebViewInput = YahooMapsWebViewInput(
        log = log,
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
