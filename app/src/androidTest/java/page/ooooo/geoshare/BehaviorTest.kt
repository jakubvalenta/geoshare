package page.ooooo.geoshare

import android.app.ActivityManager
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.onElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.head
import io.ktor.http.isSuccess
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.lib.NetworkTools.Companion.CONNECT_TIMEOUT
import page.ooooo.geoshare.lib.NetworkTools.Companion.EXPONENTIAL_DELAY_BASE
import page.ooooo.geoshare.lib.NetworkTools.Companion.EXPONENTIAL_DELAY_BASE_DELAY
import page.ooooo.geoshare.lib.NetworkTools.Companion.MAX_RETRIES
import page.ooooo.geoshare.lib.NetworkTools.Companion.REQUEST_TIMEOUT
import page.ooooo.geoshare.lib.android.GOOGLE_MAPS_PACKAGE_NAME
import page.ooooo.geoshare.lib.android.TOMTOM_PACKAGE_NAME
import page.ooooo.geoshare.lib.formats.CoordsFormat
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.ui.UserPreferencesGroupId
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.math.pow
import kotlin.math.roundToLong

class DialogScope(val dialog: UiObject2) {
    fun confirm() {
        dialog.onElement { viewIdResourceName == "geoShareConfirmationDialogConfirmButton" }.click()
    }

    fun dismiss() {
        dialog.onElement { viewIdResourceName == "geoShareConfirmationDialogDismissButton" }.click()
    }

    fun toggleDoNotAsk() {
        dialog.onElement { viewIdResourceName == "geoShareConfirmationDialogDoNotAskSwitch" }.click()
    }
}

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

interface BehaviorTest {

    companion object {
        const val ELEMENT_DOES_NOT_EXIST_TIMEOUT = 500L
        val NETWORK_TIMEOUT = (1..MAX_RETRIES).fold(CONNECT_TIMEOUT + REQUEST_TIMEOUT) { acc, curr ->
            acc + (EXPONENTIAL_DELAY_BASE.pow(curr - 1) * EXPONENTIAL_DELAY_BASE_DELAY).roundToLong() + CONNECT_TIMEOUT + REQUEST_TIMEOUT
        }
    }

    @Before
    fun goToLauncher() = uiAutomator {
        // Start from the home screen
        pressHome()
    }

    fun UiAutomatorTestScope.launchApplication() {
        // Use shell command instead of startActivity() to support Xiaomi.
        device.executeShellCommand("monkey -p ${BuildConfig.APPLICATION_ID} 1")

        // Wait for the app to appear
        waitForAppToBeVisible(BuildConfig.APPLICATION_ID)
    }

    fun closeApplication() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val activityManager = context.getSystemService(ActivityManager::class.java)
        activityManager.appTasks.forEach { it.finishAndRemoveTask() }
    }

    // FIXME
    suspend fun UiAutomatorTestScope.killApplication(timeoutMs: Long = 3_000L, pollIntervalMs: Long = 100L) {
        pressHome()
        delay(500L) // Wait for the app to go to background, otherwise it doesn't get killed
        device.executeShellCommand("am kill ${BuildConfig.APPLICATION_ID}")

        // Poll until the process is gone
        withTimeout(timeoutMs) {
            while (
                device.executeShellCommand(
                    @Suppress("SpellCheckingInspection") "pidof ${BuildConfig.APPLICATION_ID}"
                ).isNotBlank()
            ) {
                delay(pollIntervalMs)
            }
        }
    }

    fun UiAutomatorTestScope.bringApplicationToFront() {
        device.executeShellCommand(
            @Suppress("SpellCheckingInspection") "am start -n ${BuildConfig.APPLICATION_ID}/page.ooooo.geoshare.MainActivity ${BuildConfig.APPLICATION_ID}"
        )
    }

    fun closeIntro() = uiAutomator {
        quickWaitForStableInActiveWindow() // Wait for the intro to render, otherwise closing it can fail even with large timeout
        onElement { viewIdResourceName == "geoShareIntroScreenCloseButton" }.click()
    }

    fun quickWaitForStableInActiveWindow() = uiAutomator {
        waitForStableInActiveWindow(stableTimeoutMs = 1000L, stableIntervalMs = 100L, requireStableScreenshot = false)
    }

    fun onDialog(
        resourceName: String,
        timeoutMs: Long = 20_000L,
        block: DialogScope.() -> Unit,
    ) = uiAutomator {
        val dialog = onElement(timeoutMs) { viewIdResourceName == resourceName }
        DialogScope(dialog).block()
    }

    private fun isLocationGrantButton(element: AccessibilityNodeInfo): Boolean =
        @Suppress("SpellCheckingInspection") when (element.textAsString()?.lowercase()) {
            "only this time", "uniquement cette fois-ci" -> true
            else -> false
        }

    fun UiAutomatorTestScope.grantLocationPermission() {
        onElement { isLocationGrantButton(this) }.click()
    }

    fun UiAutomatorTestScope.grantLocationPermissionIfNecessary() {
        onElementOrNull(3_000L) { isLocationGrantButton(this) }?.click()
    }

    fun UiAutomatorTestScope.denyLocationPermission() {
        onElement {
            @Suppress("SpellCheckingInspection") when (textAsString()?.lowercase()) {
                "don't allow", "don’t allow", "ne pas autoriser" -> true
                else -> false
            }
        }.click()
    }

    fun UiAutomatorTestScope.assumeAppInstalled(packageName: String) {
        assumeTrue(
            "This test only works when $packageName is installed on the device",
            device.executeShellCommand("pm path $packageName").isNotEmpty(),
        )
    }

    suspend fun assumeDomainResolvable(
        @Suppress("SameParameterValue") domain: String,
        timeoutMs: Long = 1_000L,
    ) {
        // Use futures, because InetAddress.getByName() is not cancellable using Kotlin's withTimeout()
        val executor = Executors.newSingleThreadExecutor()
        val future = executor.submit<Boolean> {
            try {
                InetAddress.getByName(domain)
                true
            } catch (_: UnknownHostException) {
                false
            }
        }
        val success = try {
            withContext(Dispatchers.IO) {
                future.get(timeoutMs, TimeUnit.MILLISECONDS)
            }
        } catch (_: TimeoutException) {
            future.cancel(true)
            false
        } finally {
            executor.shutdownNow()
        }
        assumeTrue("This test only works when DNS resolves the domain $domain", success)
    }

    suspend fun assumeHttpHeadIsSuccess(@Suppress("SameParameterValue") url: String) {
        val status = withContext(Dispatchers.IO) {
            HttpClient(CIO).head(url).status
        }
        assumeTrue(
            "This test only works when HTTP HEAD request succeeds but it was ${status.value} for $url",
            status.isSuccess(),
        )
    }

    /**
     * Check that the result screen shows [expectedPoints]
     *
     * Point name is checked in a fuzzy way. It is enough if the shown name contains the expected name. We need this,
     * because we often cannot use an exact match, because Google Maps returns different place name depending on the
     * phone's language and location.
     */
    fun UiAutomatorTestScope.assertConversionSucceeded(
        expectedPoints: ImmutableList<Point>,
        timeoutMs: Long = NETWORK_TIMEOUT,
    ) {
        onElement(timeoutMs) {
            when (viewIdResourceName) {
                "geoShareResultSuccessLastPointName" -> true
                "geoShareConversionErrorMessage" -> throw AssertionError("Conversion failed")
                else -> false
            }
        }
        val expectedName = expectedPoints.lastOrNull()?.cleanName
        onElement {
            if (viewIdResourceName == "geoShareResultSuccessLastPointName") {
                if (!expectedName.isNullOrEmpty()) {
                    assertTrue(
                        """Expected "${textAsString()}" to contain "$expectedName"""",
                        textAsString()?.contains(expectedName) == true,
                    )
                } else if (expectedPoints.size > 1) {
                    assertTrue(
                        """Expected "${textAsString()}" to equal "Last point" or "Dernier point""""",
                        textAsString() in setOf("Last point", "Dernier point"),
                    )
                } else {
                    assertTrue(
                        @Suppress("SpellCheckingInspection") """Expected "${textAsString()}" to equal "Coordinates" or "Coordonnées""""",
                        textAsString() in setOf(
                            "Coordinates", @Suppress("SpellCheckingInspection") "Coordonnées"
                        ),
                    )
                }
                true
            } else {
                false
            }
        }
        val expectedCoordinatesOptions = expectedPoints
            .lastOrNull()
            ?.takeIf { it.hasCoordinates() }
            ?.let { point ->
                CoordinateFormat.entries.map { coordinateFormat ->
                    when (coordinateFormat) {
                        CoordinateFormat.DEC -> CoordsFormat.formatDecCoords(point)
                        CoordinateFormat.DEG_MIN_SEC -> CoordsFormat.formatDegMinSecCoords(point)
                    }
                }
            }
        if (expectedCoordinatesOptions != null) {
            onElement {
                if (viewIdResourceName == "geoShareResultSuccessLastPointCoordinates") {
                    assertTrue(
                        """Expected "${textAsString()} to equal one of ${expectedCoordinatesOptions.joinToString()}""",
                        textAsString() in expectedCoordinatesOptions,
                    )
                    true
                } else {
                    false
                }
            }
        }
        if (expectedPoints.size > 1) {
            onElement {
                if (viewIdResourceName == "geoShareResultSuccessAllPointsHeadline") {
                    assertTrue(
                        """Expected "${textAsString()}" to contain "${expectedPoints.size}"""",
                        textAsString()?.contains(expectedPoints.size.toString()) == true,
                    )
                    true
                } else {
                    false
                }
            }
        }
    }

    fun UiAutomatorTestScope.assertConversionSucceeded(
        expectedPoint: Point,
        timeoutMs: Long = NETWORK_TIMEOUT,
    ) =
        assertConversionSucceeded(persistentListOf(expectedPoint), timeoutMs)

    fun UiAutomatorTestScope.waitAndAssertGoogleMapsContainsElement(block: AccessibilityNodeInfo.() -> Boolean) {
        // Wait for Google Maps
        onElement(20_000L) { packageName == GOOGLE_MAPS_PACKAGE_NAME }

        // If there is a Google Maps sign in screen, skip it
        onElementOrNull(3_000L) {
            packageName == GOOGLE_MAPS_PACKAGE_NAME && @Suppress("SpellCheckingInspection") when (textAsString()) {
                "Make it your map", "Profitez d'une carte personnalisée" -> true
                else -> false
            }
        }?.let {
            onElement {
                packageName == GOOGLE_MAPS_PACKAGE_NAME && when (textAsString()?.lowercase()) {
                    "skip", "ignorer" -> true
                    else -> false
                }
            }.click()
        }

        // Verify Google Maps content
        onElement(20_000L) { packageName == GOOGLE_MAPS_PACKAGE_NAME && this.block() }
    }

    fun UiAutomatorTestScope.waitAndAssertTomTomContainsElement(block: AccessibilityNodeInfo.() -> Boolean) {
        // Wait for TomTom
        onElement(30_000L) { packageName == TOMTOM_PACKAGE_NAME }

        // If there is location permission, grant it
        grantLocationPermissionIfNecessary()

        // If there is Importing GPX tracks dialog, confirm it
        onElementOrNull(5_000L) {
            @Suppress("SpellCheckingInspection") when (textAsString()) {
                "Got it", "J'ai compris" -> true
                else -> false
            }
        }?.click()

        // Verify TomTom content
        onElement { packageName == TOMTOM_PACKAGE_NAME && this.block() }
    }

    fun UiAutomatorTestScope.shareUri(unsafeUriString: String) {
        // Use shell command instead of startActivity() to support Xiaomi
        device.executeShellCommand(
            @Suppress("SpellCheckingInspection") "am start -a android.intent.action.VIEW -d $unsafeUriString -n ${BuildConfig.APPLICATION_ID}/page.ooooo.geoshare.ConversionActivity ${BuildConfig.APPLICATION_ID}"
        )
    }

    fun UiAutomatorTestScope.goToMenuItem(block: AccessibilityNodeInfo.() -> Boolean) {
        onElement { viewIdResourceName == "geoShareMainMenuButton" }.click()
        onElement(block = block).click()
    }

    fun UiAutomatorTestScope.goToInputsScreen() {
        goToMenuItem { viewIdResourceName == "geoShareMainMenuInputs" }
    }

    fun UiAutomatorTestScope.goToUserPreferencesScreen() {
        goToMenuItem { viewIdResourceName == "geoShareMainMenuUserPreferences" }
    }

    fun UiAutomatorTestScope.goToUserPreferencesDetailConnectionPermissionScreen() {
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferencesGroupId.CONNECTION_PERMISSION}" }.click()
    }

    fun UiAutomatorTestScope.goToUserPreferencesDetailCoordinateFormatScreen() {
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferencesGroupId.COORDINATE_FORMAT}" }.click()
    }

    fun UiAutomatorTestScope.goToUserPreferencesDetailDeveloperScreen() {
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferencesGroupId.DEVELOPER_OPTIONS}" }.click()
    }

    fun UiAutomatorTestScope.goToMainScreenFromUserPreferencesDetail() {
        onElement { viewIdResourceName == "geoShareBack" }.click()
        if (onElementOrNull(1_000L) { viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferencesGroupId.DEVELOPER_OPTIONS}" } != null) {
            // On a non-tablet screen, we need to tap the back button one more time to get from the user preferences
            // list screen to the main screen
            onElement { viewIdResourceName == "geoShareBack" }.click()
        }
    }

    fun UiAutomatorTestScope.chooseFile() {
        if (onElementOrNull(3_000L) { textAsString() == "Recent" } != null) {
            // If we happen to be in the Recent directory, go to Downloads, because it's not possible to save to Recent
            device.click(50, 100) // Tap the hamburger menu
            onElement { textAsString() == "Downloads" }.click()
        } else {
            onElement {
                textAsString() == "Downloads" ||
                    textAsString()?.startsWith("Files in") == true ||
                    textAsString()?.startsWith(@Suppress("SpellCheckingInspection") "Fichiers dans le dossier") == true
            }
        }
        onElement { textAsString() in setOf("SAVE", @Suppress("SpellCheckingInspection") "ENREGISTRER") }.click()
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
}
