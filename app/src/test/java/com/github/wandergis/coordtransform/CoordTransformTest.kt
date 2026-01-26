package com.github.wandergis.coordtransform

import org.junit.Assert.assertEquals
import org.junit.Test

class CoordTransformTest {
    @Test
    fun bd09toGCJ02_example() {
        assertEquals(
            39.90865673957631 to 116.39762729119315,
            CoordTransform.bd09toGCJ02(lat = 39.915, lon = 116.404)
        )
    }
}
