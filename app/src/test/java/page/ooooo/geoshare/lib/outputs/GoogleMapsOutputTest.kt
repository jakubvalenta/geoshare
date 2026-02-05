package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.point.GCJ02Point
import page.ooooo.geoshare.lib.point.WGS84Point

class GoogleMapsOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val output = GoogleMapsOutput

    @Test
    fun copyAction_whenLastPointHasCoordinatesAndZoom_returnsLinkWithCoordinatesAsQueryAndZoom() {
        assertEquals(
            listOf(
                "https://www.google.com/maps?q=50.123456,-11.123456&z=3.4",
                "google.navigation:q=50.123456,-11.123456",
                @Suppress("SpellCheckingInspection")
                "google.streetview:cbll=50.123456,-11.123456",
            ),
            persistentListOf(WGS84Point(50.123456, -11.123456, z = 3.4)).let { points ->
                output.getPointsActions()
                    .filter { it.isEnabled(points, null) }
                    .map { it.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_whenLastPointHasQueryAndZoom_returnsLinkWithQueryAndZoomAndNoStreetView() {
        assertEquals(
            listOf(
                "https://www.google.com/maps?q=foo%20bar&z=3.4",
                "google.navigation:q=foo+bar",
            ),
            persistentListOf(WGS84Point(name = "foo bar", z = 3.4)).let { points ->
                output.getPointsActions()
                    .filter { it.isEnabled(points, null) }
                    .map { it.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_whenLastPointHasCoordinatesAndQueryAndZoom_returnsLinkWithCoordinatesAndZoom() {
        assertEquals(
            listOf(
                "https://www.google.com/maps?q=50.123456,-11.123456&z=3.4",
                "google.navigation:q=50.123456,-11.123456",
                @Suppress("SpellCheckingInspection")
                "google.streetview:cbll=50.123456,-11.123456",
            ),
            persistentListOf(WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4)).let { points ->
                output.getPointsActions()
                    .filter { it.isEnabled(points, null) }
                    .map { it.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_whenLastPointIsInChinaAndInWGS84_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            listOf(
                "https://www.google.com/maps?q=31.2285067,121.475524",
                "google.navigation:q=31.2285067,121.475524",
                @Suppress("SpellCheckingInspection")
                "google.streetview:cbll=31.2285067,121.475524",
            ),
            persistentListOf(WGS84Point(31.23044166868017, 121.47099209401793)).let { points ->
                output.getPointsActions()
                    .filter { it.isEnabled(points, null) }
                    .map { it.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_whenLastPointIsInChinaAndInGCJ02_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            listOf(
                "https://www.google.com/maps?q=31.2285069,121.4755246",
                "google.navigation:q=31.2285069,121.4755246",
                @Suppress("SpellCheckingInspection")
                "google.streetview:cbll=31.2285069,121.4755246",
            ),
            persistentListOf(GCJ02Point(31.22850685422705, 121.47552456472106)).let { points ->
                output.getPointsActions()
                    .filter { it.isEnabled(points, null) }
                    .map { it.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_whenLastPointHasNeitherPointNorQuery_returnsEmptyLinks() {
        assertEquals(
            listOf(
                "https://www.google.com/maps",
                "google.navigation:q=0,0",
            ),
            persistentListOf<WGS84Point>().let { points ->
                output.getPointsActions()
                    .filter { it.isEnabled(points, null) }
                    .map { it.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_whenPointHasCoordinates_returnsLinkWithCoordinatesAsQuery() {
        assertEquals(
            listOf(
                "https://www.google.com/maps?q=50.123456,-11.123456",
                "google.navigation:q=50.123456,-11.123456",
                @Suppress("SpellCheckingInspection")
                "google.streetview:cbll=50.123456,-11.123456",
                "google.navigation:q=50.123456,-11.123456",
                @Suppress("SpellCheckingInspection")
                "google.streetview:cbll=50.123456,-11.123456",
            ),
            persistentListOf(
                WGS84Point(50.12, -11.12),
                WGS84Point(50.123456, -11.123456),
                WGS84Point(50.12, -11.12),
            ).let { points ->
                output.getPointActions()
                    .filter { it.isEnabled(points, 1) }
                    .map {
                        when (it) {
                            is CopyAction -> it.getText(points, 1, uriQuote)
                            is OpenChooserAction -> it.getUriString(points, 1, uriQuote)
                            else -> ""
                        }
                    }
            },
        )
    }

    @Test
    fun copyAction_whenPointIsInChinaAndInWGS84_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            listOf(
                "https://www.google.com/maps?q=31.2285067,121.475524",
                "google.navigation:q=31.2285067,121.475524",
                @Suppress("SpellCheckingInspection")
                "google.streetview:cbll=31.2285067,121.475524",
                "google.navigation:q=31.2285067,121.475524",
                @Suppress("SpellCheckingInspection")
                "google.streetview:cbll=31.2285067,121.475524",
            ),
            persistentListOf(
                WGS84Point(31.23, 121.47),
                WGS84Point(31.23044166868017, 121.47099209401793),
                WGS84Point(31.23, 121.47),
            ).let { points ->
                output.getPointActions()
                    .filter { it.isEnabled(points, 1) }
                    .map {
                        when (it) {
                            is CopyAction -> it.getText(points, 1, uriQuote)
                            is OpenChooserAction -> it.getUriString(points, 1, uriQuote)
                            else -> ""
                        }
                    }
            },
        )
    }

    @Test
    fun copyAction_whenPointIsInChinaAndInGCJ02_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            listOf(
                "https://www.google.com/maps?q=31.2285069,121.4755246",
                "google.navigation:q=31.2285069,121.4755246",
                @Suppress("SpellCheckingInspection")
                "google.streetview:cbll=31.2285069,121.4755246",
                "google.navigation:q=31.2285069,121.4755246",
                @Suppress("SpellCheckingInspection")
                "google.streetview:cbll=31.2285069,121.4755246",
            ),
            persistentListOf(
                GCJ02Point(31.22, 121.47),
                GCJ02Point(31.22850685422705, 121.47552456472106),
                GCJ02Point(31.22, 121.47),
            )
                .let { points ->
                    output.getPointActions()
                        .filter { it.isEnabled(points, 1) }
                        .map {
                            when (it) {
                                is CopyAction -> it.getText(points, 1, uriQuote)
                                is OpenChooserAction -> it.getUriString(points, 1, uriQuote)
                                else -> ""
                            }
                        }
                },
        )
    }
}
