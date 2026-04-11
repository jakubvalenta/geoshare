package com.lbt05.evil_transform

import org.junit.Assert.assertTrue
import org.junit.Test

class TransformUtilTest {
    @Test
    fun outOfChina_pointIsInChina_returnsTrue() {
        assertTrue(TransformUtil.outOfChina(116.331538, 39.920439))
    }

    @Test
    fun outOfChina_pointIsInWesternJapan_returnsTrue() {
        assertTrue(TransformUtil.outOfChina(133.7583428, 34.5945482))
    }
}
