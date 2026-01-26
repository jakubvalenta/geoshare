package com.github._46319943.bd09convertor

import org.junit.Assert.assertEquals
import org.junit.Test

class BD09ConvertorTest {
    @Test
    fun convertMC2LL_example() {
        assertEquals(
            39.96162707324356 to 116.27462499999993,
            BD09Convertor.convertMC2LL(x = 12943772.884424742, y = 4832666.423350099),
        )
    }

    @Test
    fun convertMC2LL_beijing() {
        assertEquals(
            30.946576225978003 to 118.76519558159015,
            BD09Convertor.convertMC2LL(x = 13221024.95, y = 3603638.53),
        )
        assertEquals(
            22.548514883102943 to 114.06611219325782,
            BD09Convertor.convertMC2LL(x = 12697919.69, y = 2560977.31),
        )
    }
}
