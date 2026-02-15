package page.ooooo.geoshare

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiAutomatorTestScope

class MockLocationScope(val locationManager: LocationManager, val mockProviderName: String) {
    fun setLocation(lat: Double, lon: Double) {
        val location = Location(mockProviderName).apply {
            latitude = lat
            longitude = lon
            altitude = 0.0
            accuracy = 1.0f
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        }
        locationManager.setTestProviderLocation(mockProviderName, location)
    }
}

fun UiAutomatorTestScope.mockLocation(block: MockLocationScope.() -> Unit) {
    device.executeShellCommand(
        @Suppress("SpellCheckingInspection")
        "appops set ${BuildConfig.APPLICATION_ID} android:mock_location allow"
    )

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    val locationManager: LocationManager = context.getSystemService(LocationManager::class.java)
    val mockProviderName = LocationManager.GPS_PROVIDER

    locationManager.addTestProvider(
        mockProviderName,
        false, false, false, false, false, false, false,
        ProviderProperties.POWER_USAGE_LOW,
        ProviderProperties.ACCURACY_FINE,
    )
    locationManager.setTestProviderEnabled(mockProviderName, true)

    try {
        MockLocationScope(locationManager, mockProviderName).block()
    } finally {
        locationManager.removeTestProvider(mockProviderName)
    }
}
