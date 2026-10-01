package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class KagiMapsUriInputTest : InputTest {
    override val resources: Resources = mock()
    private val input = FakeInputRepository.kagiMapsUriInput
    private val openStreetMapApiInput = FakeInputRepository.openStreetMapApiInput

    @Test
    fun match_fullUrl() {
        assertEquals(
            "https://kagi.com/maps/info#16.13/50.735739/15.7395",
            input.match("https://kagi.com/maps/info#16.13/50.735739/15.7395")
        )
        assertEquals(
            "https://kagi.com/maps/directions?q=%7CLugard%20Road,%2033%20Lugard%20Rd%20The%20Peak%20Hong%20Kong%20SAR,%20China~22.277521,114.144010",
            input.match("https://kagi.com/maps/directions?q=%7CLugard%20Road,%2033%20Lugard%20Rd%20The%20Peak%20Hong%20Kong%20SAR,%20China~22.277521,114.144010")
        )
        assertEquals(
            "www.kagi.com/maps/info#16.13/50.735739/15.7395",
            input.match("www.kagi.com/maps/info#16.13/50.735739/15.7395")
        )
        assertEquals(
            "kagi.com/maps/info#16.13/50.735739/15.7395",
            input.match("kagi.com/maps/info#16.13/50.735739/15.7395")
        )
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/maps/info#16.13/50.735739/15.7395"))
    }

    @Test
    fun match_unknownScheme() {
        assertEquals(
            "kagi.com/maps/info#16.13/50.735739/15.7395",
            input.match("ftp://kagi.com/maps/info#16.13/50.735739/15.7395"),
        )
    }

    @Test
    fun match_spaces() {
        assertEquals(
            "https://kagi.com/maps/info?q=foobar",
            input.match("https://kagi.com/maps/info?q=foobar ")
        )
        assertEquals(
            "https://kagi.com/maps/info?q=foo bar",
            input.match("https://kagi.com/maps/info?q=foo bar ")
        )
        assertEquals(
            "https://kagi.com/maps/info?q=foo",
            input.match("https://kagi.com/maps/info?q=foo  bar")
        )
        assertEquals(
            "https://kagi.com/maps/info?q=foo",
            input.match("https://kagi.com/maps/info?q=foo\tbar")
        )
    }

    @Test
    fun parse_unknownPathOrParams() = runTest {
        assertEquals(ParseResult.Success(), input.parse("https://kagi.com"))
        assertEquals(ParseResult.Success(), input.parse("https://kagi.com/maps"))
        assertEquals(ParseResult.Success(), input.parse("https://kagi.com/maps/"))
        assertEquals(ParseResult.Success(), input.parse("https://kagi.com/maps/info"))
    }

    @Test
    fun parse_coordinates() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(53.205480, 5.784024, z = 17.5, name = "Shared Point", source = Source.URI)
                )
            ),
            input.parse("https://kagi.com/maps/info?q=Shared%20Point&ll=53.205291,5.783568&id=point_53.205480_5.784024#17.5/53.205291/5.783568"),
        )
    }

    @Test
    fun parse_coordinatesInvalid() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(53.205291,5.783568, z = 17.5, name = "Shared Point", source = Source.MAP_CENTER)
                )
            ),
            input.parse("https://kagi.com/maps/info?q=Shared%20Point&ll=53.205291,5.783568&id=point_spam_spam#17.5/53.205291/5.783568"),
        )
    }

    @Test
    fun parse_placeOpenStreetMapName() = runTest {
        assertEquals(
            ParseResult.Success(
                points = persistentListOf(
                    WGS84Point(
                        22.27603906188044, 114.14545744657516,
                        z = 19.0,
                        name = "Victoria Peak",
                        source = Source.MAP_CENTER,
                    ),
                ),
                next = MatchedInput(openStreetMapApiInput, "https://www.openstreetmap.org/node/26412171"),
            ),
            input.parse(uriString = "https://kagi.com/maps/info?z=19&ll=22.27603906188044,114.14545744657516&id=n26412171&q=Victoria%20Peak#17.5/22.276039/114.145457"),
        )
    }

    @Test
    fun parse_placeOpenStreetMapRelation() = runTest {
        assertEquals(
            ParseResult.Success(
                points = persistentListOf(
                    WGS84Point(
                        50.72525, 15.59019,
                        z = 12.34,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Špindlerův Mlýn",
                        source = Source.MAP_CENTER,
                    ),
                ),
                next = MatchedInput(openStreetMapApiInput, "https://www.openstreetmap.org/relation/440105"),
            ),
            input.parse(uriString = "https://kagi.com/maps/info?q=%C5%A0pindler%C5%AFv%20Ml%C3%BDn&id=r440105#12.34/50.72525/15.59019"),
        )
    }

    @Test
    fun parse_placeOpenStreetMapWay() = runTest {
        assertEquals(
            ParseResult.Success(
                points = persistentListOf(
                    WGS84Point(
                        22.275900068060622, 114.14579272270203,
                        z = 19.0,
                        name = "Victoria Peak Station HK Telecom Radio Station",
                        source = Source.MAP_CENTER,
                    ),
                ),
                next = MatchedInput(openStreetMapApiInput, "https://www.openstreetmap.org/way/448058512"),
            ),
            input.parse(uriString = "https://kagi.com/maps/info?z=19&ll=22.275900068060622,114.14579272270203&id=w448058512&q=Victoria%20Peak%20Station%20HK%20Telecom%20Radio%20Station#17.5/22.275905/114.14503"),
        )
    }

    @Test
    fun parse_placeOpenStreetMapInvalid() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        22.275900068060622, 114.14579272270203,
                        z = 19.0,
                        name = "Victoria Peak Station HK Telecom Radio Station",
                        source = Source.MAP_CENTER,
                    ),
                )
            ),
            input.parse(uriString = "https://kagi.com/maps/info?z=19&ll=22.275900068060622,114.14579272270203&id=x448058512&q=Victoria%20Peak%20Station%20HK%20Telecom%20Radio%20Station#17.5/22.275905/114.14503"),
        )
    }

    @Test
    fun parse_placeOpenStreetMapNoCenter() = runTest {
        assertEquals(
            ParseResult.Success(
                next = MatchedInput(openStreetMapApiInput, "https://www.openstreetmap.org/way/448058512"),
            ),
            input.parse(uriString = "https://kagi.com/maps/info?z=19&id=w448058512&q=Victoria%20Peak%20Station%20HK%20Telecom%20Radio%20Station"),
        )
    }

    @Test
    fun parse_emptyId() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(22.276039, 114.144692, z = 17.5, name = "Victoria Peak", source = Source.MAP_CENTER)
                )
            ),
            input.parse("https://kagi.com/maps/info?q=Victoria%20Peak&ll=22.276039,114.144692&id#17.5/22.276039/114.144692"),
        )
    }

    @Test
    fun parse_mapCenterFromQueryParam() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        50.735981, 15.739860,
                        z = 16.13,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Sněžka",
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            input.parse("https://kagi.com/maps/info?q=Sn%C4%9B%C5%BEka&id=U8zPGlHJwIDz2VwrxsSEGaA06EqXhN0w2nWmr48ffeM-a51Q_TtSVLKK_JCs6s0P&ll=50.735981,15.739860#16.13/50.735739/15.7395"),
        )
    }

    @Test
    fun parse_mapCenterFromFragment() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(50.735739, 15.7395, z = 16.13, source = Source.MAP_CENTER)
                )
            ),
            input.parse("https://kagi.com/maps/info#16.13/50.735739/15.7395"),
        )
    }

    @Test
    fun parse_nameIsTrimmed() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        50.735981, 15.739860,
                        z = 16.13,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Sněžka",
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            input.parse("https://kagi.com/maps/info?q=Sn%C4%9B%C5%BEka%20&id=U8zPGlHJwIDz2VwrxsSEGaA06EqXhN0w2nWmr48ffeM-a51Q_TtSVLKK_JCs6s0P&ll=50.735981,15.739860#16.13/50.735739/15.7395"),
        )
    }

    @Test
    fun parse_queryOnly() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(name = "foo bar", source = Source.URI)
                )
            ),
            input.parse("https://kagi.com/maps/info?q=foo%20bar"),
        )
    }

    @Test
    fun parse_queryOnlyBlank() = runTest {
        assertEquals(
            ParseResult.Success(persistentListOf()),
            input.parse("https://kagi.com/maps/info?q="),
        )
        assertEquals(
            ParseResult.Success(persistentListOf()),
            input.parse("https://kagi.com/maps/info?q=%20"),
        )
    }

    @Test
    fun parse_directionsDestinationOnly() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        22.277521, 114.144010,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "Lugard Road, 33 Lugard Rd The Peak Hong Kong SAR, China",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://kagi.com/maps/directions?q=%7CLugard%20Road,%2033%20Lugard%20Rd%20The%20Peak%20Hong%20Kong%20SAR,%20China~22.277521,114.144010")
        )
    }

    @Test
    fun parse_directionsOriginAndWaypointAndDestination() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        22.279734, 114.138412,
                        z = 15.3,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Kotewall Fire Station",
                        source = Source.URI,
                    ),
                    WGS84Point(
                        22.277521, 114.144010,
                        z = 15.3,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "Lugard Road, 33 Lugard Rd The Peak Hong Kong SAR, China",
                        source = Source.URI,
                    ),
                    WGS84Point(
                        22.272224, 114.136284,
                        z = 15.3,
                        name = "High West",
                        source = Source.URI,
                    ),
                )
            ),
            input.parse("https://kagi.com/maps/directions?provider=all&mode=traffic&q=Kotewall%20Fire%20Station~22.279734,114.138412%7CLugard%20Road,%2033%20Lugard%20Rd%20The%20Peak%20Hong%20Kong%20SAR,%20China~22.277521,114.144010%7CHigh%20West~22.272224,114.136284#15.3/22.274585/114.139119")
        )
    }

    @Test
    fun parse_directionsOriginAndCoordinateDestination() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        39.91497, 116.390846,
                        z = 15.92,
                        name = "Forbidden City, China",
                        source = Source.URI,
                    ),
                    WGS84Point(
                        39.905918, 116.379191,
                        z = 15.92,
                        source = Source.URI,
                    ),
                )
            ),
            input.parse("https://kagi.com/maps/directions?provider=all&mode=traffic&q=Forbidden%20City,%20China~39.91497,116.390846%7C39.905918,116.379191#15.92/39.909553/116.387608")
        )
    }

    @Test
    fun parseDirectionsInvalidCoordinates() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(name = "Forbidden City, China", source = Source.URI)
                )
            ),
            input.parse("https://kagi.com/maps/directions?provider=all&mode=traffic&q=Forbidden%20City,%20China~spam#15.92/39.909553/116.387608")
        )
    }

    @Test
    fun parseDirectionsNamesOnly() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(name = "foo", source = Source.URI),
                    WGS84Point(name = "bar", source = Source.URI),
                )
            ),
            input.parse("https://kagi.com/maps/directions?provider=all&mode=traffic&q=foo%7Cbar")
        )
    }

    @Test
    fun parseDirectionsBlankPoint() = runTest {
        assertEquals(
            ParseResult.Success(persistentListOf()),
            input.parse("https://kagi.com/maps/directions?q=")
        )
        assertEquals(
            ParseResult.Success(persistentListOf()),
            input.parse("https://kagi.com/maps/directions?q=|")
        )
        assertEquals(
            ParseResult.Success(persistentListOf()),
            input.parse("https://kagi.com/maps/directions?q=%20|%20")
        )
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(name = "foo", source = Source.URI),
                )
            ),
            input.parse("https://kagi.com/maps/directions?q=%20|foo")
        )
    }

    @Test
    fun parseZoomInvalid() = runTest {
        assertEquals(
            ParseResult.Success(persistentListOf(
                WGS84Point(53.205291,5.783568, source = Source.MAP_CENTER)
            )),
            input.parse("https://kagi.com/maps/directions?z=spam&ll=53.205291,5.783568")
        )
    }
}
