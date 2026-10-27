package page.ooooo.geoshare.data

import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.inputs.AmapShortLinkInput
import page.ooooo.geoshare.data.inputs.AmapUriInput
import page.ooooo.geoshare.data.inputs.AppleMapsUriInput
import page.ooooo.geoshare.data.inputs.BaiduMapShortLinkInput
import page.ooooo.geoshare.data.inputs.BaiduMapUriInput
import page.ooooo.geoshare.data.inputs.CartesIGNUriInput
import page.ooooo.geoshare.data.inputs.CoordinateInput
import page.ooooo.geoshare.data.inputs.DebugUriInput
import page.ooooo.geoshare.data.inputs.GeoUriInput
import page.ooooo.geoshare.data.inputs.GoogleMapsShortLinkInput
import page.ooooo.geoshare.data.inputs.GoogleMapsUriInput
import page.ooooo.geoshare.data.inputs.GoogleNavigationUriInput
import page.ooooo.geoshare.data.inputs.GoogleSearchUriInput
import page.ooooo.geoshare.data.inputs.HereWeGoUriInput
import page.ooooo.geoshare.data.inputs.Input
import page.ooooo.geoshare.data.inputs.KagiMapsUriInput
import page.ooooo.geoshare.data.inputs.MagicEarthUriInput
import page.ooooo.geoshare.data.inputs.MapQuestHtmlInput
import page.ooooo.geoshare.data.inputs.MapQuestUriInput
import page.ooooo.geoshare.data.inputs.MapsMeUriInput
import page.ooooo.geoshare.data.inputs.MapyComShortLinkInput
import page.ooooo.geoshare.data.inputs.MapyComUriInput
import page.ooooo.geoshare.data.inputs.OpenStreetMapUriInput
import page.ooooo.geoshare.data.inputs.OsmAndUriInput
import page.ooooo.geoshare.data.inputs.PlusCodeTextInput
import page.ooooo.geoshare.data.inputs.PlusCodeUriInput
import page.ooooo.geoshare.data.inputs.UrbiUriInput
import page.ooooo.geoshare.data.inputs.WazeUriInput
import page.ooooo.geoshare.data.inputs.YahooMapsShortLinkInput
import page.ooooo.geoshare.data.inputs.YahooMapsUriInput
import page.ooooo.geoshare.data.inputs.YandexMapsShortLinkInput
import page.ooooo.geoshare.data.inputs.YandexMapsUriInput
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
    val googleSearchUriInput: GoogleSearchUriInput
    val kagiMapsUriInput: KagiMapsUriInput
    val hereWeGoUriInput: HereWeGoUriInput
    val magicEarthUriInput: MagicEarthUriInput
    val mapQuestHtmlInput: MapQuestHtmlInput
    val mapQuestUriInput: MapQuestUriInput
    val mapsMeUriInput: MapsMeUriInput
    val mapyComShortLinkInput: MapyComShortLinkInput
    val mapyComUriInput: MapyComUriInput
    val openStreetMapUriInput: OpenStreetMapUriInput
    val osmAndUriInput: OsmAndUriInput
    val plusCodeTextInput: PlusCodeTextInput
    val plusCodeUriInput: PlusCodeUriInput
    val urbiUriInput: UrbiUriInput
    val wazeUriInput: WazeUriInput
    val yahooMapsShortLinkInput: YahooMapsShortLinkInput
    val yahooMapsUriInput: YahooMapsUriInput
    val yandexMapsShortLinkInput: YandexMapsShortLinkInput
    val yandexMapsUriInput: YandexMapsUriInput

    /**
     * All [Input] objects.
     *
     * Order matters, because when parsing a URI, [page.ooooo.geoshare.data.state.ConversionState] will try the inputs in
     * order.
     */
    val all: List<Input>
        get() = listOf(
            geoUriInput,
            googleMapsShortLinkInput,
            googleSearchUriInput,
            googleMapsUriInput,
            googleNavigationUriInput,
            appleMapsUriInput,
            amapShortLinkInput,
            amapUriInput,
            baiduMapShortLinkInput,
            baiduMapUriInput,
            cartesIGNUriInput,
            hereWeGoUriInput,
            kagiMapsUriInput,
            magicEarthUriInput,
            mapQuestHtmlInput,
            mapQuestUriInput,
            mapsMeUriInput,
            mapyComShortLinkInput,
            mapyComUriInput,
            openStreetMapUriInput,
            osmAndUriInput,
            plusCodeTextInput,
            plusCodeUriInput,
            urbiUriInput,
            wazeUriInput,
            yahooMapsShortLinkInput,
            yahooMapsUriInput,
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

    fun genPlaceholderUris(randomPoint: Point): List<String> =
        listOf(
            googleMapsShortLinkInput,
            googleMapsUriInput,
            appleMapsUriInput,
            *all
                .filterIsInstance<Input.HasRandomSource>()
                .shuffled()
                .toTypedArray()
        )
            .mapNotNull { it.genRandomSource(randomPoint) }

    fun genRandomSource(randomPoint: Point): String? =
        all
            .filterIsInstance<Input.HasRandomSource>()
            .shuffled()
            .firstNotNullOfOrNull { it.genRandomSource(randomPoint) }
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
    override val googleSearchUriInput: GoogleSearchUriInput,
    override val kagiMapsUriInput: KagiMapsUriInput,
    override val hereWeGoUriInput: HereWeGoUriInput,
    override val magicEarthUriInput: MagicEarthUriInput,
    override val mapQuestHtmlInput: MapQuestHtmlInput,
    override val mapQuestUriInput: MapQuestUriInput,
    override val mapsMeUriInput: MapsMeUriInput,
    override val mapyComShortLinkInput: MapyComShortLinkInput,
    override val mapyComUriInput: MapyComUriInput,
    override val openStreetMapUriInput: OpenStreetMapUriInput,
    override val osmAndUriInput: OsmAndUriInput,
    override val plusCodeTextInput: PlusCodeTextInput,
    override val plusCodeUriInput: PlusCodeUriInput,
    override val urbiUriInput: UrbiUriInput,
    override val wazeUriInput: WazeUriInput,
    override val yahooMapsShortLinkInput: YahooMapsShortLinkInput,
    override val yahooMapsUriInput: YahooMapsUriInput,
    override val yandexMapsShortLinkInput: YandexMapsShortLinkInput,
    override val yandexMapsUriInput: YandexMapsUriInput,
) : InputRepository
