package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class OpenStreetMapApiInputTest : InputTest {
    override val resources: Resources = mock()
    private val input = FakeInputRepository.openStreetMapApiInput

    @Test
    fun parse_node() = runTest {
        // language=Json
        val json =
            """{"version":"0.6","elements":[{"type":"node","id":6284640534,"lat":45.4771659,"lon":9.2297918,"timestamp":"2024-03-07T19:04:58Z"}]}"""
        assertEquals(
            ParseResult.Success(persistentListOf(WGS84Point(45.4771659, 9.2297918, source = Source.API))),
            input.parse(json),
        )
    }

    @Test
    fun parse_relation() = runTest {
        @Suppress("GrazieInspectionRunner")
        // language=Json
        val json =
            """{"version":"0.6","generator":"openstreetmap-cgimap 2.1.0 (2942768 spike-08.openstreetmap.org)","copyright":"OpenStreetMap and contributors","attribution":"http://www.openstreetmap.org/copyright","license":"http://opendatacommons.org/licenses/odbl/1-0/","elements":[{"type":"node","id":259609299,"lat":45.4772309,"lon":9.2295862,"timestamp":"2026-09-14T07:44:55Z","version":7,"changeset":188995777,"user":"tiziodcaio","uid":12266546},{"type":"node","id":259609300,"lat":45.4772270,"lon":9.2292124,"timestamp":"2026-09-14T07:44:55Z","version":7,"changeset":188995777,"user":"tiziodcaio","uid":12266546},{"type":"node","id":672396203,"lat":45.4776002,"lon":9.2293737,"timestamp":"2022-03-10T09:33:31Z","version":4,"changeset":118313047,"user":"122131","uid":4841818},{"type":"node","id":730747572,"lat":45.4774959,"lon":9.2295256,"timestamp":"2022-03-10T09:33:31Z","version":3,"changeset":118313047,"user":"122131","uid":4841818},{"type":"node","id":730747575,"lat":45.4774770,"lon":9.2295534,"timestamp":"2022-03-10T09:33:31Z","version":3,"changeset":118313047,"user":"122131","uid":4841818},{"type":"node","id":730747589,"lat":45.4773774,"lon":9.2295549,"timestamp":"2022-03-10T09:33:31Z","version":3,"changeset":118313047,"user":"122131","uid":4841818},{"type":"node","id":730747592,"lat":45.4774959,"lon":9.2293841,"timestamp":"2022-03-10T09:33:31Z","version":3,"changeset":118313047,"user":"122131","uid":4841818},{"type":"node","id":730747596,"lat":45.4773606,"lon":9.2295265,"timestamp":"2022-03-10T09:33:31Z","version":3,"changeset":118313047,"user":"122131","uid":4841818},{"type":"node","id":730747599,"lat":45.4774817,"lon":9.2293589,"timestamp":"2022-03-10T09:33:31Z","version":3,"changeset":118313047,"user":"122131","uid":4841818},{"type":"node","id":730747600,"lat":45.4773591,"lon":9.2293890,"timestamp":"2022-03-10T09:33:31Z","version":3,"changeset":118313047,"user":"122131","uid":4841818},{"type":"node","id":730747602,"lat":45.4773774,"lon":9.2293573,"timestamp":"2022-03-10T09:33:31Z","version":3,"changeset":118313047,"user":"122131","uid":4841818},{"type":"node","id":3183380568,"lat":45.4775994,"lon":9.2290779,"timestamp":"2022-03-10T09:33:31Z","version":3,"changeset":118313047,"user":"122131","uid":4841818},{"type":"node","id":3183380573,"lat":45.4776882,"lon":9.2292067,"timestamp":"2022-03-10T09:33:31Z","version":2,"changeset":118313047,"user":"122131","uid":4841818},{"type":"node","id":3183380576,"lat":45.4775657,"lon":9.2290790,"timestamp":"2022-03-10T09:33:31Z","version":3,"changeset":118313047,"user":"122131","uid":4841818},{"type":"node","id":3183380593,"lat":45.4776890,"lon":9.2292514,"timestamp":"2022-03-10T09:33:31Z","version":2,"changeset":118313047,"user":"122131","uid":4841818},{"type":"node","id":4612338079,"lat":45.4773416,"lon":9.2296803,"timestamp":"2026-09-14T13:28:44Z","version":3,"changeset":189011094,"user":"tiziodcaio","uid":12266546},{"type":"node","id":4612338081,"lat":45.4773421,"lon":9.2297926,"timestamp":"2026-09-14T13:28:44Z","version":4,"changeset":189011094,"user":"tiziodcaio","uid":12266546},{"type":"node","id":5683348856,"lat":45.4775818,"lon":9.2290785,"timestamp":"2025-06-26T14:23:42Z","version":3,"changeset":168142684,"user":"tiziodcaio","uid":12266546,"tags":{"access":"permissive","door":"hinged","entrance":"secondary","wheelchair":"yes"}},{"type":"node","id":5683443077,"lat":45.4773698,"lon":9.2295407,"timestamp":"2022-03-10T09:33:31Z","version":2,"changeset":118313047,"user":"122131","uid":4841818},{"type":"node","id":5683443083,"lat":45.4774878,"lon":9.2293679,"timestamp":"2024-12-04T11:40:14Z","version":3,"changeset":159915454,"user":"comodino","uid":21201574,"tags":{"entrance":"yes"}},{"type":"node","id":14180061345,"lat":45.4770853,"lon":9.2292154,"timestamp":"2026-09-14T07:44:55Z","version":1,"changeset":188995777,"user":"tiziodcaio","uid":12266546},{"type":"node","id":14180061346,"lat":45.4770890,"lon":9.2295891,"timestamp":"2026-09-14T07:44:55Z","version":1,"changeset":188995777,"user":"tiziodcaio","uid":12266546},{"type":"node","id":14180775074,"lat":45.4772357,"lon":9.2296812,"timestamp":"2026-09-14T13:28:44Z","version":1,"changeset":189011094,"user":"tiziodcaio","uid":12266546},{"type":"node","id":14180775075,"lat":45.4772362,"lon":9.2297936,"timestamp":"2026-09-14T13:28:44Z","version":1,"changeset":189011094,"user":"tiziodcaio","uid":12266546},{"type":"node","id":14181164030,"lat":45.4774857,"lon":9.2292097,"timestamp":"2026-09-14T16:46:43Z","version":1,"changeset":189021264,"user":"tiziodcaio","uid":12266546},{"type":"node","id":14181164031,"lat":45.4775156,"lon":9.2297867,"timestamp":"2026-09-14T16:46:43Z","version":1,"changeset":189021264,"user":"tiziodcaio","uid":12266546},{"type":"node","id":14181164032,"lat":45.4774324,"lon":9.2297899,"timestamp":"2026-09-14T16:46:43Z","version":1,"changeset":189021264,"user":"tiziodcaio","uid":12266546},{"type":"node","id":14181164033,"lat":45.4774308,"lon":9.2297068,"timestamp":"2026-09-14T16:46:43Z","version":1,"changeset":189021264,"user":"tiziodcaio","uid":12266546},{"type":"node","id":14181164034,"lat":45.4775140,"lon":9.2297036,"timestamp":"2026-09-14T16:46:43Z","version":1,"changeset":189021264,"user":"tiziodcaio","uid":12266546},{"type":"way","id":58968321,"timestamp":"2018-06-11T23:19:40Z","version":2,"changeset":59756631,"user":"frafra","uid":55024,"nodes":[730747592,730747572,730747575,730747589,5683443077,730747596,730747600,730747602,730747599,5683443083,730747592]},{"type":"way","id":1558770948,"timestamp":"2026-09-14T08:23:44Z","version":2,"changeset":188997294,"user":"tiziodcaio","uid":12266546,"nodes":[259609300,14180061345,14180061346,259609299,259609300],"tags":{"building":"university","building:levels":"2","building:part":"yes","roof:material":"roof_tiles","roof:shape":"gabled"}},{"type":"way","id":1558849962,"timestamp":"2026-09-14T13:28:44Z","version":1,"changeset":189011094,"user":"tiziodcaio","uid":12266546,"nodes":[4612338079,14180775074,14180775075,4612338081,4612338079],"tags":{"building":"university","building:levels":"1","building:part":"yes","roof:colour":"#fff","roof:shape":"flat"}},{"type":"way","id":1558899522,"timestamp":"2026-09-14T16:46:43Z","version":1,"changeset":189021264,"user":"tiziodcaio","uid":12266546,"nodes":[672396203,14181164030,3183380576,5683348856,3183380568,3183380573,3183380593,672396203],"tags":{"area":"yes","building":"university","building:part":"yes","roof:shape":"hipped"}},{"type":"way","id":1558899524,"timestamp":"2026-09-14T16:46:43Z","version":1,"changeset":189021264,"user":"tiziodcaio","uid":12266546,"nodes":[14181164031,14181164032,14181164033,14181164034,14181164031],"tags":{"area":"yes","building":"university","building:part":"yes","roof:shape":"flat"}},{"type":"relation","id":910699,"timestamp":"2026-09-14T16:53:21Z","version":11,"changeset":189021553,"user":"tiziodcaio","uid":12266546,"members":[{"type":"way","ref":58968321,"role":"inner"},{"type":"way","ref":1558770948,"role":"inner"},{"type":"way","ref":1558849962,"role":"inner"},{"type":"way","ref":1558899522,"role":"outer"},{"type":"way","ref":1558899524,"role":"outer"},{"type":"relation","ref":21393086,"role":"outer"}],"tags":{"building":"university","building:levels":"2","name":"Edificio 9 – Giuseppe Bruni","ref":"Edificio 9","type":"multipolygon"}},{"type":"relation","id":21393086,"timestamp":"2026-09-14T16:53:21Z","version":2,"changeset":189021553,"user":"tiziodcaio","uid":12266546,"members":[{"type":"way","ref":23949381,"role":"outer"},{"type":"way","ref":1558899523,"role":"outer"}],"tags":{"area":"yes","building":"university","building:part":"yes","type":"multipolygon"}}]}"""
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(45.4772309, 9.2295862, source = Source.API),
                    WGS84Point(45.4772270, 9.2292124, source = Source.API),
                    WGS84Point(45.4776002, 9.2293737, source = Source.API),
                    WGS84Point(45.4774959, 9.2295256, source = Source.API),
                    WGS84Point(45.4774770, 9.2295534, source = Source.API),
                    WGS84Point(45.4773774, 9.2295549, source = Source.API),
                    WGS84Point(45.4774959, 9.2293841, source = Source.API),
                    WGS84Point(45.4773606, 9.2295265, source = Source.API),
                    WGS84Point(45.4774817, 9.2293589, source = Source.API),
                    WGS84Point(45.4773591, 9.2293890, source = Source.API),
                    WGS84Point(45.4773774, 9.2293573, source = Source.API),
                    WGS84Point(45.4775994, 9.2290779, source = Source.API),
                    WGS84Point(45.4776882, 9.2292067, source = Source.API),
                    WGS84Point(45.4775657, 9.2290790, source = Source.API),
                    WGS84Point(45.4776890, 9.2292514, source = Source.API),
                    WGS84Point(45.4773416, 9.2296803, source = Source.API),
                    WGS84Point(45.4773421, 9.2297926, source = Source.API),
                    WGS84Point(45.4775818, 9.2290785, source = Source.API),
                    WGS84Point(45.4773698, 9.2295407, source = Source.API),
                    WGS84Point(45.4774878, 9.2293679, source = Source.API),
                    WGS84Point(45.4770853, 9.2292154, source = Source.API),
                    WGS84Point(45.4770890, 9.2295891, source = Source.API),
                    WGS84Point(45.4772357, 9.2296812, source = Source.API),
                    WGS84Point(45.4772362, 9.2297936, source = Source.API),
                    WGS84Point(45.4774857, 9.2292097, source = Source.API),
                    WGS84Point(45.4775156, 9.2297867, source = Source.API),
                    WGS84Point(45.4774324, 9.2297899, source = Source.API),
                    WGS84Point(45.4774308, 9.2297068, source = Source.API),
                    WGS84Point(45.4775140, 9.2297036, source = Source.API),
                )
            ),
            input.parse(json),
        )
    }

    @Test
    fun parse_way() = runTest {
        // language=Json
        val json =
            """{"version":"0.6","elements":[{"type":"node","id":5683443079,"lat":45.4770640,"lon":9.2296749,"timestamp":"2023-09-18T20:49:59Z"},{"type":"node","id":5683443080,"lat":45.4771158,"lon":9.2296737,"timestamp":"2023-03-03T21:48:57Z"},{"type":"node","id":5683443081,"lat":45.4771159,"lon":9.2296361,"timestamp":"2018-06-11T23:19:39Z"},{"type":"node","id":5683443082,"lat":45.4772950,"lon":9.2296354,"timestamp":"2018-06-11T23:19:39Z"},{"type":"way","id":596674456,"timestamp":"2025-06-26T19:35:22Z","nodes":[5683443079,5683443080,5683443081,5683443082]}]}"""
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(45.4770640, 9.2296749, source = Source.API),
                    WGS84Point(45.4771158, 9.2296737, source = Source.API),
                    WGS84Point(45.4771159, 9.2296361, source = Source.API),
                    WGS84Point(45.4772950, 9.2296354, source = Source.API),
                )
            ),
            input.parse(json),
        )
    }
}
