package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import io.ktor.http.HttpStatusCode
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.assumeHttpGetReturnsStatus
import page.ooooo.geoshare.tests.testUri

class YahooMapsInputBehaviorTest : InputBehaviorTest {
    @Test
    fun yahooMaps_offline() = uiAutomator {
        // Map center
        testUri(
            WGS84Point(35.04067, 135.73777, z = 16.0, source = Source.MAP_CENTER),
            "https://map.yahoo.co.jp/?lat=35.04067&lon=135.73777&zoom=16&maptype=basic",
        )

        // Directions
        testUri(
            persistentListOf(
                WGS84Point(
                    35.04361,135.75926,
                    z = 14.0,
                    name = "京都府京都市北区小山上総町",
                    source = Source.URI,
                ),
                WGS84Point(
                    35.02924,135.76683,
                    z = 14.0,
                    name = "京都府京都市上京区革堂内町",
                    source = Source.URI,
                ),
                WGS84Point(
                    35.01749, 135.76726,
                    z = 14.0,
                    name = "京都府京都市中京区下御霊前町",
                    source = Source.URI,
                ),
                WGS84Point(
                    35.01084,135.75962,
                    z = 14.0,
                    name = "京都府京都市中京区虎屋町",
                    source = Source.URI,
                ),
            ),
            "https://map.yahoo.co.jp/route/walk?from=%E4%BA%AC%E9%83%BD%E5%BA%9C%E4%BA%AC%E9%83%BD%E5%B8%82%E5%8C%97%E5%8C%BA%E5%B0%8F%E5%B1%B1%E4%B8%8A%E7%B7%8F%E7%94%BA&to=%E4%BA%AC%E9%83%BD%E5%BA%9C%E4%BA%AC%E9%83%BD%E5%B8%82%E4%B8%AD%E4%BA%AC%E5%8C%BA%E8%99%8E%E5%B1%8B%E7%94%BA&fromLat=35.04361&fromLon=135.75926&toLat=35.01084&toLon=135.75962&waypoints=name%3A%E4%BA%AC%E9%83%BD%E5%BA%9C%E4%BA%AC%E9%83%BD%E5%B8%82%E4%B8%8A%E4%BA%AC%E5%8C%BA%E9%9D%A9%E5%A0%82%E5%86%85%E7%94%BA%2Clat%3A35.02924%2Clon%3A135.76683%3Bname%3A%E4%BA%AC%E9%83%BD%E5%BA%9C%E4%BA%AC%E9%83%BD%E5%B8%82%E4%B8%AD%E4%BA%AC%E5%8C%BA%E4%B8%8B%E5%BE%A1%E9%9C%8A%E5%89%8D%E7%94%BA%2Clat%3A35.01749%2Clon%3A135.76726%3B&sort=1&lat=35.02766&lon=135.76411&zoom=14&maptype=basic",
        )
    }

    @Test
    fun yahooMaps_online() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("yahoo.jp")
        }

        // Short link
        testUri(
            WGS84Point(35.03923, 135.72843, z = 17.0, source = Source.URI),
            "https://yahoo.jp/wMAoiQ", // Resolves to https://map.yahoo.co.jp/place?lat=35.03923&lon=135.72843&zoom=17&maptype=basic
            grantConnectionPermission = true,
        )
    }

    @Test
    fun yahooMaps_onlineAndServiceAccessible() = uiAutomator {
        runBlocking {
            assumeHttpGetReturnsStatus("https://map.yahoo.co.jp", HttpStatusCode.OK)
        }

        // Place
        testUri(
            WGS84Point(35.03935, 135.72926, z = 17.0, source = Source.URI),
            "https://map.yahoo.co.jp/place?gid=xnjNltgGwY6&lat=35.04000&lon=135.72805&zoom=17&maptype=basic", // Page JavaScript changes URL to https://map.yahoo.co.jp/place?gid=xnjNltgGwY6&lat=35.03935&lon=135.72926&zoom=17&maptype=basic
            grantConnectionPermission = true,
        )
    }
}
