package page.ooooo.geoshare.lib.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

class IterableExtensionsTest {
    @Test
    fun chunkedParis() {
        assertEquals(
            listOf(1 to 2, 3 to 4),
            listOf(1, 2, 3, 4, 5).chunkedPairs(),
        )
        assertEquals(
            listOf<Pair<Int, Int>>(),
            listOf<Int>().chunkedPairs(),
        )
        assertEquals(
            listOf(1 to 2),
            listOf(1, 2).chunkedPairs(),
        )
    }

    @Test
    fun removeDuplicates() {
        assertEquals(
            listOf(1, 2, 3, 2),
            listOf(1, 2, 2, 2, 3, 2).removeRepeated(),
        )
        assertEquals(
            listOf<Int>(),
            listOf<Int>().removeRepeated(),
        )
        assertEquals(
            listOf(1),
            listOf(1).removeRepeated(),
        )
        assertEquals(
            listOf(1, 2),
            listOf(1, 2).removeRepeated(),
        )
        assertEquals(
            listOf(2),
            listOf(2, 2).removeRepeated(),
        )
    }

    @Test
    fun removeDuplicates_comparesEqualityUsingPassedFunction() {
        assertEquals(
            listOf(1 to 1, 2 to 2, 3 to 3, 2 to 2),
            listOf(1 to 1, 2 to 2, 2 to 2, 2 to 2, 3 to 3, 2 to 2).removeRepeated { prev, curr ->
                prev?.first == curr.first && prev.second == curr.second
            },
        )
    }

    @Test
    fun zipWithNextFirstNull_returnsNullPlusZippedElements() {
        assertEquals(
            listOf(null to 1, 1 to 2, 2 to 3),
            listOf(1, 2, 3).zipWithNextFirstNull { a, b -> a to b },
        )
        assertEquals(
            listOf(null to 1, 1 to 2),
            listOf(1, 2).zipWithNextFirstNull { a, b -> a to b },
        )
        assertEquals(
            listOf(null to 1),
            listOf(1).zipWithNextFirstNull { a, b -> a to b },
        )
        assertEquals(
            emptyList<Pair<Int, Int>>(),
            emptyList<Int>().zipWithNextFirstNull { a, b -> a to b },
        )
    }

    @Test
    fun zipWithNextFirstNull_callsTransformInOrder() {
        val res = mutableListOf<Pair<Int?, Int>>()
        listOf(1, 2, 3).zipWithNextFirstNull { a, b -> res.add(a to b) }
        assertEquals(
            listOf(null to 1, 1 to 2, 2 to 3),
            res,
        )
    }

    @Test
    fun zipWithNextLastNull_returnsZippedElementsPlusNull() {
        assertEquals(
            listOf(1 to 2, 2 to 3, 3 to null),
            listOf(1, 2, 3).zipWithNextLastNull { a, b -> a to b },
        )
        assertEquals(
            listOf(1 to 2, 2 to null),
            listOf(1, 2).zipWithNextLastNull { a, b -> a to b },
        )
        assertEquals(
            listOf(1 to null),
            listOf(1).zipWithNextLastNull { a, b -> a to b },
        )
        assertEquals(
            emptyList<Pair<Int, Int>>(),
            emptyList<Int>().zipWithNextLastNull { a, b -> a to b },
        )
    }

    @Test
    fun zipWithNextLastNull_callsTransformInOrder() {
        val res = mutableListOf<Pair<Int, Int?>>()
        listOf(1, 2, 3).zipWithNextLastNull { a, b -> res.add(a to b) }
        assertEquals(
            listOf(1 to 2, 2 to 3, 3 to null),
            res,
        )
    }
}
