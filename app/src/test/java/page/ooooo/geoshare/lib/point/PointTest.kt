package page.ooooo.geoshare.lib.point

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote

class PointTest {

    @Test
    fun toGCJ02_whenPointIsWGS84AndDoesNotHaveCoords_returnsGCJ02PointWithoutCoords() {
        assertEquals(
            GCJ02Point(z = 3.14, name = "foo bar", source = Source.GENERATED),
            WGS84Point(z = 3.14, name = "foo bar", source = Source.GENERATED).toGCJ02(),
        )
    }

    @Test
    fun toGCJ02_whenPointIsWGS84AndInChina_returnsGCJ02PointWithConvertedCoords() {
        assertEquals(
            GCJ02Point(31.22281206362763, lon = 121.46840659541449, 3.14, "foo bar", source = Source.GENERATED),
            WGS84Point(
                31.224731304675522,
                lon = 121.46385323166844,
                3.14,
                "foo bar",
                source = Source.GENERATED
            ).toGCJ02(),
        )
    }

    @Test
    fun toGCJ02_whenPointIsWGS84AndNotInChina_returnsGCJ02PointWithUnchangedCoords() {
        assertEquals(
            GCJ02Point(45.8289525077221, 1.266689300537103, 3.14, "foo bar", source = Source.GENERATED),
            WGS84Point(45.8289525077221, 1.266689300537103, 3.14, "foo bar", source = Source.GENERATED).toGCJ02(),
        )
    }

    @Test
    fun toWGS84_whenPointIsGCJ02AndDoesNotHaveCoords_returnsWGS84PointWithoutCoords() {
        assertEquals(
            WGS84Point(z = 3.14, name = "foo bar", source = Source.GENERATED),
            GCJ02Point(z = 3.14, name = "foo bar", source = Source.GENERATED).toWGS84(),
        )
    }

    @Test
    fun toWGS84_whenPointIsGCJ02AndInChina_returnsWGS84PointWithConvertedCoords() {
        assertEquals(
            WGS84Point(31.224731304675522, lon = 121.46385323166844, 3.14, "foo bar", source = Source.GENERATED),
            GCJ02Point(31.222811749011463, 121.46840706467624, 3.14, "foo bar", source = Source.GENERATED).toWGS84(),
        )
    }

    @Test
    fun toWGS84_whenPointIsGCJ02AndNotInChina_returnsWGS84PointWithUnchangedCoords() {
        assertEquals(
            WGS84Point(45.8289525077221, 1.266689300537103, 3.14, "foo bar", source = Source.GENERATED),
            GCJ02Point(45.8289525077221, 1.266689300537103, 3.14, "foo bar", source = Source.GENERATED).toWGS84(),
        )
    }

    @Test
    fun toGCJ02_whenPointIsBD09MCAndDoesNotHaveCoords_returnsGCJ02PointWithoutCoords() {
        assertEquals(
            GCJ02Point(z = 3.14, name = "foo bar", source = Source.GENERATED),
            BD09MCPoint(z = 3.14, name = "foo bar", source = Source.GENERATED).toGCJ02(),
        )
    }

    @Test
    fun toGCJ02_whenPointIsBD09MCAndInChina_returnsGCJ02PointWithConvertedCoords() {
        assertEquals(
            GCJ02Point(28.696786436412197, 121.45032959369264, 3.14, "foo bar", source = Source.GENERATED),
            BD09MCPoint(3317203.0, 13520653.0, 3.14, "foo bar", source = Source.GENERATED).toGCJ02(),
        )
        assertEquals(
            GCJ02Point(28.686779688493015, 121.29095727245614, 3.14, "foo bar", source = Source.GENERATED),
            BD09MCPoint(3315902.2199999997, 13502918.375, 3.14, "foo bar", source = Source.GENERATED).toGCJ02(),
        )
        assertEquals(
            GCJ02Point(23.110319308993134, 113.30138024838311, 3.14, "foo bar", source = Source.GENERATED),
            BD09MCPoint(2629182.88, 12613508.26, 3.14, "foo bar", source = Source.GENERATED).toGCJ02(),
        )
        assertEquals(
            GCJ02Point(23.146380831856163, 113.30063234845544, 3.14, "foo bar", source = Source.GENERATED),
            BD09MCPoint(2633524.681382545, 12613424.449999997, 3.14, "foo bar", source = Source.GENERATED).toGCJ02(),
        )
    }

    @Test
    fun formatUriString_whenPointHasCoordinatesAndZoomAndName_returnsCoordsTemplateWithFilledVariables() {
        assertEquals(
            "https://maps.apple.com/?ll=50.123456%2C-11.123456&z=3.4&q=foo%20bar",
            WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED)
                .formatUriString(
                    coordsUriTemplate = "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                    uriQuote = FakeUriQuote,
                ),
        )
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=50.123456%2C-11.123456",
            WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED)
                .formatUriString(
                    coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                    uriQuote = FakeUriQuote,
                ),
        )
    }

    @Test
    fun formatUriString_whenPointHasCoordinatesAndZoomAndNameAndTemplateIsMissingVariables_returnsCoordsTemplateUnchanged() {
        assertEquals(
            "https://maps.apple.com/",
            WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED)
                .formatUriString(
                    coordsUriTemplate = "https://maps.apple.com/",
                    uriQuote = FakeUriQuote,
                ),
        )
    }

    @Test
    fun formatUriString_whenPointHasCoordinatesOnly_returnsCoordsTemplateWithEmptyNameVariableAndDefaultZoomVariable() {
        assertEquals(
            "https://maps.apple.com/?ll=50.123456%2C-11.123456&z=16&q=",
            WGS84Point(50.123456, -11.123456, source = Source.GENERATED)
                .formatUriString(
                    coordsUriTemplate = "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                    uriQuote = FakeUriQuote,
                ),
        )
    }

    @Test
    fun formatUriString_whenPointHasNameAndZoomOnly_returnsNameTemplateWithFilledQueryVariableAndZoomVariableUnchanged() {
        assertEquals(
            "https://maps.apple.com/?q=foo%20bar&z={z}",
            WGS84Point(name = "foo bar", z = 3.4, source = Source.GENERATED)
                .formatUriString(
                    coordsUriTemplate = "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                    nameUriTemplate = "https://maps.apple.com/?q={q}&z={z}",
                    uriQuote = FakeUriQuote,
                ),
        )
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=foo%20bar",
            WGS84Point(name = "foo bar", z = 3.4, source = Source.GENERATED)
                .formatUriString(
                    coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                    nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                    uriQuote = FakeUriQuote,
                ),
        )
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=foo%20bar",
            WGS84Point(lat = 50.123456, name = "foo bar", z = 3.4, source = Source.GENERATED)
                .formatUriString(
                    coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                    nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                    uriQuote = FakeUriQuote,
                ),
        )
    }

    @Test
    fun formatUriString_whenPointIsWGS84AndInChinaAndSrsIsGCJ02_returnsCoordsTemplateWithConvertedCoords() {
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=31.2285067%2C121.475524",
            WGS84Point(31.23044166868017, 121.47099209401793, source = Source.GENERATED)
                .formatUriString(
                    coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                    nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                    srs = Srs.GCJ02,
                    uriQuote = FakeUriQuote,
                ),
        )
    }

    @Test
    fun formatUriString_whenPointIsGCJ02AndInChinaAndSrsIsGCJ02_returnsCoordsTemplateWithCoordsUnchanged() {
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=31.2285069%2C121.4755246",
            GCJ02Point(31.22850685422705, 121.47552456472106, source = Source.GENERATED)
                .formatUriString(
                    coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                    nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                    srs = Srs.GCJ02,
                    uriQuote = FakeUriQuote,
                ),
        )
    }

    @Test
    fun formatUriString_whenPointIsEmpty_returnsNull() {
        assertNull(
            WGS84Point(source = Source.GENERATED)
                .formatUriString(
                    coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                    nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                    uriQuote = FakeUriQuote,
                )
        )
    }

    @Test
    fun formatUriString_whenPointHasCoordsAndNameAndCoordsTemplateIsEmpty_returnsNameTemplate() {
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=foo%20bar",
            WGS84Point(50.123456, -11.123456, name = "foo bar", source = Source.GENERATED)
                .formatUriString(
                    coordsUriTemplate = "",
                    nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                    uriQuote = FakeUriQuote,
                )
        )
    }

    @Test
    fun formatUriString_whenPointHasCoordsOnlyAndCoordsTemplateIsEmpty_returnsNull() {
        assertNull(
            WGS84Point(50.123456, -11.123456, source = Source.GENERATED)
                .formatUriString(
                    coordsUriTemplate = "",
                    nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
                    uriQuote = FakeUriQuote,
                )
        )
    }

    @Test
    fun formatUriString_whenPointHasNameOnlyAndNameTemplateIsEmpty_returnsNull() {
        assertNull(
            WGS84Point(name = "foo bar", z = 3.4, source = Source.GENERATED)
                .formatUriString(
                    coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                    nameUriTemplate = "",
                    uriQuote = FakeUriQuote,
                )
        )
    }

    @Test
    fun formatUriString_whenBothTemplatesAreEmpty_returnsNull() {
        assertNull(
            WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4, source = Source.GENERATED)
                .formatUriString(
                    coordsUriTemplate = "",
                    nameUriTemplate = "",
                    uriQuote = FakeUriQuote,
                )
        )
    }
}
