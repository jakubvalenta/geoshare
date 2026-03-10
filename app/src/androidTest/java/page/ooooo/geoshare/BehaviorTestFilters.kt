package page.ooooo.geoshare

import android.os.Build
import androidx.test.filters.AbstractFilter
import androidx.test.filters.CustomFilter
import org.junit.runner.Description

class NotEmulatorFilter : AbstractFilter() {
    override fun evaluateTest(description: Description?) = Build.HARDWARE != "ranchu"

    override fun describe() = "Run if the device is not an emulator"
}

@CustomFilter(filterClass = NotEmulatorFilter::class)
annotation class NotEmulator
