package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.position.Point

class GoogleMapsOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val outputGroup = GoogleMapsOutput

    @Test
    fun copyAction_whenPositionHasCoordinatesAndZoom_returnsLinkWithCoordinatesAsQueryAndZoom() {
        assertEquals(
            listOf(
                "https://www.google.com/maps?q=50.123456,-11.123456&z=3.4",
                "google.navigation:q=50.123456,-11.123456",
                @Suppress("SpellCheckingInspection")
                "google.streetview:cbll=50.123456,-11.123456",
            ),
            Position(Srs.WGS84, 50.123456, -11.123456, z = 3.4).let { position ->
                outputGroup.getPositionActions()
                    .filter { it.isEnabled(position, null) }
                    .map { it.getText(position, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_whenPositionHasQueryAndZoom_returnsLinkWithQueryAndZoomAndNoStreetView() {
        assertEquals(
            listOf(
                "https://www.google.com/maps?q=foo%20bar&z=3.4",
                "google.navigation:q=foo+bar",
            ),
            Position(q = "foo bar", z = 3.4).let { position ->
                outputGroup.getPositionActions()
                    .filter { it.isEnabled(position, null) }
                    .map { it.getText(position, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_whenPositionHasCoordinatesAndQueryAndZoom_returnsLinkWithCoordinatesAndZoom() {
        assertEquals(
            listOf(
                "https://www.google.com/maps?q=50.123456,-11.123456&z=3.4",
                "google.navigation:q=50.123456,-11.123456",
                @Suppress("SpellCheckingInspection")
                "google.streetview:cbll=50.123456,-11.123456",
            ),
            Position(Srs.WGS84, 50.123456, -11.123456, q = "foo bar", z = 3.4).let { position ->
                outputGroup.getPositionActions()
                    .filter { it.isEnabled(position, null) }
                    .map { it.getText(position, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_whenPositionIsInChinaAndInWGS84_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            listOf(
                "https://www.google.com/maps?q=31.2285067,121.475524",
                "google.navigation:q=31.2285067,121.475524",
                @Suppress("SpellCheckingInspection")
                "google.streetview:cbll=31.2285067,121.475524",
            ),
            Position(Srs.WGS84, 31.23044166868017, 121.47099209401793).let { position ->
                outputGroup.getPositionActions()
                    .filter { it.isEnabled(position, null) }
                    .map { it.getText(position, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_whenPositionIsInChinaAndInGCJ02_returnsLinkWithCoordinatesInGCJ02() {
        assertEquals(
            listOf(
                "https://www.google.com/maps?q=31.2285069,121.4755246",
                "google.navigation:q=31.2285069,121.4755246",
                @Suppress("SpellCheckingInspection")
                "google.streetview:cbll=31.2285069,121.4755246",
            ),
            Position(Srs.GCJ02, 31.22850685422705, 121.47552456472106).let { position ->
                outputGroup.getPositionActions()
                    .filter { it.isEnabled(position, null) }
                    .map { it.getText(position, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_whenPositionHasNeitherPointNorQuery_returnsEmptyLinks() {
        assertEquals(
            listOf(
                "https://www.google.com/maps",
                "google.navigation:q=0,0",
            ),
            Position().let { position ->
                outputGroup.getPositionActions()
                    .filter { it.isEnabled(position, null) }
                    .map { it.getText(position, null, uriQuote) }
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
            Position(
                persistentListOf(
                    Point(Srs.WGS84, 50.12, -11.12),
                    Point(Srs.WGS84, 50.123456, -11.123456),
                    Point(Srs.WGS84, 50.12, -11.12),
                )
            ).let { position ->
                outputGroup.getPointActions()
                    .filter { it.isEnabled(position, 1) }
                    .map {
                        when (it) {
                            is CopyAction -> it.getText(position, 1, uriQuote)
                            is OpenChooserAction -> it.getUriString(position, 1, uriQuote)
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
            Position(
                persistentListOf(
                    Point(Srs.WGS84, 31.23, 121.47),
                    Point(Srs.WGS84, 31.23044166868017, 121.47099209401793),
                    Point(Srs.WGS84, 31.23, 121.47),
                )
            ).let { position ->
                outputGroup.getPointActions()
                    .filter { it.isEnabled(position, 1) }
                    .map {
                        when (it) {
                            is CopyAction -> it.getText(position, 1, uriQuote)
                            is OpenChooserAction -> it.getUriString(position, 1, uriQuote)
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
            Position(
                persistentListOf(
                    Point(Srs.GCJ02, 31.22, 121.47),
                    Point(Srs.GCJ02, 31.22850685422705, 121.47552456472106),
                    Point(Srs.GCJ02, 31.22, 121.47),
                )
            ).let { position ->
                outputGroup.getPointActions()
                    .filter { it.isEnabled(position, 1) }
                    .map {
                        when (it) {
                            is CopyAction -> it.getText(position, 1, uriQuote)
                            is OpenChooserAction -> it.getUriString(position, 1, uriQuote)
                            else -> ""
                        }
                    }
            },
        )
    }
}
