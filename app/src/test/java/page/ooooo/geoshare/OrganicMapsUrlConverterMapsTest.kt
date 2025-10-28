package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.OrganicMapsUrlConverter

class OrganicMapsUrlConverterMapsTest : BaseUrlConverterTest() {
    override val urlConverter = OrganicMapsUrlConverter()

    @Test
    fun isShortUrl_shortLinkOrganicMaps() {
        assertEquals(
            Position("40.7127400", "-74.0059965", z = "9"),
            parseUrl("https://omaps.app/Umse5f0H8a/Nova_Iorque")
        )
        assertEquals(
            Position("40.7127400", "-74.0059965", z = "5"),
            parseUrl("https://comaps.at/Emse5f0H8a/Nova_Iorque")
        )
    }
}
