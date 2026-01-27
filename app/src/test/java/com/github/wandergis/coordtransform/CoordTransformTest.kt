package com.github.wandergis.coordtransform

import org.junit.Assert.assertEquals
import org.junit.Test

class CoordTransformTest {
    @Test
    fun bd09toGCJ02_example() {
        assertEquals(
            39.90865673957631 to 116.39762729119315,
            CoordTransform.bd09toGCJ02(39.915, 116.404)
        )
    }

    @Test
    fun bd09toGCJ02_beijing() {
        assertEquals(
            30.940779294367676 to 118.75866025417945,
            CoordTransform.bd09toGCJ02(30.946576225978003, 118.76519558159015)
        )
        assertEquals(
            22.542860065931386 to 114.05956013578918,
            CoordTransform.bd09toGCJ02(22.548514883102943, 114.06611219325782)
        )
    }
}
