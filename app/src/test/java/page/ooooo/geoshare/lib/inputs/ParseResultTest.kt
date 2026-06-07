package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class ParseResultTest {
    private val emptyResult = ParseResult()
    private val coordinatesAndZoomAndNameResult = ParseResult(
        persistentListOf(
            WGS84Point(3.0, 4.0, source = Source.URI),
            WGS84Point(1.0, 2.0, z = 3.14, name = "coordinatesAndZoomAndName", source = Source.URI),
        ),
        next = MatchedInput(FakeInputRepository.googleMapsHtmlInput, "coordinatesAndZoomAndNameMatch")
    )
    private val coordinatesResult = ParseResult(
        persistentListOf(
            WGS84Point(3.0, 4.0, source = Source.URI),
            WGS84Point(1.0, 2.0, source = Source.URI),
        ),
        next = MatchedInput(FakeInputRepository.googleMapsHtmlInput, "coordinatesMatch")
    )
    private val zoomResult = ParseResult(
        persistentListOf(
            WGS84Point(7.0, 8.0, source = Source.URI),
            WGS84Point(z = 3.14, source = Source.URI),
        ),
        next = MatchedInput(FakeInputRepository.googleMapsHtmlInput, "zoomMatch")
    )
    private val nameResult = ParseResult(
        persistentListOf(
            WGS84Point(5.0, 6.0, source = Source.URI),
            WGS84Point(name = "nameResult", source = Source.URI),
        ),
        next = MatchedInput(FakeInputRepository.googleMapsHtmlInput, "nameMatch")
    )
    private val zoomAndNameResult = ParseResult(
        persistentListOf(
            WGS84Point(9.0, 10.0, source = Source.URI),
            WGS84Point(name = "zoomAndNameResult", z = 3.14, source = Source.URI),
        ),
        next = MatchedInput(FakeInputRepository.googleMapsHtmlInput, "zoomAndNameMatch")
    )

    @Test
    fun merge_whenListIsEmpty_returnsEmptyResult() {
        assertEquals(
            emptyResult,
            emptyList<ParseResult>().merge()
        )
    }

    @Test
    fun merge_whenListHasFirstResultWithCoordinatesOnly_returnsFirstResultUnchanged() {
        assertEquals(
            coordinatesResult,
            listOf(coordinatesResult).merge(),
        )
    }

    @Test
    fun merge_whenListHasFirstResultWithNameOnly_returnsFirstResultUnchanged() {
        assertEquals(
            nameResult,
            listOf(nameResult).merge(),
        )
    }

    @Test
    fun merge_whenListHasFirstResultWithCoordinatesAndZoomAndName_andSecondResultWithZoomAndName_returnsFirstResultUnchanged() {
        assertEquals(
            coordinatesAndZoomAndNameResult,
            listOf(coordinatesAndZoomAndNameResult, zoomAndNameResult).merge(),
        )
    }

    @Test
    fun merge_whenListHasFirstResultWithCoordinatesOnlyAndSecondResultWithZoomOnly_returnsMergedResult() {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(3.0, 4.0, source = Source.URI),
                    WGS84Point(1.0, 2.0, z = 3.14, source = Source.URI),
                ),
                next = coordinatesResult.next,
            ),
            listOf(coordinatesResult, zoomResult).merge(),
        )
    }

    @Test
    fun merge_whenListHasFirstResultWithCoordinatesOnlyAndSecondResultWithNameOnly_returnsMergedResult() {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(3.0, 4.0, source = Source.URI),
                    WGS84Point(1.0, 2.0, name = "nameResult", source = Source.URI),
                ),
                next = coordinatesResult.next,
            ),
            listOf(coordinatesResult, nameResult).merge(),
        )
    }

    @Test
    fun merge_whenListHasFirstResultWithCoordinatesOnlyAndThirdResultWithZoomAndName_returnsMergedResult() {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(3.0, 4.0, source = Source.URI),
                    WGS84Point(1.0, 2.0, z = 3.14, name = "zoomAndNameResult", source = Source.URI),
                ),
                next = coordinatesResult.next,
            ),
            listOf(coordinatesResult, emptyResult, zoomAndNameResult).merge(),
        )
    }

    @Test
    fun merge_whenListHasEmptyFirstResult_returnsFirstResultUnchanged() {
        assertEquals(
            emptyResult,
            listOf(emptyResult).merge(),
        )
    }

    @Test
    fun merge_whenListHasEmptyFirstResultAndSecondResultWithCoordinatesOnly_returnsFirstResultUnchanged() {
        assertEquals(
            emptyResult,
            listOf(emptyResult, coordinatesResult).merge(),
        )
    }

    @Test
    fun merge_whenListHasEmptyFirstResultAndSecondResultWithZoomOnly_returnsMergedResult() {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(z = 3.14, source = Source.URI),
                )
            ),
            listOf(emptyResult, zoomResult).merge(),
        )
    }

    @Test
    fun merge_whenListHasEmptyFirstResultAndSecondResultWithNameOnly_returnsMergedResult() {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(name = "nameResult", source = Source.URI),
                )
            ),
            listOf(emptyResult, nameResult).merge(),
        )
    }

    @Test
    fun merge_whenListHasEmptyFirstResultAndThirdResultWithZoomAndName_returnsMergedResult() {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(z = 3.14, name = "zoomAndNameResult", source = Source.URI),
                )
            ),
            listOf(emptyResult, emptyResult, zoomAndNameResult).merge(),
        )
    }

    @Test
    fun merge_whenListHasEmptyFirstResultAndEmptySecondResult_returnsFirstResultUnchanged() {
        assertEquals(
            emptyResult,
            listOf(emptyResult, emptyResult).merge(),
        )
    }
}
