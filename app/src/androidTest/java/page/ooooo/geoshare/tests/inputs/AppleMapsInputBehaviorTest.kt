package page.ooooo.geoshare.tests.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.tests.assumeDomainResolvable
import page.ooooo.geoshare.tests.launchApplication
import page.ooooo.geoshare.tests.testText
import page.ooooo.geoshare.tests.testUri
import page.ooooo.geoshare.tests.waitForAppToBeVisible

class AppleMapsInputBehaviorTest : InputBehaviorTest {
    @Test
    fun appleMaps_offline() = uiAutomator {
        // Launch app
        launchApplication()
        waitForAppToBeVisible()

        // Place with coordinates
        testUri(
            WGS84Point(52.4804611, 13.4250923, name = "Marked Location", source = Source.URI),
            "https://maps.apple.com/place?address=Boddinstra%C3%9Fe%2C+Hermannstra%C3%9Fe+36%E2%80%9337%2C+12049+Berlin%2C+Germany&coordinate=52.4804611%2C13.4250923&name=Marked+Location",
        )

        // Place with coordinates within Mainland China
        testUri(
            WGS84Point(39.914972, 116.390850, name = "Forbidden City", source = Source.URI),
            "https://maps.apple.com/place?place-id=I40913A408AA1EC68&address=No.+4+Jingshanqian+Street%2C+Dongcheng+District%2C+Beijing%2C+China&coordinate=39.914972%2C116.390850&name=Forbidden+City&_provider=9902"
        )

        // Place
        testUri(
            WGS84Point(
                52.4890246, 13.4295963,
                name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Reuterplatz",
                source = Source.URI,
            ),
            "https://maps.apple.com/place?place-id=I1E40915DF4BA1C96&address=Reuterplatz+3,+12047+Berlin,+Germany&coordinate=52.4890246,13.4295963&name=Reuterplatz&_provider=9902",
        )

        // Search
        testUri(
            WGS84Point(50.894967, 4.341626, name = "Central Park", z = 10.0, source = Source.MAP_CENTER),
            "https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=10&t=s",
        )

        // Directions
        testUri(
            persistentListOf(
                WGS84Point(
                    name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                    "Hugo de Vrieslaan, Hugo de Vrieslaan Amsterdam Netherlands",
                    source = Source.URI,
                ),
                WGS84Point(
                    52.352443, 4.921501,
                    source = Source.URI,
                ),
                WGS84Point(
                    52.355175, 4.928403,
                    source = Source.URI,
                ),
                WGS84Point(
                    52.351108, 4.935267,
                    source = Source.URI,
                ),
            ),
            "https://maps.apple.com/directions?source=Hugo%20de%20Vrieslaan%2C%20Hugo%20de%20Vrieslaan%20Amsterdam%20Netherlands&waypoint=52.352443%2C4.921501&waypoint=52.355175%2C4.928403&destination=52.351108%2C4.935267&mode=driving",
        )

        // Map center
        testUri(
            WGS84Point(52.49115540927951, 13.42595574770533, source = Source.MAP_CENTER),
            "https://maps.apple.com/search?span=0.0076562252877820924,0.009183883666992188&center=52.49115540927951,13.42595574770533",
        )

        // Text
        testText(
            WGS84Point(52.49115540927951, 13.42595574770533, source = Source.URI),
            "https://maps.apple.com/?ll=52.49115540927951,13.42595574770533",
        )
    }

    @Test
    fun appleMaps_online() = uiAutomator {
        runBlocking {
            assumeDomainResolvable("maps.apple.com")
        }

        // Place id
        testUri(
            WGS84Point(52.4735927, 13.4050798, source = Source.HTML),
            "https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902",
            grantConnectionPermission = true,
        )
        testUri(
            WGS84Point(52.4618234, 13.4010092, source = Source.HTML),
            "https://maps.apple.com/place?auid=17017496253231963769&lsp=7618",
            grantConnectionPermission = true,
        )

        // Short link
        testUri(
            WGS84Point(52.4737758, 13.4373898, source = Source.HTML),
            "https://maps.apple/p/7E-Brjrk_THN14",
            grantConnectionPermission = true,
        )
    }
}
