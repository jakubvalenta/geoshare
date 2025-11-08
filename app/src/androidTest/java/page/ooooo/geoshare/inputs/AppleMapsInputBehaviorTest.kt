package page.ooooo.geoshare.inputs

import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class AppleMapsInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Coordinates
        testUri(
            Position(Srs.WGS84, 52.4804611, 13.4250923),
            "https://maps.apple.com/place?address=Boddinstra%C3%9Fe%2C+Hermannstra%C3%9Fe+36%E2%80%9337%2C+12049+Berlin%2C+Germany&coordinate=52.4804611%2C13.4250923&name=Marked+Location",
        )

        // Coordinates and query
        testUri(
            Position(Srs.WGS84, 52.4890246, 13.4295963),
            "https://maps.apple.com/place?place-id=I1E40915DF4BA1C96&address=Reuterplatz+3,+12047+Berlin,+Germany&coordinate=52.4890246,13.4295963&name=Reuterplatz&_provider=9902",
        )

        // Query
        testUri(
            Position(Srs.WGS84, 50.894967, 4.341626, q = "Central Park", z = 10.0),
            "https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=10&t=s",
        )

        // Place id
        testUri(
            Position(Srs.WGS84, 52.4735927, 13.4050798),
            "https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902",
        )
        testUri(
            Position(Srs.WGS84, 52.4618234, 13.4010092),
            "https://maps.apple.com/place?auid=17017496253231963769&lsp=7618",
        )

        // Map view
        testUri(
            Position(Srs.WGS84, 52.49115540927951, 13.42595574770533),
            "https://maps.apple.com/search?span=0.0076562252877820924,0.009183883666992188&center=52.49115540927951,13.42595574770533",
        )

        // Short URI
        testUri(
            Position(Srs.WGS84, 52.4737758, 13.4373898),
            "https://maps.apple/p/7E-Brjrk_THN14",
        )

        // Text
        testTextUri(
            Position(Srs.WGS84, 52.49115540927951, 13.42595574770533),
            "https://maps.apple.com/?ll=52.49115540927951,13.42595574770533",
        )
    }
}
