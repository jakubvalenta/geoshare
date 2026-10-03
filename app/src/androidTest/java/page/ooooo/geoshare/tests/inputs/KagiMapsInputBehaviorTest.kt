package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.assertConversionSucceeds
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.denyConnectionPermission
import page.ooooo.geoshare.tests.shareUri
import page.ooooo.geoshare.tests.testUri
import page.ooooo.geoshare.tests.testUriFails

class KagiMapsInputBehaviorTest : InputBehaviorTest {
    @Test
    fun kagiMaps_offline() = uiAutomator {
        // Point
        testUri(
            persistentListOf(
                WGS84Point(53.205480, 5.784024, z = 17.5, name = "Shared Point", source = Source.URI)
            ),
            "https://kagi.com/maps/info?q=Shared%20Point&ll=53.205291,5.783568&id=point_53.205480_5.784024#17.5/53.205291/5.783568",
        )

        // Opaque id (not supported)
        testUriFails(
            setOf(
                "Links with place ids are not supported, because they are accessible only after logging in to Kagi.",
                // TODO Add French
            ),
            "https://kagi.com/maps/info?q=Sn%C4%9B%C5%BEka&id=U8zPGlHJwIDz2VwrxsSEGaA06EqXhN0w2nWmr48ffeM-a51Q_TtSVLKK_JCs6s0P&ll=50.735981,15.739860#16.13/50.735739/15.7395",
        )

        // Map center
        testUri(
            WGS84Point(50.735739, 15.7395, z = 16.13, source = Source.MAP_CENTER),
            "https://kagi.com/maps/#16.13/50.735739/15.7395",
        )

        // Directions
        testUri(
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
            ),
            "https://kagi.com/maps/directions?provider=all&mode=traffic&q=Kotewall%20Fire%20Station~22.279734,114.138412%7CLugard%20Road,%2033%20Lugard%20Rd%20The%20Peak%20Hong%20Kong%20SAR,%20China~22.277521,114.144010%7CHigh%20West~22.272224,114.136284#15.3/22.274585/114.139119",
        )
    }

    @Test
    fun kagiMaps_online() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("www.openstreetmap.org") // Kagi Maps input uses OpenStreetMap API
        }

        // OSM way
        testUri(
            persistentListOf(
                WGS84Point(22.2759891, 114.1457360, z = 19.0, source = Source.API),
                WGS84Point(22.2759569, 114.1458881, z = 19.0, source = Source.API),
                WGS84Point(22.2758356, 114.1458644, z = 19.0, source = Source.API),
                WGS84Point(22.2758363, 114.1457616, z = 19.0, source = Source.API),
                WGS84Point(22.2758200, 114.1457576, z = 19.0, source = Source.API),
                WGS84Point(22.2758331, 114.1456982, z = 19.0, source = Source.API),
                WGS84Point(22.2758389, 114.1458454, z = 19.0, source = Source.API),
                WGS84Point(
                    22.2758205, 114.1458413,
                    z = 19.0,
                    name = "Victoria Peak Station HK Telecom Radio Station",
                    source = Source.API,
                ),
            ),
            "https://kagi.com/maps/info?z=19&ll=22.275900068060622,114.14579272270203&id=w448058512&q=Victoria%20Peak%20Station%20HK%20Telecom%20Radio%20Station#17.5/22.275905/114.14503",
            grantConnectionPermission = true,
        )

        // OSM way, when permission is denied
        shareUri("https://kagi.com/maps/info?z=19&ll=22.275900068060622,114.14579272270203&id=w448058512&q=Victoria%20Peak%20Station%20HK%20Telecom%20Radio%20Station#17.5/22.275905/114.14503")
        denyConnectionPermission()
        assertConversionSucceeds(
            persistentListOf(
                WGS84Point(
                    z = 19.0,
                    name = "Victoria Peak Station HK Telecom Radio Station",
                    source = Source.URI,
                )
            )
        )
    }
}
