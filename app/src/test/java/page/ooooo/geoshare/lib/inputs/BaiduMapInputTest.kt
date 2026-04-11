package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.BD09MCPoint
import page.ooooo.geoshare.lib.geo.Source

class BaiduMapInputTest : InputTest {
    override val input = BaiduMapInput()

    @Test
    fun uriPattern_fullUrl() {
        assertEquals(
            "https://map.baidu.com/@13520653,3317203,13z",
            getUri("https://map.baidu.com/@13520653,3317203,13z")
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") "https://map.baidu.com/poi/%E9%BB%84%E5%B2%A9%E5%AE%A2%E8%BF%90%E4%B8%AD%E5%BF%83/@13502918.375,3315902.2199999997,16z?uid=fef3b5922f87e66c63180999&info_merge=1&isBizPoi=false&ugc_type=3&ugc_ver=1&device_ratio=2&compat=1&routetype=drive&en_uid=fef3b5922f87e66c63180999&pcevaname=pc4.1&querytype=detailConInfo&da_src=shareurl",
            getUri(
                @Suppress("SpellCheckingInspection") "https://map.baidu.com/poi/%E9%BB%84%E5%B2%A9%E5%AE%A2%E8%BF%90%E4%B8%AD%E5%BF%83/@13502918.375,3315902.2199999997,16z?uid=fef3b5922f87e66c63180999&info_merge=1&isBizPoi=false&ugc_type=3&ugc_ver=1&device_ratio=2&compat=1&routetype=drive&en_uid=fef3b5922f87e66c63180999&pcevaname=pc4.1&querytype=detailConInfo&da_src=shareurl"
            ),
        )
        assertEquals(
            @Suppress("SpellCheckingInspection") """https://map.baidu.com/dir/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E7%99%BD%E4%BA%91%E8%A1%97%E9%81%93%E7%83%9F%E9%9B%A8%E8%B7%AF/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E6%A2%85%E8%8A%B1%E6%9D%91%E8%A1%97%E9%81%93%E6%B3%B0%E5%85%B4%E7%9B%B4%E8%A1%9735%E5%8F%B7/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7/@12614173.165,2630534.5250000004,16z?querytype=nav&c=257&sn=1$$$$12613508.26,2629184.09$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E7%99%BD%E4%BA%91%E8%A1%97%E9%81%93%E7%83%9F%E9%9B%A8%E8%B7%AF$$0$$$$&en=1$$$$12614727.164999995,2631131.0213408465$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E6%A2%85%E8%8A%B1%E6%9D%91%E8%A1%97%E9%81%93%E6%B3%B0%E5%85%B4%E7%9B%B4%E8%A1%9735%E5%8F%B7$$0$$$$$$1$$%20to:1$$$$12611885.88,2631139.59$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7$$0$$$$&sc=257&ec=257+to:257&pn=0&rn=5&mrs=0&version=4&route_traffic=1&sy=0&da_src=shareurl""",
            getUri(
                @Suppress("SpellCheckingInspection") """https://map.baidu.com/dir/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E7%99%BD%E4%BA%91%E8%A1%97%E9%81%93%E7%83%9F%E9%9B%A8%E8%B7%AF/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E6%A2%85%E8%8A%B1%E6%9D%91%E8%A1%97%E9%81%93%E6%B3%B0%E5%85%B4%E7%9B%B4%E8%A1%9735%E5%8F%B7/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7/@12614173.165,2630534.5250000004,16z?querytype=nav&c=257&sn=1$$$$12613508.26,2629184.09$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E7%99%BD%E4%BA%91%E8%A1%97%E9%81%93%E7%83%9F%E9%9B%A8%E8%B7%AF$$0$$$$&en=1$$$$12614727.164999995,2631131.0213408465$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E6%A2%85%E8%8A%B1%E6%9D%91%E8%A1%97%E9%81%93%E6%B3%B0%E5%85%B4%E7%9B%B4%E8%A1%9735%E5%8F%B7$$0$$$$$$1$$%20to:1$$$$12611885.88,2631139.59$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7$$0$$$$&sc=257&ec=257+to:257&pn=0&rn=5&mrs=0&version=4&route_traffic=1&sy=0&da_src=shareurl"""
            ),
        )
        assertEquals(
            "https://map.baidu.com/?shareurl=1&poiShareUid=fef3b5922f87e66c63180999",
            getUri("https://map.baidu.com/?shareurl=1&poiShareUid=fef3b5922f87e66c63180999")
        )
    }

    @Test
    fun uriPattern_shortUrl() {
        assertEquals("https://j.map.baidu.com/0f/tbWk", getUri("https://j.map.baidu.com/0f/tbWk"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertNull(getUri("https://www.example.com/@13520653,3317203,13z"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertEquals(
            "map.baidu.com/@13520653,3317203,13z",
            input.uriPattern.find("ftp://map.baidu.com/@13520653,3317203,13z")?.value,
        )
    }

    @Test
    fun uriPattern_matchesShortLinkInText() {
        assertEquals(
            "https://j.map.baidu.com/64/lqEk",
            getUri(
                uriString = "这里是地图上的点：江苏省苏州市吴中区金庭镇移影桥\n" +
                    "查看详情>>https://j.map.baidu.com/64/lqEk  #百度地图#"
            ),
        )
    }

    @Test
    fun parseUri_center() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(BD09MCPoint(3317203.0, 13520653.0, 13.0, source = Source.MAP_CENTER))),
            parseUri("https://map.baidu.com/@13520653,3317203,13z"),
        )
    }

    @Test
    fun parseUri_point() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    BD09MCPoint(
                        3619117.0, 13392211.0,
                        17.0,
                        name = "地图上的点",
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            parseUri(@Suppress("SpellCheckingInspection") "https://map.baidu.com/poi/%E5%9C%B0%E5%9B%BE%E4%B8%8A%E7%9A%84%E7%82%B9/@13392211,3619117,17z?querytype=share&poiShareId=p8cdf0522067cf66173901fc9e4&da_src=shareurl"),
        )
    }

    @Test
    fun parseUri_sharedPoint() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                webUriString = "https://map.baidu.com/?poiShareId=p8cdf0522067cf66173901fc9e4",
            ),
            parseUri("https://map.baidu.com/?poiShareId=p8cdf0522067cf66173901fc9e4"),
        )
    }

    @Test
    fun parseUri_sharedPointDetail() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                webUriString = "https://map.baidu.com/?newmap=1&s=inf%26uid%3D2c2bd9487c142391100daa62&sharecallbackflag=poiDetailPage",
            ),
            parseUri("https://map.baidu.com/?newmap=1&s=inf%26uid%3D2c2bd9487c142391100daa62&sharecallbackflag=poiDetailPage"),
        )
    }

    @Test
    fun parseUri_place() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    BD09MCPoint(
                        3315902.2199999997, 13502918.375,
                        16.0,
                        name = "黄岩客运中心",
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            parseUri(@Suppress("SpellCheckingInspection") "https://map.baidu.com/poi/%E9%BB%84%E5%B2%A9%E5%AE%A2%E8%BF%90%E4%B8%AD%E5%BF%83/@13502918.375,3315902.2199999997,16z?uid=fef3b5922f87e66c63180999&info_merge=1&isBizPoi=false&ugc_type=3&ugc_ver=1&device_ratio=2&compat=1&routetype=drive&en_uid=fef3b5922f87e66c63180999&pcevaname=pc4.1&querytype=detailConInfo&da_src=shareurl"),
        )
    }

    @Test
    fun parseUri_sharedPlace_returnsSupportsWebParsing() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                webUriString = "https://map.baidu.com/?shareurl=1&poiShareUid=fef3b5922f87e66c63180999",
            ),
            parseUri("https://map.baidu.com/?shareurl=1&poiShareUid=fef3b5922f87e66c63180999"),
        )
    }

    @Test
    fun parseUri_mobilePlaceDetailWithoutCoords_returnsSupportsWebParsing() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                webUriString = "https://map.baidu.com/mobile/webapp/place/detail/qt=inf&uid=p8cdf0522067cf66173901fc9e4/act=read_share&vt=map&da_from=weixin&openna=1"
            ),
            parseUri("https://map.baidu.com/mobile/webapp/place/detail/qt=inf&uid=p8cdf0522067cf66173901fc9e4/act=read_share&vt=map&da_from=weixin&openna=1")
        )
    }

    @Test
    fun parseUri_mobilePlaceDetailWithCoords_returnsSucceeded() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(BD09MCPoint(3619117.0, 13392211.0, source = Source.URI))
            ),
            parseUri("https://map.baidu.com/mobile/webapp/place/detail/qt=inf&uid=p8cdf0522067cf66173901fc9e4/act=read_share&vt=map&da_from=weixin&openna=1&sharegeo=13392211%2C3619117")
        )
    }

    @Test
    fun parseUri_directionsOnePointNoParams() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    BD09MCPoint(
                        name = "广东省广州市越秀区大塘街道中山三路东昌大街2号",
                        source = Source.URI,
                    )
                ),
            ),
            parseUri(
                "https://map.baidu.com/dir/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7/@12614173.165,2630534.5250000004,16z"
            ),
        )
    }

    @Test
    fun parseUri_directionsTwoPoints() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    BD09MCPoint(
                        2629182.88, 12613508.26,
                        name = "广东省广州市越秀区白云街道烟雨路",
                        source = Source.URI,
                    ),
                    BD09MCPoint(
                        2631139.59, 12611885.88,
                        name = "广东省广州市越秀区大塘街道中山三路东昌大街2号",
                        source = Source.URI,
                    ),
                ),
            ),
            parseUri(
                @Suppress("SpellCheckingInspection") "https://map.baidu.com/dir/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E7%99%BD%E4%BA%91%E8%A1%97%E9%81%93%E7%83%9F%E9%9B%A8%E8%B7%AF/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7/@12614173.165,2630534.5250000004,16z?querytype=nav&navtp=2&c=257&drag=1&sc=257&ec=257&sy=0&sn=1$$$$12613508.26,2629182.88$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E7%99%BD%E4%BA%91%E8%A1%97%E9%81%93%E7%83%9F%E9%9B%A8%E8%B7%AF$$$$$$12613508.26,2629182.88$$&en=1$$$$12611885.88,2631139.59$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7$$$$$$12611885.88,2631139.59$$&version=4&mrs=1&route_traffic=1&da_src=shareurl"
            ),
        )
    }

    @Test
    fun parseUri_directionsTwoPointsNoParams() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    BD09MCPoint(name = "广东省广州市越秀区白云街道烟雨路", source = Source.URI),
                    BD09MCPoint(name = "广东省广州市越秀区大塘街道中山三路东昌大街2号", source = Source.URI),
                ),
            ),
            parseUri(
                "https://map.baidu.com/dir/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E7%99%BD%E4%BA%91%E8%A1%97%E9%81%93%E7%83%9F%E9%9B%A8%E8%B7%AF/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7/@12614173.165,2630534.5250000004,16z"
            ),
        )
    }

    @Test
    fun parseUri_directionsThreePoints() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    BD09MCPoint(
                        2629184.09, 12613508.26,
                        name = "广东省广州市越秀区白云街道烟雨路",
                        source = Source.URI,
                    ),
                    BD09MCPoint(
                        2631131.0213408465, 12614727.164999995,
                        name = "广东省广州市越秀区梅花村街道泰兴直街35号",
                        source = Source.URI,
                    ),
                    BD09MCPoint(
                        2631139.59, 12611885.88,
                        name = "广东省广州市越秀区大塘街道中山三路东昌大街2号",
                        source = Source.URI,
                    ),
                )
            ),
            parseUri(
                @Suppress("SpellCheckingInspection") "https://map.baidu.com/dir/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E7%99%BD%E4%BA%91%E8%A1%97%E9%81%93%E7%83%9F%E9%9B%A8%E8%B7%AF/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E6%A2%85%E8%8A%B1%E6%9D%91%E8%A1%97%E9%81%93%E6%B3%B0%E5%85%B4%E7%9B%B4%E8%A1%9735%E5%8F%B7/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7/@12614173.165,2630534.5250000004,16z?querytype=nav&c=257&sn=1$$$$12613508.26,2629184.09$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E7%99%BD%E4%BA%91%E8%A1%97%E9%81%93%E7%83%9F%E9%9B%A8%E8%B7%AF$$0$$$$&en=1$$$$12614727.164999995,2631131.0213408465$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E6%A2%85%E8%8A%B1%E6%9D%91%E8%A1%97%E9%81%93%E6%B3%B0%E5%85%B4%E7%9B%B4%E8%A1%9735%E5%8F%B7$$0$$$$$$1$$%20to:1$$$$12611885.88,2631139.59$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7$$0$$$$&sc=257&ec=257+to:257&pn=0&rn=5&mrs=0&version=4&route_traffic=1&sy=0&da_src=shareurl"
            ),
        )
    }

    @Test
    fun parseUri_directionsThreePointsNoParams() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    BD09MCPoint(name = "广东省广州市越秀区白云街道烟雨路", source = Source.URI),
                    BD09MCPoint(name = "广东省广州市越秀区梅花村街道泰兴直街35号", source = Source.URI),
                    BD09MCPoint(name = "广东省广州市越秀区大塘街道中山三路东昌大街2号", source = Source.URI),
                )
            ),
            parseUri(
                "https://map.baidu.com/dir/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E7%99%BD%E4%BA%91%E8%A1%97%E9%81%93%E7%83%9F%E9%9B%A8%E8%B7%AF/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E6%A2%85%E8%8A%B1%E6%9D%91%E8%A1%97%E9%81%93%E6%B3%B0%E5%85%B4%E7%9B%B4%E8%A1%9735%E5%8F%B7/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7/@12614173.165,2630534.5250000004,16z"
            ),
        )
    }

    @Test
    fun parseUri_directionsFourPoints() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    BD09MCPoint(
                        2629182.88, 12613508.26,
                        name = "广东省广州市越秀区白云街道烟雨路",
                        source = Source.URI,
                    ),
                    BD09MCPoint(
                        2631131.0213408465, 12614727.164999995,
                        name = "广东省广州市越秀区梅花村街道泰兴直街35号",
                        source = Source.URI,
                    ),
                    BD09MCPoint(
                        2633524.681382545, 12613424.449999997,
                        name = "广东省广州市越秀区黄花岗街道永福路36号DE座",
                        source = Source.URI,
                    ),
                    BD09MCPoint(
                        2631139.59, 12611885.88,
                        name = "广东省广州市越秀区大塘街道中山三路东昌大街2号",
                        source = Source.URI,
                    ),
                )
            ),
            parseUri(
                @Suppress("SpellCheckingInspection") "https://map.baidu.com/dir/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E7%99%BD%E4%BA%91%E8%A1%97%E9%81%93%E7%83%9F%E9%9B%A8%E8%B7%AF/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E6%A2%85%E8%8A%B1%E6%9D%91%E8%A1%97%E9%81%93%E6%B3%B0%E5%85%B4%E7%9B%B4%E8%A1%9735%E5%8F%B7/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E9%BB%84%E8%8A%B1%E5%B2%97%E8%A1%97%E9%81%93%E6%B0%B8%E7%A6%8F%E8%B7%AF36%E5%8F%B7DE%E5%BA%A7/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7/@12612741.165,2631232.5250000004,16z?querytype=nav&c=257&sn=1$$$$12613508.26,2629182.88$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E7%99%BD%E4%BA%91%E8%A1%97%E9%81%93%E7%83%9F%E9%9B%A8%E8%B7%AF$$0$$$$&en=1$$$$12614727.164999995,2631131.0213408465$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E6%A2%85%E8%8A%B1%E6%9D%91%E8%A1%97%E9%81%93%E6%B3%B0%E5%85%B4%E7%9B%B4%E8%A1%9735%E5%8F%B7$$0$$$$$$1$$%20to:1$$$$12613424.449999997,2633524.681382545$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E9%BB%84%E8%8A%B1%E5%B2%97%E8%A1%97%E9%81%93%E6%B0%B8%E7%A6%8F%E8%B7%AF36%E5%8F%B7DE%E5%BA%A7$$0$$$$$$1$$%20to:1$$$$12611885.88,2631139.59$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7$$0$$$$&sc=257&ec=257+to:257+to:257&pn=0&rn=5&mrs=0&version=4&route_traffic=1&sy=0&da_src=shareurl"
            ),
        )
    }

    @Test
    fun parseUri_directionsFourPointsNoParams() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    BD09MCPoint(name = "广东省广州市越秀区白云街道烟雨路", source = Source.URI),
                    BD09MCPoint(name = "广东省广州市越秀区梅花村街道泰兴直街35号", source = Source.URI),
                    BD09MCPoint(name = "广东省广州市越秀区黄花岗街道永福路36号DE座", source = Source.URI),
                    BD09MCPoint(name = "广东省广州市越秀区大塘街道中山三路东昌大街2号", source = Source.URI),
                )
            ),
            parseUri(
                "https://map.baidu.com/dir/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E7%99%BD%E4%BA%91%E8%A1%97%E9%81%93%E7%83%9F%E9%9B%A8%E8%B7%AF/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E6%A2%85%E8%8A%B1%E6%9D%91%E8%A1%97%E9%81%93%E6%B3%B0%E5%85%B4%E7%9B%B4%E8%A1%9735%E5%8F%B7/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E9%BB%84%E8%8A%B1%E5%B2%97%E8%A1%97%E9%81%93%E6%B0%B8%E7%A6%8F%E8%B7%AF36%E5%8F%B7DE%E5%BA%A7/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7/@12612741.165,2631232.5250000004,16z"
            ),
        )
    }
}
