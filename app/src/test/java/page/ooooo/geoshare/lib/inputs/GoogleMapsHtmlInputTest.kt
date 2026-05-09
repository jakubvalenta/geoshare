package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source

class GoogleMapsHtmlInputTest : InputTest {
    private val input = GoogleMapsHtmlInput

    @Test
    fun parse_link() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(44.4490541, 26.0888398, source = Source.JAVASCRIPT)
                )
            ),
            input.parse(
                @Suppress("SpellCheckingInspection")
                """<html>
<head>
  <script>
    (function(){var kEI='_H8_Z8OhH_Pr7_UP-_zakAI';window.APP_OPTIONS=[];window.APP_INITIALIZATION_STATE=[[[2848.2250748641354,26.088839800000002,44.449054100000005],[0,0,0],[1024,768],13.1],[[["m",[17,75028,47427],13,[713465651,713465651,713465651,713465663,713465663,713465663,713465663,713465663,7134",null,\"EvgDKYQi49-NlUMIDwAAAAEAAAMAAAAAAAAAAAAABBABAAAAAA\",null,null,[[[1,91]],1,null,69,24]],null,\"Calea Victoriei 202 București, Bucuresti 010098, Roemenië\",null,null,\"https://www.google.com/maps/preview/place/RAI+-+Romantic+%26+Intimate,+Calea+Victoriei+202+Bucure%C8%99ti,+Bucuresti+010098,+Roemeni%C3%AB/@44.4490541,26.0888398,2848a,13.1y/data\\u003d!4m2!3m1!1s0x40b1ffed911b9fcf:0x7394a7e7855d3929\",1,null,null,[[[\"AF1QipOa7QuHeDgFndxr0ak-_H\",10,12,\"\",null,590.6734,[\"https://lh5.googleusercontent.com/p/AF1QipOa7QuHedw203-h152-k-no\",\"RAI - Romantic \\u0026 Intimate\",[4624,3468],[203,100]],null,[[3,26.088l,null,[\"en\"],[[\"Recently opened for business\",null,[0,28]]]],[null,null,null,null,null,\"https://business.google.ckpRMEZuU1VOd2RqaG1ZbGQ6QClEWFhOJxUdI7F4YqrX\\u0026entry\\u003dugca\"],[null,0,null,[\"https://www.google.com/maps/@/data\\u003d!4m7!23m6!1m5!1sChdDSUhNMG5d3929!3m1!1s2@1:CIHM0ogKEICAgpwM%7C?hl\\u003dnl"],[900,900]],null,["sc2","per","mo","b"]]]];
  </script>
</head>
<body></body>
</html>
"""
            ),
        )
    }

    @Test
    fun parse_appInitializationStateOnly() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(52.484201500000005, 13.416727700000001, source = Source.JAVASCRIPT)
                )
            ),
            input.parse(
                """/div\u003e\u003c/div\u003e\u003c/div\u003e"]],0];window.APP_INITIALIZATION_STATE=[[[2429.720134961757,13.416727700000001,52.484201500000005],[0,0,0],[1024,768],13.1],[[["m",[17,70414,43002]"""
            ),
        )
    }

    @Test
    fun parse_whenHtmlContainsGenericMetaTagAndAppInitState_returnsNextInput() = runTest {
        assertEquals(
            ParseResult(nextInput = GoogleMapsWebViewInput),
            input.parse(
                @Suppress("SpellCheckingInspection")
                """<head><meta content="Google Maps" itemprop="name">
    <script>window.APP_INITIALIZATION_STATE=[[[2476371.7645101217, 4.9274546, 52.6901019],[0, 0, 0],[1024, 768],13.1]""",
                "https://www.google.com/maps/place/Berlin,+Germany/"
            ),
        )
    }

    @Test
    fun parse_directionsPreview() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(43.7481, -79.6332, source = Source.HTML)
                )
            ),
            input.parse(
                @Suppress("SpellCheckingInspection")
                """<head><link href="/maps/preview/directions?authuser=0&amp;hl=cs&amp;gl=cz&amp;pb=%211m0%211m4%213m2%213d43.7481%214d-79.6332%216e2%213m12%211m3%211d1311445.7816005738%212d14.4656836%213d50.05974015%212m3%211f0.0%212f0.0%213f0.0%213m2%211i1024%212i768%214f13.1%216m55%211m5%2118b1%2130b1%2131m1%211b1%2134e1%212m4%215m1%216e2%2120e3%2139b1%216m26%2149b1%2163m0%2166b1%2174i150000%2185b1%2191b1%21114b1%21149b1%21206b1%21209b1%21212b1%21216b1%21222b1%21223b1%21232b1%21234b1%21235b1%21239b1%21244b1%21246b1%21250b1%21253b1%21258b1%21260b1%21266b1%21268b1%2110b1%2112b1%2113b1%2114b1%2116b1%2117m1%213e1%2120m5%211e6%212e1%215e2%216b1%2114b1%2146m1%211b0%2196b1%2199b1%2115m3%211sf1LOaYSxN4G-i-gPtfbPkQQ%217e81%2115i10142%2120m28%211m6%211m2%211i0%212i0%212m2%211i530%212i768%211m6%211m2%211i974%212i0%212m2%211i1024%212i768%211m6%211m2%211i0%212i0%212m2%211i1024%212i20%211m6%211m2%211i0%212i748%212m2%211i1024%212i768%2127b1%2140i773%2147m2%218b1%2110e2" as="fetch" crossorigin="" rel="preload">
[null,null,43.7481,-79.6332]...[null,[-79.6332,43.7481]"""
            ),
        )
    }

    @Test
    fun parse_placeList() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(59.1293656, 11.4585672, source = Source.JAVASCRIPT),
                    GCJ02MainlandChinaPoint(59.4154007, 11.659710599999999, source = Source.JAVASCRIPT),
                    GCJ02MainlandChinaPoint(59.147731699999994, 11.550661199999999, source = Source.JAVASCRIPT),
                )
            ),
            input.parse(
                @Suppress("SpellCheckingInspection")
                """<html>
<head>
  <script>
    window.APP_INITIALIZATION_STATE=[[[2503344.9559356705,5.2793702499999995,52.21299185]]];
    (function(){var kEI='6LdIZ6T8G6Tt7_UP2uiGWQ';window.APP_OPTIONS=["[[[\"mfmnkPs6RuGyp0HOmXLSKg\",1,null,1,1],4,[2,1,\"https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg\"],[\"Lea Luithle\",\"https://lh3.googleusercontent.com/\"],\"RW24 Vortour\",\"\",null,null,[[null,[null,null,\"\",null,\"\",[null,null,59.1293656,11.4585672],[\"5063292292208158623\",\"-8209508828443931886\"]],\"The Ford\",\"\",null,null,null,[],[[1],[\"5063292292208158623\",\"-8209508828443931886\"]],[1718363560,896197000],[1718363560,896197000],null,[\"Lea Luithle\",\"https://lh3.googleusercontent.com/\"]],[null,[null,null,\"\",null,\"\",[null,null,59.4154007,11.659710599999999],[\"5063044840345374559\",\"-4449205876271392420\"]],\"Kirkebyøya\",\"\",null,null,null,[],[[1],[\"5063044840345374559\",\"-4449205876271392420\"]],[1718287605,621247000],[1718287605,621247000],null,[\"Lea Luithle\",\"https://lh3.googleusercontent.com/\"]],[null,[null,null,\"\",null,\"\",[null,null,59.147731699999994,11.550661199999999],[\"5063299310147010735\",\"8191888888476196124\"]],\"Gapahuk\",\"\",null,null,null,[],[[1],[\"5063299310147010735\",\"8191888888476196124\"]],[1718284910,232913000],[1718284910,232913000],null,[\"Lea Luithle\",\"https://lh3.googleusercontent.com/\"]]],[null,null,null,[21,\"21\"]],[1718284909,570276000],[1718729026,705637000],8],\"\",null,null,null,null,\"MjQyNDM2NjAyNg\\u003d\\u003d\"]"];window.ES5DGURL='/maps/@/data\x3d!3m1!4b1!4m2!11m1!2smfmmXLSKg?g_ep\x3dCAISDT0ICREU%3D\x26ucbcb\x3d1\x26dg\x3des5';
  </script>
</head>
<body></body>
</html>
"""
            ),
        )
    }

    @Test
    fun parse_placeListHighPrecision() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(5.5592846, -0.19743059999999998, source = Source.JAVASCRIPT)
                )
            ),
            input.parse(
                """ll,"Ghana",null,"",[null,null,5.5592846,-0.19743059999999998],["1143791729983858547","-469"""
            ),
        )
    }

    @Test
    fun parse_myMaps() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(52.49016, 13.434500000000071, source = Source.JAVASCRIPT),
                    GCJ02MainlandChinaPoint(52.49534999999999, 13.431890000000067, source = Source.JAVASCRIPT),
                    GCJ02MainlandChinaPoint(52.4901894, 13.433825899999988, source = Source.JAVASCRIPT),
                    GCJ02MainlandChinaPoint(52.4898201, 13.433602800000017, source = Source.JAVASCRIPT),
                    GCJ02MainlandChinaPoint(52.4960741, 13.435130399999935, source = Source.JAVASCRIPT),
                    GCJ02MainlandChinaPoint(52.4961778, 13.422070500000018, source = Source.JAVASCRIPT),
                    GCJ02MainlandChinaPoint(52.49514559999999, 13.423243800000023, source = Source.JAVASCRIPT),
                    GCJ02MainlandChinaPoint(52.497884, 13.429134, source = Source.JAVASCRIPT),
                    GCJ02MainlandChinaPoint(52.4957432, 13.43344819999993, source = Source.JAVASCRIPT),
                )
            ),
            input.parse(
                @Suppress("SpellCheckingInspection")
                """<html>
<head>
  <script>
    var _pageData ='["mf.map","XXX","YYY",null,[13.422070500000018,52.497884,13.435130399999935,52.4898201],[13.422070500000018,52.497884,13.435130399999935,52.4898201],[[null,"ZZZ","Untitled layer","",[[["https://mt.googleapis.com/vt/icon/WWW"],null,2,null,null,null,[[[],null,1,1,[[null,[52.49016,13.434500000000071]],"0",null,"ZZZ",[52.49016,13.434500000000071],[0,-32],"131985DC295DD490"],[["Julini (Mo closed)"]]],[[],null,1,1,[[null,[52.49534999999999,13.431890000000067]],"0",null,"ZZZ",[52.49534999999999,13.431890000000067],[0,-32],"1319C47BA60D18F7"],[["Bastard (We closed)"]]],[[],null,1,1,[[null,[52.4901894,13.433825899999988]],"0",null,"ZZZ",[52.4901894,13.433825899999988],[0,-32],"131987AEE949800C"],[["Dhaba Mira (always open)"]]],[[],null,1,1,[[null,[52.4898201,13.433602800000017]],"0",null,"ZZZ",[52.4898201,13.433602800000017],[0,-32],"131988132665DFBF"],[["bistro le johnn rose (Tu, We closed)"]]],[[],null,1,1,[[null,[52.4960741,13.435130399999935]],"0",null,"ZZZ",[52.4960741,13.435130399999935],[0,-32],"1319C5B17588C5F8"],[["Chai Wallahs (Mo closed)"]]],[[],null,1,1,[[null,[52.4961778,13.422070500000018]],"0",null,"ZZZ",[52.4961778,13.422070500000018],[0,-32],"1319C615D1E739DC"],[["Cocolo Ramen X-berg (Always open)"]]],[[],null,1,1,[[null,[52.49514559999999,13.423243800000023]],"0",null,"ZZZ",[52.49514559999999,13.423243800000023],[0,-32],"1319C6F30D3FCB8B"],[["Turkish Market (only Tu \\u0026 Fr open)"]]],[[],null,1,1,[[null,[52.497884,13.429134]],"0",null,"ZZZ",[52.497884,13.429134],[0,-32],"1319DB5C191B8889"],[["Thai ?? (always open)"]]],[[],null,1,1,[[null,[52.4957432,13.43344819999993]],"0",null,"ZZZ",[52.4957432,13.43344819999993],[0,-32],"1319DEA4DBC747EB"],[["Facciola (Mo closed)"]]]]]],null,null,true,null,null,null,null,[["ZZZ",1,null,null,null,"https://www.google.com/maps/d/kml?mid\\u003dXXX\\u0026resourcekey\\u0026lid\\u003dZZZ",null,null,null,null,null,2,null,[[["131985DC295DD490",[[[52.49016,13.434500000000071]]],null,null,0,[["name",["Julini (Mo closed)"],1],null,null,null,[null,"ChIJ8as4g7FPqEcRvbQWq1Da5VI",true]],[1,["Julini (Mo closed)"]],0],["1319C47BA60D18F7",[[[52.49534999999999,13.431890000000067]]],null,null,0,[["name",["Bastard (We closed)"],1],null,null,null,[null,"ChIJ8Usso7RPqEcRWM6jtBzsVQo",true]],[1,["Bastard (We closed)"]],1],["131987AEE949800C",[[[52.4901894,13.433825899999988]]],null,null,0,[["name",["Dhaba Mira (always open)"],1],null,null,null,[null,"ChIJmwQFd7FPqEcRQKVgK7DSgms",true]],[1,["Dhaba Mira (always open)"]],2],["131988132665DFBF",[[[52.4898201,13.433602800000017]]],null,null,0,[["name",["bistro le johnn rose (Tu, We closed)"],1],["description",["French"],1],null,null,[null,"ChIJOxwYdLFPqEcRhSYR5xtYs5E",true]],[1,["bistro le johnn rose (Tu, We closed)"]],3],["1319C5B17588C5F8",[[[52.4960741,13.435130399999935]]],null,null,0,[["name",["Chai Wallahs (Mo closed)"],1],null,null,null,[null,"ChIJG6FYNbNPqEcR6WL2iInmu2Q",true]],[1,["Chai Wallahs (Mo closed)"]],4],["1319C615D1E739DC",[[[52.4961778,13.422070500000018]]],null,null,0,[["name",["Cocolo Ramen X-berg (Always open)"],1],null,null,null,[null,"ChIJCUmw4MpPqEcRjuEofFrRt0M",true]],[1,["Cocolo Ramen X-berg (Always open)"]],5],["1319C6F30D3FCB8B",[[[52.49514559999999,13.423243800000023]]],null,null,0,[["name",["Turkish Market (only Tu \\u0026 Fr open)"],1],null,null,null,[null,"ChIJS1PcnLVPqEcRHoOu9kxrOYw",true]],[1,["Turkish Market (only Tu \\u0026 Fr open)"]],6],["1319DB5C191B8889",[[[52.497884,13.429134]]],null,null,0,[["name",["Thai ?? (always open)"],1],null,null,null,[null,"ChIJQ1Fdw0pOqEcRf3-IUw6QmTE",false]],[1,["Thai ?? (always open)"]],7],["1319DEA4DBC747EB",[[[52.4957432,13.43344819999993]]],null,null,0,[["name",["Facciola (Mo closed)"],1],null,null,null,[null,"ChIJp1eUVbNPqEcReoQxg5fUiIo",true]],[1,["Facciola (Mo closed)"]],8]],[[["https://mt.googleapis.com/vt/icon/WWW",[16,32]],[["DB4436",1],1200],[["DB4436",0.2980392156862745],["DB4436",1],1200]]]]]],null,null,null,null,null,4]],[2],null,null,"mapspro_in_drive","XXX",';
  </script>
</head>
<body></body>
</html>
"""
            ),
        )
    }

    @Test
    fun parse_placeListOnePoint() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(59.1293656, 11.4585672, source = Source.JAVASCRIPT)
                )
            ),
            input.parse(
                @Suppress("SpellCheckingInspection")
                """<html>
<head>
  <script>
    (function(){var kEI='6LdIZ6T8G6Tt7_UP2uiGWQ';window.APP_OPTIONS=["[[[\"mfmnkPs6RuGyp0HOmXLSKg\",1,null,1,1],4,[2,1,\"https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg\"],[\"Lea Luithle\",\"https://lh3.googleusercontent.com/\"],\"RW24 Vortour\",\"\",null,null,[[null,[null,null,\"\",null,\"\",[null,null,59.1293656,11.4585672],[\"5063292292208158623\",\"-8209508828443931886\"]]';
  </script>
</head>
<body></body>
</html>
"""
            ),
        )
    }

    @Test
    fun parse_htmlDoesNotMatch_returnsNextInput() = runTest {
        assertEquals(
            ParseResult(nextInput = GoogleMapsWebViewInput),
            input.parse("spam", "https://www.google.com/maps/place/Berlin,+Germany/")
        )
    }

    @Test
    fun parse_googleSearchHtmlDoesNotContainUrl_returnsNextInput() = runTest {
        assertEquals(
            ParseResult(nextInput = GoogleMapsWebViewInput),
            input.parse("<html></html>", "https://www.google.com/maps/place/Berlin,+Germany/"),
        )
    }

    @Test
    fun parse_googleSearchHtmlContainsRelativeUrl_returnsNextInputWithAbsoluteUrl() = runTest {
        assertEquals(
            ParseResult(
                nextInput = GoogleMapsUriInput,
                nextMatch = "https://www.google.com/maps/place//data=!4m2!3m1!1s0xc3f7d4e21a00705%3A0xa9ea51361ed84bda?sa=X&ved=2ahUKEwiY7vv80aeKAxU41QIHHSgBOlsQ4kB6BAgHEAA&hl=de&gl=de",
            ),
            input.parse(
                @Suppress("SpellCheckingInspection")
                """<html>
<body>
  <a
    href="/maps/place//data=!4m2!3m1!1s0xc3f7d4e21a00705:0xa9ea51361ed84bda?sa=X&amp;ved=1t:8290&amp;hl=de&amp;gl=de&amp;ictx=111"
    data-url="/maps/place//data=!4m2!3m1!1s0xc3f7d4e21a00705:0xa9ea51361ed84bda?sa=X&amp;ved=2ahUKEwiY7vv80aeKAxU41QIHHSgBOlsQ4kB6BAgHEAA&amp;hl=de&amp;gl=de"
  >Camino del Quinto</a>
</body>
</html>
""",
                "https://www.google.com/search/foo",
            ),
        )
    }

    @Test
    fun parse_googleSearchHtmlContainsAbsoluteUrl_returnsNextInput() = runTest {
        assertEquals(
            ParseResult(
                nextInput = GoogleMapsUriInput,
                nextMatch = "https://www.example.com/foo",
            ),
            input.parse("""<html><a href="" data-url="https://www.example.com/foo"></a></html>"""),
        )
    }

    @Test
    fun parse_googleSearchHtmlContainsInvalidUrl_returnsNextInput() = runTest {
        assertEquals(
            ParseResult(
                nextInput = GoogleMapsUriInput,
                nextMatch = "https://example.com//spam",
            ),
            input.parse("""<html><a href="" data-url="spam"></a></html>"""),
        )
    }
}
