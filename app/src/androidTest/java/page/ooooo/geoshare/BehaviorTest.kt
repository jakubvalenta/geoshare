package page.ooooo.geoshare

import android.app.ActivityManager
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.onElement
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.head
import io.ktor.http.isSuccess
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.NetworkTools.Companion.CONNECT_TIMEOUT
import page.ooooo.geoshare.lib.network.NetworkTools.Companion.EXPONENTIAL_DELAY_BASE
import page.ooooo.geoshare.lib.network.NetworkTools.Companion.EXPONENTIAL_DELAY_BASE_DELAY
import page.ooooo.geoshare.lib.network.NetworkTools.Companion.MAX_RETRIES
import page.ooooo.geoshare.lib.network.NetworkTools.Companion.REQUEST_TIMEOUT
import page.ooooo.geoshare.ui.UserPreferencesGroupId
import java.net.InetAddress
import java.net.SocketException
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

    fun UiAutomatorTestScope.closeIntro() {
        quickWaitForStableInActiveWindow() // Wait for the intro to render, otherwise closing it can fail even with large timeout
        onElement { viewIdResourceName == "geoShareIntroScreenCloseButton" }.click()
    }

    fun UiAutomatorTestScope.quickWaitForStableInActiveWindow() {
        waitForStableInActiveWindow(stableTimeoutMs = 1_000L, stableIntervalMs = 100L, requireStableScreenshot = false)
    }

    fun onDialog(
        resourceName: String,
        timeoutMs: Long = 10_000L,
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
        val status = try {
            withContext(Dispatchers.IO) {
                HttpClient(CIO).head(url).status
            }
        } catch (_: SocketException) {
            null
        }
        assumeTrue(
            "This test only works when HTTP HEAD request succeeds but it ${if (status != null) "was ${status.value}" else "timed out"} for $url",
            status?.isSuccess() == true,
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
        expectedPoints: Points,
        timeoutMs: Long = NETWORK_TIMEOUT,
    ) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)

        onElement(timeoutMs) {
            when (viewIdResourceName) {
                "geoShareResultSuccessLastPointName" -> true
                "geoShareConversionErrorMessage" -> throw AssertionError("Conversion failed")
                else -> false
            }
        }
        val lastPoint = expectedPoints.lastOrNull() ?: return
        lastPoint.cleanName.let { expectedName ->
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
        }
        lastPoint
            .takeIf { it.hasCoordinates() }
            ?.let { point ->
                CoordinateFormat.entries.map { coordinateFormat ->
                    when (coordinateFormat) {
                        CoordinateFormat.DEC -> CoordinateFormatter.formatDecCoords(
                            coordinateConverter.toWGS84(point)
                        )

                        CoordinateFormat.DEG_MIN_SEC -> CoordinateFormatter.formatDegMinSecCoords(
                            coordinateConverter.toWGS84(point)
                        )
                    }
                }
            }
            ?.let { expectedCoordinatesOptions ->
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
        lastPoint.source.let { expectedSource ->
            onElement { viewIdResourceName == "geoShareResultSuccessLastPointSource_${expectedSource}" }
            if (!lastPoint.accurate) {
                onElement { viewIdResourceName == "geoShareResultSuccessLastPointCheckSRS" }
            } else if (expectedSource == Source.JAVASCRIPT) {
                onElement { viewIdResourceName == "geoShareResultSuccessLastPointCheckJavaScript" }
            } else if (expectedSource == Source.MAP_CENTER) {
                onElement { viewIdResourceName == "geoShareResultSuccessLastPointCheckMapCenter" }
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
        onElement(20_000L) { packageName == PackageNames.GOOGLE_MAPS }

        // If there is a Google Maps sign in screen, skip it
        onElementOrNull(3_000L) {
            packageName == PackageNames.GOOGLE_MAPS && @Suppress("SpellCheckingInspection") when (textAsString()) {
                "Make it your map", "Profitez d'une carte personnalisée" -> true
                else -> false
            }
        }?.let {
            onElement {
                packageName == PackageNames.GOOGLE_MAPS && when (textAsString()?.lowercase()) {
                    "skip", "ignorer" -> true
                    else -> false
                }
            }.click()
        }

        // Verify Google Maps content
        onElement(20_000L) { packageName == PackageNames.GOOGLE_MAPS && this.block() }
    }

    fun UiAutomatorTestScope.waitAndAssertTomTomContainsElement(block: AccessibilityNodeInfo.() -> Boolean) {
        // Wait for TomTom
        onElement(30_000L) { packageName == PackageNames.TOMTOM }

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
        onElement { packageName == PackageNames.TOMTOM && this.block() }
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

    fun UiAutomatorTestScope.goToInputsList() {
        goToMenuItem { viewIdResourceName == "geoShareMainMenuInputs" }
    }

    fun UiAutomatorTestScope.goToUserPreferencesList() {
        goToMenuItem { viewIdResourceName == "geoShareMainMenuUserPreferences" }
    }

    fun UiAutomatorTestScope.goToUserPreferencesDetail(groupId: UserPreferencesGroupId) {
        onElement { viewIdResourceName == "geoShareUserPreferencesListPane" }
            .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareUserPreferencesGroup_${groupId}" }
            .click()
    }

    fun UiAutomatorTestScope.goToMainScreenFromUserPreferencesDetail() {
        onElement { viewIdResourceName == "geoShareBack" }.click()
        if (
            onElementOrNull(1_000L) {
                viewIdResourceName == "geoShareLinksListPane" ||
                    viewIdResourceName == "geoShareUserPreferencesListPane"
            } != null
        ) {
            // On a non-tablet screen, we need to tap the back button one more time to get from the user preferences
            // list screen to the main screen
            onElement { viewIdResourceName == "geoShareBack" }.click()
        }
    }

    /**
     * Return the main screen scrollable element that contains app icons. Works on phone as well as tablet.
     */
    fun UiAutomatorTestScope.onMainScrollablePane(): UiObject2 =
        onElement {
            // First try supporting pane, which is displayed only on wide screens
            viewIdResourceName == "geoShareMainSupportingPane" ||
                // Then try the main pane, which is displayed on all devices but doesn't contain apps on wide screens
                viewIdResourceName == "geoShareMainPane"
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
