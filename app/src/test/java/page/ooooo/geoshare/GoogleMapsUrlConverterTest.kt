package page.ooooo.geoshare

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter

class GoogleMapsUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = GoogleMapsUrlConverter()

    @Test
    fun uriPattern_fullUrl() {
        assertTrue(doesUriPatternMatch("https://www.google.com/maps/@52.5067296,13.2599309,6z"))
        assertTrue(doesUriPatternMatch("google.com/maps/@52.5067296,13.2599309,6z"))
    }

    @Test
    fun uriPattern_shortUrl() {
        assertTrue(doesUriPatternMatch("https://maps.app.goo.gl/foo"))
        assertTrue(doesUriPatternMatch("https://app.goo.gl/maps/foo"))
        assertTrue(doesUriPatternMatch("https://g.co/kgs/foo"))
        assertTrue(doesUriPatternMatch("maps.app.goo.gl/foo"))
        assertTrue(doesUriPatternMatch("app.goo.gl/maps/foo"))
        assertTrue(doesUriPatternMatch("g.co/kgs/foo"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/maps/@52.5067296,13.2599309,6z"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertFalse(doesUriPatternMatch("ftp://www.google.com/maps/@52.5067296,13.2599309,6z"))
    }

    @Test
    fun uriPattern_matchesShortUriWithoutQueryString() {
        assertEquals(
            "https://maps.app.goo.gl/foo",
            getUri("https://maps.app.goo.gl/foo?g_st=isi")
        )
    }

    @Test
    fun uriPattern_matchesShortUriWithoutInvalidTrailingCharacters() {
        assertEquals(
            "https://maps.app.goo.gl/jVuuNEZ_-FQ3UGhX7",
            getUri("https://maps.app.goo.gl/jVuuNEZ_-FQ3UGhX7%3C/a%3E%3C/p%3E")
        )
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertEquals(
            Position(),
            parseUrl("https://maps.google.com")
        )
        assertEquals(
            Position(),
            parseUrl("https://maps.google.com/")
        )
        assertEquals(
            Position(),
            parseUrl("https://maps.google.com/?spam=1")
        )
    }

    @Test
    fun parseUrl_unknownPath() {
        assertNull(parseUrl("https://maps.google.com/spam"))
    }

    @Test
    fun parseUrl_coordinatesOnly() {
        assertEquals(
            Position("52.5067296", "13.2599309", z = "6"),
            parseUrl("https://www.google.com/maps/@52.5067296,13.2599309,6z")
        )
    }

    @Test
    fun parseUrl_coordinatesOnlyStreetView() {
        assertEquals(
            Position("53.512825", "57.6891441"),
            @Suppress("SpellCheckingInspection")
            parseUrl("https://www.google.com/maps/@53.512825,57.6891441,0a,75y,90t/data=abc?utm_source=mstt_0&g_ep=def")
        )
    }

    @Test
    fun parseUrl_placeAndPositiveCoordinates() {
        assertEquals(
            Position("52.5067296", "13.2599309", q = "Berlin, Germany", z = "11"),
            parseUrl("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910")
        )
    }

    @Test
    fun parseUrl_placeAndPositiveCoordinatesWithManyDecimalPlaces() {
        assertEquals(
            Position("44.448337599999995", "26.0834555"),
            parseUrl("https://www.google.com/maps/place/Strada+Occidentului+7,+Bucure%C8%99ti,+Romania/data=!4m6!3m5!1s0x40b201fdfa573623:0x4f53bb5ad3fdc97f!7e2!8m2!3d44.448337599999995!4d26.0834555?utm_source=mstt_1&entry=gps&coh=192189&g_ep=abc")
        )
    }

    @Test
    fun parseUrl_placeAndNegativeCoordinates() {
        assertEquals(
            Position("-17.2165721", "-149.9470294", q = "Berlin, Germany", z = "11"),
            parseUrl("https://www.google.com/maps/place/Berlin,+Germany/@-17.2165721,-149.9470294,11z/")
        )
    }

    @Test
    fun parseUrl_placeAndIntegerCoordinates() {
        assertEquals(
            Position("52", "13", q = "Berlin, Germany", z = "11"),
            parseUrl("https://www.google.com/maps/place/Berlin,+Germany/@52,13,11z/")
        )
    }

    @Test
    fun parseUrl_placeAndFractionalZoom() {
        assertEquals(
            Position("52.5067296", "13.2599309", q = "Berlin, Germany", z = "6.33"),
            parseUrl("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,6.33z/")
        )
    }

    @Test
    fun parseUrl_placeAndData() {
        assertEquals(
            Position("40.785091", "-73.968285", q = "Central Park", z = "15"),
            parseUrl("https://www.google.com/maps/place/Central+Park/@40.785091,-73.968285,15z/data=!3m1!4b1!4m5!3m4!1s0x89c2589a018531e3:0xb9df1f3170d990b5!8m2"),
        )
    }

    @Test
    fun parseUrl_placeAndPositiveCoordinatesAndPositiveDataCoordinates() {
        assertEquals(
            Position("44.4490541", "26.0888398", z = "11.42"),
            @Suppress("SpellCheckingInspection")
            parseUrl("https://www.google.com/maps/place/RAI+-+Romantic+%26+Intimate/@44.5190589,25.7489796,11.42z/data=!4m6!3m5!1s0x40b1ffed911b9fcf:0x7394a7e7855d3929!8m2!3d44.4490541!4d26.0888398!16s%2Fg%2F11svmp0zhs")
        )
    }

    @Test
    fun parseUrl_placeAndNegativeCoordinatesAndNegativeDataCoordinates() {
        assertEquals(
            Position("40.785091", "-73.968285", z = "15"),
            parseUrl("https://www.google.com/maps/place/Central+Park/@40.8,-73.9,15z/data=!3m1!4b1!4m5!3m4!1s0x89c2589a018531e3:0xb9df1f3170d990b5!8m2!3d40.785091!4d-73.968285"),
        )
    }

    @Test
    fun parseUrl_placeAndPositiveDataCoordinates() {
        assertEquals(
            Position("44.4490541", "26.0888398"),
            parseUrl("https://www.google.com/maps/place/RAI+-+Romantic+%26+Intimate,+Calea+Victoriei+202+Bucure%C8%99ti,+Bucuresti+010098,+Rom%C3%A2nia/data=!4m6!3m5!1s0x40b1ffed911b9fcf:0x7394a7e7855d3929!8m2!3d44.4490541!4d26.0888398!16s%2Fg%2F11svmp0zhs")

        )
    }

    @Test
    fun parseUrl_placeAsCoordinates() {
        assertEquals(
            Position("52.04", "-2.35", z = "15"),
            parseUrl("https://maps.google.com/maps/place/52.04,-2.35/@52.03877,-2.3416,15z/data=!3m1!1e3")

        )
    }

    @Test
    fun parseUrl_placeAsCoordinatesWithPlus() {
        assertEquals(
            Position("52.492611", "13.431726", z = "17"),
            parseUrl("https://www.google.com/maps/place/52.492611,+13.431726/@52.4929475,13.4317905,17z/data=!4m4!3m3!8m2?force=pwa")
        )
    }

    @Test
    fun parseUrl_placeCoordinatesOnly() {
        assertEquals(
            Position("52.03877", "-2.3416"),
            parseUrl("https://maps.google.com/maps/place/52.03877,-2.3416/data=!3m1!1e3")
        )
    }

    @Test
    fun parseUrl_placeOnly() {
        assertEquals(
            Position(q = "Poznań Old Town, 61-001 Poznań, Poland"),
            parseUrl("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd")
        )
    }

    @Test
    fun parseUrl_placeWithoutName() {
        assertEquals(
            Position(null, null),
            parseUrl("https://www.google.com/maps/place//data=!4m2!3m1!1s0xc3f7d4e21a00705:0xa9ea51361ed84bda")
        )
    }

    @Test
    fun parseUrl_placeList() {
        assertEquals(
            Position(),
            parseUrl("https://www.google.com/maps/placelists/list/abcdef?g_ep=ghijkl%3D&g_st=isi")
        )
    }

    @Test
    fun parseUrl_searchCoordinates() {
        assertEquals(
            Position("48.8584", "2.2945"),
            parseUrl("https://www.google.com/maps/search/48.8584,2.2945")
        )
    }

    @Test
    fun parseUrl_searchPlace() {
        assertEquals(
            Position(q = "restaurants near me"),
            parseUrl("https://www.google.com/maps/search/restaurants+near+me")
        )
    }

    @Test
    fun parseUrl_searchQueryCoordinates() {
        assertEquals(
            Position("47.5951518", "-122.3316393"),
            parseUrl("https://www.google.com/maps/search/?query_place_id=ChIJKxjxuaNqkFQR3CK6O1HNNqY&query=47.5951518,-122.3316393&api=1")
        )
    }

    @Test
    fun parseUrl_searchQueryPlace() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Position(q = "centurylink+field"),
            parseUrl("https://www.google.com/maps/search/?api=1&query=centurylink%2Bfield")
        )
    }

    @Test
    fun parseUrl_parameterQTakesPrecedence() {
        assertEquals(
            Position("40.7128", "-74.0060"),
            parseUrl("https://www.google.com/?q=40.7128,-74.0060&viewpoint=34.0522,-118.2437")
        )
        assertEquals(
            Position("40.7128", "-74.0060"),
            parseUrl("https://www.google.com/?q=40.7128,-74.0060&center=34.0522,-118.2437")
        )
    }

    @Test
    fun parseUrl_parameterDestinationTakesPrecedence() {
        assertEquals(
            Position(q = "Cherbourg,France"),
            parseUrl("https://www.google.com/?destination=Cherbourg,France&q=Paris,France")
        )
        assertEquals(
            Position(q = "Cherbourg,France"),
            parseUrl("https://www.google.com/?destination=Cherbourg,France&query=Paris,France")
        )
    }

    @Test
    fun parseUrl_directionsCoordinates() {
        assertEquals(
            Position("34.0522", "-118.2437"),
            parseUrl("https://www.google.com/maps/dir/40.7128,-74.0060/34.0522,-118.2437")
        )
    }

    @Test
    fun parseUrl_directionsCoordinatesWithCenter() {
        assertEquals(
            Position("34.0522", "-118.2437", z = "16"),
            parseUrl("https://www.google.com/maps/dir/40.7128,-74.0060/34.0522,-118.2437/@52.4844406,13.4217121,16z/")
        )
    }

    @Test
    fun parseUrl_directionsPlaceAndCoordinates() {
        assertEquals(
            Position(
                q = "Hôpital Européen Georges Pompidou Assistance Publique-Hôpitaux de Paris,20 r Leblanc, 75015 Paris",
                z = "8",
            ),
            @Suppress("SpellCheckingInspection")
            parseUrl("https://www.google.com/maps/dir/My+location/H%c3%b4pital+Europ%c3%a9en+Georges+Pompidou+Assistance+Publique-H%c3%b4pitaux+de+Paris,20+r+Leblanc%2c+75015+Paris/@48.83887481689453,2.2740750312805176,8z/")
        )
    }

    @Test
    fun parseUrl_directionsFromTo() {
        assertEquals(
            Position(q = "Los Angeles, CA"),
            parseUrl("https://www.google.com/maps/dir/New+York,+NY/Los+Angeles,+CA")
        )
    }

    @Test
    fun parseUrl_directionsFromToWithInvalidData() {
        assertEquals(
            Position(q = "Potsdam"),
            parseUrl("https://www.google.com/maps/dir/Berlin/Potsdam/data=spam")
        )
    }

    @Test
    fun parseUrl_directionsFromToVia() {
        assertEquals(
            Position(q = "Washington, DC"),
            parseUrl("https://www.google.com/maps/dir/New+York,+NY/Philadelphia,+PA/Washington,+DC")
        )
    }

    @Test
    fun parseUrl_directionsFromToViaWithCenter() {
        assertEquals(
            Position(q = "Reuterstraße 1, Berlin-Neukölln, Germany", z = "16"),
            @Suppress("SpellCheckingInspection")
            parseUrl("https://www.google.com/maps/dir/Hermannstra%C3%9Fe+1,+12049+Berlin,+Germany/Weserstr.+1,+12047+Berlin,+Germany/Reuterstra%C3%9Fe+1,+Berlin-Neuk%C3%B6lln,+Germany/@52.4844406,13.4217121,16z/")
        )
    }

    @Test
    fun parseUrl_directionsFromToViaWithCoordinatesInData() {
        assertEquals(
            Position(
                z = "16",
                points = listOf(
                    "52.4858222" to "13.4236883",
                    "52.4881038" to "13.4255518",
                    "52.4807739" to "13.4300356",
                ),
            ),
            @Suppress("SpellCheckingInspection")
            parseUrl("https://www.google.com/maps/dir/Hermannstra%C3%9Fe+1,+12049+Berlin,+Germany/Weserstr.+1,+12047+Berlin,+Germany/Reuterstra%C3%9Fe+1,+Berlin-Neuk%C3%B6lln,+Germany/@52.4844406,13.4217121,16z/data=!3m1!4b1!4m20!4m19!1m5!1m1!1s0x47a84fb831937021:0x28d6914e5ca0f9f5!2m2!1d13.4236883!2d52.4858222!1m5!1m1!1s0x47a84fb7098f1d89:0x74c8a84ad2981e9f!2m2!1d13.4255518!2d52.4881038!1m5!1m1!1s0x47a84fbb7c0791d7:0xf6e39aaedab8b2d9!2m2!1d13.4300356!2d52.4807739!3e2")
        )
    }

    @Test
    fun parseUrl_directionsEmpty() {
        assertEquals(
            Position(),
            parseUrl("https://www.google.com/maps/dir/")
        )
    }

    @Test
    fun parseUrl_streetView() {
        assertEquals(
            Position("48.8584", "2.2945"),
            @Suppress("SpellCheckingInspection")
            parseUrl("https://www.google.com/maps/@48.8584,2.2945,3a,75y,90t/data=!3m8!1e1!3m6!1sAF1QipP5ELjVeDJfzgBQBp5XM-HsNU0Ep1k_KgE!2e10!3e11!6shttps:%2F%2Flh5.googleusercontent.com%2Fp%2FAF1QipP5ELjVeDJfzgBQBp5XM-HsNU0Ep1k_KgE%3Dw203-h100-k-no-pi-0-ya293.79999-ro-0-fo100!7i10240!8i5120")
        )
    }

    @Test
    fun parseUrl_apiCenter() {
        assertEquals(
            Position("-33.712206", "150.311941", z = "12"),
            parseUrl("https://www.google.com/maps/@?api=1&map_action=map&center=-33.712206,150.311941&zoom=12&basemap=terrain")
        )
    }

    @Test
    fun parseUrl_apiCenterWithInvalidZoom() {
        assertEquals(
            Position("-33.712206", "150.311941"),
            parseUrl("https://www.google.com/maps/@?api=1&map_action=map&center=-33.712206,150.311941&zoom=spam&basemap=terrain")
        )
    }

    @Test
    fun parseUrl_apiDirections() {
        assertEquals(
            Position(q = "Cherbourg,France"),
            parseUrl("https://www.google.com/maps/dir/?api=1&origin=Paris,France&destination=Cherbourg,France&travelmode=driving&waypoints=Versailles,France%7CChartres,France%7CLe%2BMans,France%7CCaen,France")
        )
    }

    @Test
    fun parseUrl_apiViewpoint() {
        assertEquals(
            Position("48.857832", "2.295226"),
            parseUrl("https://www.google.com/maps/@?fov=80&pitch=38&heading=-45&viewpoint=48.857832,2.295226&map_action=pano&api=1")
        )
    }

    @Test
    fun parseUrl_qParameterCoordinates() {
        assertEquals(
            Position("48.857832", "2.295226"),
            parseUrl("https://www.google.com/maps?foo=bar&q=48.857832,2.295226&spam")
        )
    }

    @Test
    fun parseUrl_qParameterCoordinatesWithTrailingSlash() {
        assertEquals(
            Position("48.857832", "2.295226"),
            parseUrl("https://www.google.com/maps/?q=48.857832,2.295226")
        )
    }

    @Test
    fun parseUrl_qParameterCoordinatesWithEmptyPath() {
        assertEquals(
            Position("39.797573", "18.370173"),
            parseUrl("https://maps.google.com/?q=39.797573,18.370173&entry=gps&g_ep=abc&shorturl=1")
        )
    }

    @Test
    fun parseUrl_qParameterPlace() {
        assertEquals(
            Position(q = "Central Park"),
            parseUrl("https://www.google.com/maps?foo=bar&q=Central Park&spam")
        )
    }

    @Test
    fun parseUrl_qParameterPlaceWithoutPath() {
        assertEquals(
            Position(q = "Café Heinemann, Bismarckstraße 91, 41061 Mönchengladbach"),
            parseUrl("https://maps.google.com?q=Caf%C3%A9+Heinemann,+Bismarckstra%C3%9Fe+91,+41061+M%C3%B6nchengladbach")
        )
    }

    @Test
    fun parseUrl_qParameterEmpty() {
        assertEquals(
            Position(),
            parseUrl("https://www.google.com/maps")
        )
    }

    @Test
    fun parseUrl_googleSearch() {
        assertEquals(
            Position(),
            parseUrl("https://www.google.com/search?sca_esv=123&hl=en")
        )
    }

    @Test
    fun parseUrl_myMaps() {
        assertEquals(
            Position(),
            parseUrl("https://www.google.com/maps/d/edit?mid=abcdef")
        )
        assertEquals(
            Position(),
            parseUrl("https://www.google.com/maps/d/viewer?mid=abcdef")
        )
    }

    @Test
    fun parseUrl_http() {
        assertEquals(
            Position("52.5067296", "13.2599309", q = "Berlin, Germany", z = "11"),
            parseUrl("http://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"),
        )
    }

    @Test
    fun parseUrl_ukDomain() {
        assertEquals(
            Position("52.5067296", "13.2599309", q = "Berlin, Germany", z = "11"),
            parseUrl("https://maps.google.co.uk/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"),
        )
    }

    @Test
    fun parseUrl_noScheme() {
        assertEquals(
            Position("52.5067296", "13.2599309", q = "Berlin, Germany", z = "11"),
            parseUrl("maps.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"),
        )
    }

    @Test
    fun parseHtml_link() {
        assertEquals(
            Position("44.4490541", "26.0888398"),
            @Suppress("SpellCheckingInspection")
            parseHtml(
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
    fun parseHtml_placeList() {
        assertEquals(
            Position(
                points = listOf(
                    "59.1293656" to "11.4585672",
                    "59.4154007" to "11.659710599999999",
                    "59.147731699999994" to "11.550661199999999"
                )
            ),
            @Suppress("SpellCheckingInspection")
            parseHtml(
                """<html>
<head>
  <script>
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
    fun parseHtml_myMaps() {
        assertEquals(
            Position(
                points = listOf(
                    "52.49016" to "13.434500000000071",
                    "52.49534999999999" to "13.431890000000067",
                    "52.4901894" to "13.433825899999988",
                    "52.4898201" to "13.433602800000017",
                    "52.4960741" to "13.435130399999935",
                    "52.4961778" to "13.422070500000018",
                    "52.49514559999999" to "13.423243800000023",
                    "52.497884" to "13.429134",
                    "52.4957432" to "13.43344819999993",
                )
            ),
            @Suppress("SpellCheckingInspection")
            parseHtml(
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
    fun parseHtml_placeListOnePoint() {
        assertEquals(
            Position("59.1293656", "11.4585672"),
            @Suppress("SpellCheckingInspection")
            parseHtml(
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
    fun parseHtml_failure() {
        assertNull(parseHtml("spam"))
    }

    @Test
    fun parseHtml_googleSearchHtmlDoesNotContainUrl_returnsNull() {
        assertNull(parseHtml("<html></html>"))
    }

    @Test
    fun parseHtml_googleSearchHtmlContainsRelativeUrl_returnsIt() {
        assertEquals(
            "/maps/place//data=!4m2!3m1!1s0xc3f7d4e21a00705:0xa9ea51361ed84bda?sa=X&amp;ved=2ahUKEwiY7vv80aeKAxU41QIHHSgBOlsQ4kB6BAgHEAA&amp;hl=de&amp;gl=de",
            @Suppress("SpellCheckingInspection")
            parseHtmlRedirect(
                """<html>
<body>
  <a
    href="/maps/place//data=!4m2!3m1!1s0xc3f7d4e21a00705:0xa9ea51361ed84bda?sa=X&amp;ved=1t:8290&amp;hl=de&amp;gl=de&amp;ictx=111"
    data-url="/maps/place//data=!4m2!3m1!1s0xc3f7d4e21a00705:0xa9ea51361ed84bda?sa=X&amp;ved=2ahUKEwiY7vv80aeKAxU41QIHHSgBOlsQ4kB6BAgHEAA&amp;hl=de&amp;gl=de"
  >Camino del Quinto</a>
</body>
</html>
"""
            )
        )
    }

    @Test
    fun parseHtml_googleSearchHtmlContainsAbsoluteUrl_returnsIt() {
        val html = """<html><a href="" data-url="https://www.example.com/foo"></a></html>"""
        assertEquals(
            "https://www.example.com/foo",
            parseHtmlRedirect(html)
        )
    }

    @Test
    fun parseHtml_googleSearchHtmlContainsInvalidUrl_returnsNull() {
        val html = """<html><a href="" data-url="abc"></a></html>"""
        assertNull(parseHtml(html))
    }

    @Test
    fun parseHtml_mapsQuery_returnsNull() {
        val html = """<html><a href="" data-url="abc"></a></html>"""
        assertNull(parseHtml(html))
    }

    @Test
    fun isShortUri_mapsAppGooGlCorrect() {
        assertTrue(isShortUrl("https://maps.app.goo.gl/foo"))
    }

    @Test
    fun isShortUri_mapsAppGooGlWithQueryStringCorrect() {
        assertFalse(isShortUrl("https://maps.app.goo.gl/foo?g_st=isi"))
    }

    @Test
    fun isShortUri_mapsAppGooGlMissingPath() {
        assertFalse(isShortUrl("https://maps.app.goo.gl/"))
    }

    @Test
    fun isShortUri_appGooGlCorrect() {
        assertTrue(isShortUrl("https://app.goo.gl/maps/foo"))
    }

    @Test
    fun isShortUri_appGooGlWrongPath() {
        assertTrue(isShortUrl("https://app.goo.gl/maps"))
        assertTrue(isShortUrl("https://app.goo.gl/maps/"))
        assertTrue(isShortUrl("https://app.goo.gl/foo/bar"))
    }

    @Test
    fun isShortUri_gooGlCorrect() {
        assertTrue(isShortUrl("https://goo.gl/maps/foo"))
    }

    @Test
    fun isShortUri_gooGlWrongPath() {
        assertTrue(isShortUrl("https://goo.gl/maps"))
        assertTrue(isShortUrl("https://goo.gl/maps/"))
        assertTrue(isShortUrl("https://goo.gl/foo/bar"))
    }

    @Test
    fun isShortUri_gCoCorrect() {
        assertTrue(isShortUrl("https://g.co/kgs/foo"))
    }

    @Test
    fun isShortUri_unknownDomain() {
        assertFalse(isShortUrl("https://www.example.com/foo"))
    }
}
