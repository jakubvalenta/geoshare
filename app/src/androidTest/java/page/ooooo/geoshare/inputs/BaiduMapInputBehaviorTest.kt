package page.ooooo.geoshare.inputs

import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.Position
import page.ooooo.geoshare.lib.point.Srs

class BaiduMapInputBehaviorTest : BaseInputBehaviorTest() {
    @Test
    fun test() {
        // Launch app and close intro
        launchApplication()
        closeIntro()

        // Center
        testUri(
            Position(Srs.BD09MC, 3317203.0, 13520653.0, z = 13.0),
            "https://map.baidu.com/@13520653,3317203,13z",
        )

        // Place
        testUri(
            Position(Srs.BD09MC, 3315902.2199999997, 13502918.375, z = 16.0, name = "黄岩客运中心"),
            @Suppress("SpellCheckingInspection") "https://map.baidu.com/poi/%E9%BB%84%E5%B2%A9%E5%AE%A2%E8%BF%90%E4%B8%AD%E5%BF%83/@13502918.375,3315902.2199999997,16z?uid=fef3b5922f87e66c63180999&info_merge=1&isBizPoi=false&ugc_type=3&ugc_ver=1&device_ratio=2&compat=1&routetype=drive&en_uid=fef3b5922f87e66c63180999&pcevaname=pc4.1&querytype=detailConInfo&da_src=shareurl",
        )

        // Directions
        testUri(
            Position(
                persistentListOf(
                    Point(Srs.BD09MC, 2629182.88, 12613508.26, "广东省广州市越秀区白云街道烟雨路"),
                    Point(
                        Srs.BD09MC,
                        2631131.0213408465,
                        12614727.164999995,
                        "广东省广州市越秀区梅花村街道泰兴直街35号"
                    ),
                    Point(
                        Srs.BD09MC,
                        2633524.681382545,
                        12613424.449999997,
                        "广东省广州市越秀区黄花岗街道永福路36号DE座"
                    ),
                    Point(Srs.BD09MC, 2631139.59, 12611885.88, "广东省广州市越秀区大塘街道中山三路东昌大街2号"),
                ),
            ),
            @Suppress("SpellCheckingInspection") "https://map.baidu.com/dir/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E7%99%BD%E4%BA%91%E8%A1%97%E9%81%93%E7%83%9F%E9%9B%A8%E8%B7%AF/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E6%A2%85%E8%8A%B1%E6%9D%91%E8%A1%97%E9%81%93%E6%B3%B0%E5%85%B4%E7%9B%B4%E8%A1%9735%E5%8F%B7/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E9%BB%84%E8%8A%B1%E5%B2%97%E8%A1%97%E9%81%93%E6%B0%B8%E7%A6%8F%E8%B7%AF36%E5%8F%B7DE%E5%BA%A7/%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7/@12612741.165,2631232.5250000004,16z?querytype=nav&c=257&sn=1$$$$12613508.26,2629182.88$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E7%99%BD%E4%BA%91%E8%A1%97%E9%81%93%E7%83%9F%E9%9B%A8%E8%B7%AF$$0$$$$&en=1$$$$12614727.164999995,2631131.0213408465$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E6%A2%85%E8%8A%B1%E6%9D%91%E8%A1%97%E9%81%93%E6%B3%B0%E5%85%B4%E7%9B%B4%E8%A1%9735%E5%8F%B7$$0$$$$$$1$$%20to:1$$$$12613424.449999997,2633524.681382545$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E9%BB%84%E8%8A%B1%E5%B2%97%E8%A1%97%E9%81%93%E6%B0%B8%E7%A6%8F%E8%B7%AF36%E5%8F%B7DE%E5%BA%A7$$0$$$$$$1$$%20to:1$$$$12611885.88,2631139.59$$%E5%B9%BF%E4%B8%9C%E7%9C%81%E5%B9%BF%E5%B7%9E%E5%B8%82%E8%B6%8A%E7%A7%80%E5%8C%BA%E5%A4%A7%E5%A1%98%E8%A1%97%E9%81%93%E4%B8%AD%E5%B1%B1%E4%B8%89%E8%B7%AF%E4%B8%9C%E6%98%8C%E5%A4%A7%E8%A1%972%E5%8F%B7$$0$$$$&sc=257&ec=257+to:257+to:257&pn=0&rn=5&mrs=0&version=4&route_traffic=1&sy=0&da_src=shareurl",
        )
    }
}
