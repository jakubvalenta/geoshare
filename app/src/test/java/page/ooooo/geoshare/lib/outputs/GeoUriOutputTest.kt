package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class GeoUriOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val output = GeoUriOutput

    @Test
    fun copyAction_whenPositionDoesNotHaveCoordinates_returnsUriWithZeroCoordinatesInPath() {
        assertEquals(
            "geo:0,0",
            output.getPositionActions().firstNotNullOf { it as? CopyAction }
                .getText(Position(Srs.WGS84), null, uriQuote),
        )
    }

    @Test
    fun copyAction_whenPositionHasCoordinates_returnsUriWithCoordinatesInTheQParam() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456",
            output.getPositionActions().firstNotNullOf { it as? CopyAction }
                .getText(Position(Srs.WGS84, 50.123456, -11.123456), null, uriQuote),
        )
    }

    @Test
    fun copyAction_whenPositionHasCoordinatesAndName_returnsUriWithCoordinatesAndNameInTheQParam() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456(foo%20bar)",
            output.getPositionActions().firstNotNullOf { it as? CopyAction }
                .getText(Position(Srs.WGS84, 50.123456, -11.123456, name = "foo bar"), null, uriQuote),
        )
    }

    @Test
    fun copyOutput_whenPositionHasCoordinatesAndNameAndZoom_returnsUriWithCoordinatesAndNameInTheQParamAndTheZParam() {
        assertEquals(
            "geo:50.123456,-11.123456?z=3.4&q=50.123456,-11.123456(foo%20bar)",
            output
                .getPositionActions().firstNotNullOf { it as? CopyAction }
                .getText(Position(Srs.WGS84, 50.123456, -11.123456, z = 3.4, name = "foo bar"), null, uriQuote),
        )
    }

    @Test
    fun copyAction_whenPositionHasCoordinatesAndQueryAndZoom_returnsUriWithTheQAndZParams() {
        assertEquals(
            "geo:50.123456,-11.123456?z=3.4&q=foo%20bar",
            output.getPositionActions().firstNotNullOf { it as? CopyAction }
                .getText(Position(Srs.WGS84, 50.123456, -11.123456, q = "foo bar", z = 3.4), null, uriQuote),
        )
    }

    @Test
    fun copyOutput_whenPositionHasZoom_returnsUriWithZParamUnlessThePackageNameHasZoomDisabled() {
        assertEquals(
            listOf(
                "com.example.test" to "geo:50.123456,-11.123456?z=3.4&q=50.123456,-11.123456",
                "com.garmin.android.apps.explore" to "geo:50.123456,-11.123456?q=50.123456,-11.123456",
            ),
            Position(Srs.WGS84, 50.123456, -11.123456, z = 3.4).let { position ->
                output.getAppActions(
                    listOf(
                        "com.example.test",
                        "com.garmin.android.apps.explore",
                    ).map { AndroidTools.App(it, AndroidTools.AppType.GEO_URI) }
                )
                    .filter { (_, action) -> action.isEnabled(position, null) }
                    .map { (packageName, action) ->
                        packageName to action.getUriString(position, null, uriQuote)
                    }
            },
        )
    }

    @Test
    fun appAction_whenPositionHasCoordinatesAndName_returnsUriWithCoordinatesAndNameUnlessThePackageNameHasNameDisabled() {
        assertEquals(
            listOf(
                "com.example.test" to "geo:50.123456,-11.123456?q=50.123456,-11.123456(foo%20bar)",
                @Suppress("SpellCheckingInspection")
                "de.schildbach.oeffi" to "geo:50.123456,-11.123456?q=50.123456,-11.123456",
            ),
            Position(Srs.WGS84, 50.123456, -11.123456, name = "foo bar").let { position ->
                output.getAppActions(
                    listOf(
                        "com.example.test",
                        @Suppress("SpellCheckingInspection")
                        "de.schildbach.oeffi",
                    ).map { AndroidTools.App(it, AndroidTools.AppType.GEO_URI) }
                )
                    .filter { (_, action) -> action.isEnabled(position, null) }
                    .map { (packageName, action) ->
                        packageName to action.getUriString(position, null, uriQuote)
                    }
            },
        )
    }

    @Test
    fun appOutput_whenPositionIsInGCJ02AndIsInJapan_returnsUriWithCoordinatesUnchanged() {
        assertEquals(
            "geo:34.5945482,133.7583428?q=34.5945482,133.7583428",
            Position(Srs.GCJ02, 34.5945482, 133.7583428).let { position ->
                output.getAppActions(
                    listOf(
                        "com.example.test",
                    ).map { AndroidTools.App(it, AndroidTools.AppType.GEO_URI) }
                )
                    .firstOrNull { (_, action) -> action.isEnabled(position, null) }
                    ?.second
                    ?.getUriString(position, null, uriQuote)
            }
        )
    }

    @Test
    fun appOutput_whenPositionIsInGCJ02AndIsInChina_returnsUriWithCoordinatesConvertedToWGS84() {
        assertEquals(
            "geo:39.9191328,116.3254076?q=39.9191328,116.3254076",
            Position(Srs.GCJ02, 39.920439, 116.331538).let { position ->
                output.getAppActions(
                    listOf(
                        "com.example.test",
                    ).map { AndroidTools.App(it, AndroidTools.AppType.GEO_URI) }
                )
                    .firstOrNull { (_, action) -> action.isEnabled(position, null) }
                    ?.second
                    ?.getUriString(position, null, uriQuote)
            }
        )
    }

    @Test
    fun appOutput_whenPositionIsInWGS84AndPackageNameRequiresGCJ02_returnsUriWithCoordinatesConvertedToGCJ02() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            listOf(
                "com.example.test" to "geo:31.2304417,121.4709921?q=31.2304417,121.4709921", // WGS 84
                "com.google.android.apps.maps" to "geo:31.2285067,121.475524?q=31.2285067,121.475524", // GCJ-02
                "us.spotco.maps" to "geo:31.2285067,121.475524?q=31.2285067,121.475524", // GCJ-02
                "com.autonavi.minimap" to "geo:31.2285067,121.475524?q=31.2285067,121.475524", // GCJ-02
            ),
            Position(Srs.WGS84, 31.23044166868017, 121.47099209401793).let { position ->
                output.getAppActions(
                    @Suppress("SpellCheckingInspection")
                    listOf(
                        "com.example.test", // WGS 84
                        "com.google.android.apps.maps", // GCJ-02
                        "us.spotco.maps", // GCJ-02
                        "com.autonavi.minimap", // GCJ-02
                    ).map { AndroidTools.App(it, AndroidTools.AppType.GEO_URI) }
                )
                    .filter { (_, action) -> action.isEnabled(position, null) }
                    .map { (packageName, action) -> packageName to action.getUriString(position, null, uriQuote) }
            }
        )
    }
}
