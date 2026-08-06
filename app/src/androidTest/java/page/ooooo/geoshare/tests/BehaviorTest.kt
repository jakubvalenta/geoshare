package page.ooooo.geoshare.tests

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.io.PlatformTestStorageRegistry
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.onElement
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.scrollToElementOrNull
import androidx.test.uiautomator.textAsString
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.AssumptionViolatedException
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.data.local.preferences.Automation
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.calcExponentialBackoffMillis
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.formatters.GeoUriFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.network.CONNECT_TIMEOUT
import page.ooooo.geoshare.lib.network.REQUEST_TIMEOUT
import page.ooooo.geoshare.ui.UserPreferenceGroupId
import java.net.InetAddress
import java.net.SocketException
import java.net.UnknownHostException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.time.Duration.Companion.seconds

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

const val ELEMENT_DOES_NOT_EXIST_TIMEOUT = 500L
val TOAST_TIMEOUT = 5.seconds
const val MAX_ATTEMPTS = 10
val NETWORK_TIMEOUT = (1..MAX_ATTEMPTS).fold(CONNECT_TIMEOUT + REQUEST_TIMEOUT) { acc, curr ->
    acc + calcExponentialBackoffMillis(curr) + CONNECT_TIMEOUT + REQUEST_TIMEOUT
}

fun UiAutomatorTestScope.launchApplication(packageName: String = BuildConfig.APPLICATION_ID) {
    // Use shell command instead of startActivity() to support Xiaomi.
    device.executeShellCommand("monkey -p $packageName 1")
}

fun UiAutomatorTestScope.waitForAppToBeVisible(
    packageName: String = BuildConfig.APPLICATION_ID,
    timeoutMs: Long = 10_000L,
) {
    waitForAppToBeVisible(packageName, timeoutMs)
}

fun closeApplication() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val activityManager = context.getSystemService(ActivityManager::class.java)
    activityManager.appTasks.forEach { it.finishAndRemoveTask() }
}

fun UiAutomatorTestScope.closeIntro() {
    quickWaitForStableInActiveWindow() // Wait for the intro to render, otherwise closing it can fail even with large timeout
    onElementOrNull(3_000L) { viewIdResourceName == "geoShareIntroCloseButton" }?.click()
}

fun UiAutomatorTestScope.quickWaitForStableInActiveWindow() {
    waitForStableInActiveWindow(stableTimeoutMs = 1_000L, stableIntervalMs = 100L, requireStableScreenshot = false)
}

fun UiObject2.confirmDialog() {
    onElement { viewIdResourceName == "geoShareConfirmationDialogConfirmButton" }.click()
}

fun UiObject2.dismissDialog() {
    onElement { viewIdResourceName == "geoShareConfirmationDialogDismissButton" }.click()
}

fun UiObject2.toggleDoNotAsk() {
    onElement { viewIdResourceName == "geoShareConfirmationDialogDoNotAskSwitch" }.click()
}

private fun AccessibilityNodeInfo.isGrantPermissionButton(): Boolean =
    textAsString()?.lowercase() in setOf(
        "allow",
        "only this time",
        @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "uniquement cette fois-ci",
    )

private fun AccessibilityNodeInfo.isDenyPermissionButton(): Boolean =
    textAsString()?.lowercase() in setOf(
        "deny",
        "don't allow",
        "don’t allow", // Notice the different quote character
        "ne pas autoriser",
    )

fun UiAutomatorTestScope.isSystemPermissionShown(): Boolean =
    onElementOrNull(3_000L) { isGrantPermissionButton() } != null

fun UiAutomatorTestScope.grantSystemPermission() {
    onElement { isGrantPermissionButton() }.click()
}

fun UiAutomatorTestScope.denySystemPermission() {
    onElement { isDenyPermissionButton() }.click()
}

fun UiAutomatorTestScope.isAppInstalled(packageName: String): Boolean =
    device.executeShellCommand("pm path $packageName").isNotEmpty()

fun UiAutomatorTestScope.assumeAppInstalled(packageName: String) {
    assumeTrue(
        "This test only works when $packageName is installed on the device",
        isAppInstalled(packageName),
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

suspend fun assumeHttpGetReturnsStatus(@Suppress("SameParameterValue") url: String, status: HttpStatusCode) {
    val resStatus = try {
        withContext(Dispatchers.IO) {
            HttpClient(CIO).use { client ->
                client.get(url).status
            }
        }
    } catch (_: SocketException) {
        null
    }
    assumeTrue(
        "This test only works when HTTP GET request returns 404 but it ${if (resStatus != null) "was ${resStatus.value}" else "timed out"} for $url",
        resStatus == status,
    )
}

fun assumeNotEmulator() {
    assumeTrue("This test only works on a physical device, not an emulator", Build.HARDWARE != "ranchu")
}

/**
 * Check that the result screen shows [expectedPoints]
 *
 * Point name is checked in a fuzzy way. It is enough if the shown name contains the expected name. We need this,
 * because we often cannot use an exact match, because Google Maps returns different place name depending on the
 * phone's language and location.
 */
fun UiAutomatorTestScope.assertConversionSucceeds(
    expectedPoints: Points,
    fallbackNames: Set<String> = emptySet(),
    accurate: Boolean? = null,
    timeoutMs: Long = NETWORK_TIMEOUT,
) {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val geometries = Geometries(context)
    val coordinateConverter = CoordinateConverter(geometries)

    onElement(timeoutMs) {
        when (viewIdResourceName) {
            "geoShareResultLastPointName" -> true
            "geoShareConversionErrorMessage" -> throw AssertionError("Conversion failed: ${textAsString()}")
            else -> false
        }
    }
    val lastPoint = expectedPoints.lastOrNull() ?: return
    lastPoint.cleanName.let { expectedName ->
        val expectedNames = if (!expectedName.isNullOrEmpty()) {
            setOf(expectedName) + fallbackNames
        } else if (expectedPoints.size > 1) {
            setOf("Last point", "Dernier point")
        } else {
            setOf("Coordinates", @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Coordonnées")
        }
        onElement {
            if (viewIdResourceName == "geoShareResultLastPointName") {
                assertTrue(
                    """Expected "${textAsString()}" to equal one of ${expectedNames.joinToString()}""",
                    textAsString() in expectedNames,
                )
                true
            } else {
                false
            }
        }
    }
    if (lastPoint.hasCoordinates()) {
        val expectedCoordinatesOptions = CoordinateFormat.entries.map { coordinateFormat ->
            when (coordinateFormat) {
                CoordinateFormat.DEC -> CoordinateFormatter.formatDecCoords(
                    coordinateConverter.toWGS84(lastPoint)
                )

                CoordinateFormat.DEG_MIN_SEC -> CoordinateFormatter.formatDegMinSecCoords(
                    coordinateConverter.toWGS84(lastPoint)
                )
            }
        }
        onElement {
            if (viewIdResourceName == "geoShareResultLastPointCoordinates") {
                assertTrue(
                    """Expected "${textAsString()}" to equal one of ${expectedCoordinatesOptions.joinToString()}""",
                    textAsString() in expectedCoordinatesOptions,
                )
                true
            } else {
                false
            }
        }
    } else {
        onElement { viewIdResourceName == "geoShareResultCheckNameOnly" }
    }
    lastPoint.source.let { expectedSource ->
        onElement { viewIdResourceName == "geoShareResultLastPointSource_${expectedSource}" }
        if (!(accurate ?: lastPoint.isAccurate())) {
            onElement { viewIdResourceName == "geoShareResultCheckSRS" }
        } else if (expectedSource == Source.JAVASCRIPT) {
            onElement { viewIdResourceName == "geoShareResultCheckExperimental" }
        } else if (expectedSource == Source.MAP_CENTER) {
            onElement { viewIdResourceName == "geoShareResultCheckMapCenter" }
        }
    }
    if (expectedPoints.size > 1) {
        // Notice that we don't test the coordinates of the points but only their number
        val expectedPointsNumber = "(${expectedPoints.size})"
        onElement {
            if (viewIdResourceName == "geoShareResultPointsHeadline") {
                assertTrue(
                    """Expected "${textAsString()}" to contain "$expectedPointsNumber"""",
                    textAsString()?.contains(expectedPointsNumber) == true,
                )
                true
            } else {
                false
            }
        }
        if (expectedPoints.any { !it.hasCoordinates() }) {
            onElement { viewIdResourceName == "geoShareResultCheckNameOnlyPoints" }
        }
    }
}

fun UiAutomatorTestScope.assertConversionSucceeds(
    expectedPoint: Point,
    fallbackNames: Set<String> = emptySet(),
    accurate: Boolean? = null,
    timeoutMs: Long = NETWORK_TIMEOUT,
) = assertConversionSucceeds(persistentListOf(expectedPoint), fallbackNames, accurate, timeoutMs)

fun UiAutomatorTestScope.waitAndAssertGoogleMapsContainsElement(block: AccessibilityNodeInfo.() -> Boolean) {
    // Wait for Google Maps
    onElement(20_000L) { packageName == PackageNames.GOOGLE_MAPS }

    // If there is a Google Maps sign in screen, skip it
    onElementOrNull(3_000L) {
        packageName == PackageNames.GOOGLE_MAPS && textAsString() in setOf(
            "Make it your map",
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Profitez d'une carte personnalisée"
        )
    }?.let {
        onElement {
            packageName == PackageNames.GOOGLE_MAPS && textAsString()?.lowercase() in setOf(
                "skip",
                "ignorer",
            )
        }.click()
    }

    // Verify Google Maps content
    onElement(20_000L) { packageName == PackageNames.GOOGLE_MAPS && this.block() }
}

fun UiAutomatorTestScope.assertConversionFails(expectedMessage: Set<String>, timeoutMs: Long = NETWORK_TIMEOUT) {
    onElement(timeoutMs) {
        if (viewIdResourceName == "geoShareConversionErrorMessage") {
            assertTrue(
                """Expected "${textAsString()}" to equal one of ${expectedMessage.joinToString()}""",
                textAsString() in expectedMessage,
            )
            true
        } else {
            false
        }
    }
}

fun UiAutomatorTestScope.waitAndAssertTomTomContainsElement(block: AccessibilityNodeInfo.() -> Boolean) {
    // Wait for TomTom
    onElement(30_000L) { packageName == PackageNames.TOMTOM }

    // If there is a location permission dialog, confirm it
    if (isSystemPermissionShown()) {
        grantSystemPermission()
    }

    // If there is an "Importing GPX tracks" dialog, confirm it
    onElementOrNull(5_000L) {
        textAsString() in setOf(
            "Got it",
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "J'ai compris"
        )
    }?.click()

    // Verify TomTom content
    onElement { packageName == PackageNames.TOMTOM && this.block() }
}

fun UiAutomatorTestScope.shareUri(unsafeUriString: String = "geo:52.47254,13.4345") {
    // Use shell command instead of startActivity() to support Xiaomi
    device.executeShellCommand(
        "am start -a android.intent.action.VIEW -d $unsafeUriString -n ${BuildConfig.APPLICATION_ID}/page.ooooo.geoshare.ConversionActivity ${BuildConfig.APPLICATION_ID}"
    )
}

fun UiAutomatorTestScope.shareRandomUri(): WGS84Point {
    val point = WGS84Point(NaivePoint.genRandomPoint())
    shareUri(GeoUriFormatter.formatGeoUriString(point))
    return point
}

fun UiAutomatorTestScope.configureConnectionPermissionPreference(permission: Permission) {
    goToUserPreferencesDetail(UserPreferenceGroupId.CONNECTION_PERMISSION)
    onElement { viewIdResourceName == "geoShareUserPreferenceConnectionPermission_$permission" }.click()
}

fun UiAutomatorTestScope.testUri(
    expectedPoints: Points,
    unsafeUriString: String,
    fallbackNames: Set<String> = emptySet(),
    accurate: Boolean? = null,
    timeoutMs: Long = NETWORK_TIMEOUT,
) {
    shareUri(unsafeUriString)
    quickWaitForStableInActiveWindow() // Wait for the result to render, because there might be the old result
    assertConversionSucceeds(expectedPoints, fallbackNames, accurate, timeoutMs)
}

fun UiAutomatorTestScope.testUri(
    expectedPoint: Point,
    unsafeUriString: String,
    fallbackNames: Set<String> = emptySet(),
    accurate: Boolean? = null,
    timeoutMs: Long = NETWORK_TIMEOUT,
) = testUri(persistentListOf(expectedPoint), unsafeUriString, fallbackNames, accurate, timeoutMs)

fun UiAutomatorTestScope.testUriFails(
    expectedMessage: Set<String>,
    unsafeUriString: String,
    timeoutMs: Long = NETWORK_TIMEOUT,
) {
    shareUri(unsafeUriString)
    quickWaitForStableInActiveWindow() // Wait for the result to render, because there might be the old result
    assertConversionFails(expectedMessage, timeoutMs)
}

fun UiAutomatorTestScope.setMainInput(unsafeText: String = "geo:52.47254,13.4345") {
    // Set main input
    onElement { viewIdResourceName == "geoShareMainSourceTextField" }.setText(unsafeText)
    quickWaitForStableInActiveWindow() // Wait for the submit button to get its final position, after setting text

    // Submit main form
    onElement { viewIdResourceName == "geoShareMainSourceTextField" }.let { textField ->
        // Get the text field again to prevent stale element
        if (textField.isFocused) {
            // If the field is focused, the submit button can be covered by IME, so submit by pressing Enter
            pressEnter()
        } else {
            // If the field is not focused, then pressing Enter doesn't submit, so submit by clicking the submit button
            onElement { viewIdResourceName == "geoShareMainSubmitButton" }.click()
        }
    }
}

/**
 * Test the conversion of a text source (e.g. coordinates string or Plus Codes) to coordinates.
 *
 * Enter the text in the form on the main screen. It would be faster to share the text with the app using
 * `am start ... android.intent.action.SEND`, but unfortunately that command doesn't work when there are spaces in the
 * text.
 */
fun UiAutomatorTestScope.testText(expectedPoints: Points, unsafeText: String) {
    goBackToMainForm()
    setMainInput(unsafeText)
    assertConversionSucceeds(expectedPoints)
}

fun UiAutomatorTestScope.testText(expectedPoint: Point, unsafeText: String) =
    testText(persistentListOf(expectedPoint), unsafeText)

fun UiAutomatorTestScope.goToInputList() {
    // If we're on the main screen, use the main menu
    onElementOrNull(1_000) { viewIdResourceName == "geoShareMainMenuButton" }?.let { mainMenu ->
        mainMenu.click()
        onElement { viewIdResourceName == "geoShareMainMenuInputs" }.click()
    }
}

fun UiAutomatorTestScope.goToUserPreferencesDetail(groupId: UserPreferenceGroupId) {
    // If we're on the main screen, use the main menu
    onElementOrNull(1_000) { viewIdResourceName == "geoShareMainMenuButton" }?.let { mainMenu ->
        mainMenu.click()
        onElement { viewIdResourceName == "geoShareMainMenuUserPreferences" }.click()
    } ?: run {
        // If we're on the detail screen, go back
        onElementOrNull(1_000) { viewIdResourceName == "geoShareUserPreferencesControlsPane" }?.also {
            onElement { viewIdResourceName == "geoShareBack" }.click()
        }
    }
    onElement { viewIdResourceName == "geoShareUserPreferencesListPane" }
        .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareUserPreferencesGroup_${groupId}" }
        .click()
}

fun UiAutomatorTestScope.goBackToElement(block: AccessibilityNodeInfo.() -> Boolean): UiObject2 {
    repeat(4) {
        val element = onElementOrNull(1_000, block = block)
        if (element != null) {
            // We've reached the desired screen
            return element
        } else {
            // Go back
            onElement { viewIdResourceName == "geoShareBack" }.click()
            quickWaitForStableInActiveWindow() // Wait for the screen to render
        }
    }
    throw IllegalStateException("Exceeded max back steps")
}

fun UiAutomatorTestScope.goBackToMainForm() = goBackToElement { viewIdResourceName == "geoShareMainSourceTextField" }

/**
 * Returns the scrollable element that contains the app icons on the result screen.
 *
 * Works on phone as well as tablet.
 */
fun UiAutomatorTestScope.onMainScrollablePane(): UiObject2 = onElement {
    // First try supporting pane, which is displayed only on wide screens
    viewIdResourceName == "geoShareMainSupportingPane" ||
        // Then try the main pane, which is displayed on all devices but doesn't contain apps on wide screens
        viewIdResourceName == "geoShareMainPane"
}

/**
 * Scrolls to and returns an [automation] item on the automation preferences screen.
 */
fun UiAutomatorTestScope.scrollToAutomationItem(automation: Automation): UiObject2 =
    onElement { viewIdResourceName == "geoShareUserPreferencesControlsPane" }
        .scrollToElement(Direction.DOWN, 20_000L) {
            viewIdResourceName == "geoShareUserPreferenceAutomation_${Json.encodeToString<Automation>(automation)}"
        }

fun UiAutomatorTestScope.launchNavigationInApp(@Suppress("SameParameterValue") packageName: String) {
    onElement { viewIdResourceName == "geoShareApp_$packageName" }.longClick()
    onElement {
        viewIdResourceName == "geoShareAppOutput" && textAsString() in setOf(
            "Navigate",
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Naviguer"
        )
    }.click()
}

fun UiObject2.expandSheet() {
    swipe(Direction.UP, 1f)
}

fun UiObject2.longScrollSheet(direction: Direction = Direction.DOWN) {
    scroll(direction, 10f)
}

fun UiObject2.scrollToSheetItem(
    direction: Direction = Direction.DOWN,
    block: AccessibilityNodeInfo.() -> Boolean,
): UiObject2 =
    scrollToElement(direction) { viewIdResourceName == "geoShareResultSheetItemHeadline" && block() }

fun UiAutomatorTestScope.chooseFile() {
    if (onElementOrNull(3_000L) { textAsString() == "Recent" } != null) {
        // If we happen to be in the Recent directory, go to Downloads, because it's not possible to save to Recent
        device.click(50, 100) // Tap the hamburger menu
        onElement { textAsString() == "Downloads" }.click()
    } else {
        onElement {
            textAsString() == "Downloads" ||
                textAsString()?.startsWith("Files in") == true ||
                textAsString()?.startsWith(
                    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                    "Fichiers dans le dossier"
                ) == true
        }
    }
    onElement {
        textAsString()?.lowercase() in setOf(
            "save",
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "enregistrer",
        )
    }.click()
}

fun UiAutomatorTestScope.findContact(name: String): UiObject2? {
    // If using the Android open-source contacts app, click the search button
    onElementOrNull(3_000L) {
        packageName == "com.android.contacts" && contentDescription in setOf(
            "Search",
            "Search contacts",
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Rechercher dans vos contacts",
        )
    }?.click()

    // Search contacts
    val searchField = onElementOrNull(3_000L) {
        packageName == "com.android.contacts" && viewIdResourceName == "android:id/search_src_text"
    }
    val searchTerm = name.split(' ').first()
    if (searchField != null) {
        searchField.setText(searchTerm)
    } else {
        type(searchTerm)
    }

    // Return the found contact if it's immediately visible
    val foundContact = onElementOrNull(3_000L) { textAsString() == name && isVisibleToUser }
    if (foundContact != null) {
        return foundContact
    }

    // Scroll to the found contact
    return onElementOrNull(3_000L) { isScrollable }
        ?.scrollToElementOrNull(Direction.DOWN, 45_000L) { textAsString() == name && isVisibleToUser }
}

fun UiAutomatorTestScope.insertOrEditContact(name: String = "GeoShare Test Contact") {
    val existingContact = findContact(name)
    if (existingContact != null) {
        // Select an existing contact
        existingContact.click()
    } else {
        // If using the Android open-source contacts app, click the back button
        onElementOrNull(3_000L) {
            packageName == "com.android.contacts" && contentDescription in setOf(
                "Navigate up",
                "stop searching",
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "arrêter la recherche",
            )
        }?.click()

        // Create a new contact
        onElement {
            textAsString() in setOf(
                "Create new contact",
                "Create a new contact",
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Créer un contact",
            )
        }.click()

        // If there is an "Add account" dialog, dismiss it
        onElementOrNull(3_000L) { textAsString()?.lowercase() == "keep local" }?.click()

        // Fill name
        onElement { textAsString() in setOf("First name", "Name", "Prénom") }.setText(name)
    }

    // Save the contact
    onElement {
        packageName == "com.android.contacts" && contentDescription == "Save" ||
            textAsString()?.lowercase() in setOf(
            "save",
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "enregistrer",
        )
    }.click()
}

fun UiAutomatorTestScope.openContact(name: String = "GeoShare Test Contact") {
    setOf("com.android.contacts", "com.google.android.contacts").first { packageName ->
        launchApplication(packageName)

        // If there is an "Allow contacts to send you notifications" dialog, dismiss it
        if (isSystemPermissionShown()) {
            denySystemPermission()
        }

        waitForAppToBeVisible(packageName, 3_000L)
    }

    // If there is a "Some menu items have moved..." popup, close it
    onElementOrNull(3_000L) { packageName == "com.google.android.contacts" && viewIdResourceName == "android:id/closeButton" }
        ?.click()

    val contactDetailOpen = onElementOrNull(3_000L) {
        packageName == "com.android.contacts" && viewIdResourceName == "com.android.contacts:id/menu_edit" ||
            packageName == "com.google.android.contacts" && viewIdResourceName == "com.google.android.contacts:id/menu_insert_or_edit"
    } != null
    if (contactDetailOpen) {
        // If the contacts app is already open on the contact detail screen, do nothing
        return
    }

    // Find contact
    val contact = findContact(name)
    assertNotNull(contact)
    contact?.click()

    // If there are permission dialogs, dismiss them
    var i = 0
    while (isSystemPermissionShown() && i < 3) {
        denySystemPermission()
        i++
    }
}

fun UiAutomatorTestScope.assertContactContainsText(expectedText: String) {
    // Expand the contact field list
    with(device) {
        // Swipe to reveal the "See all" button, because the contacts app crashes, if we click the button when it's
        // not visible
        repeat(2) {
            swipe(displayWidth / 2, displayHeight / 2, displayWidth / 2, 0, 10)
            quickWaitForStableInActiveWindow() // Wait for the swiping to finish
        }
    }
    onElementOrNull(3_000L) { packageName == "com.android.contacts" && textAsString() == "See all" }?.click()

    // Assert
    onElement { isScrollable }.scrollToElement(Direction.DOWN) { textAsString() == expectedText }
}

fun UiAutomatorTestScope.mockLocation(block: MockLocationScope.() -> Unit) {
    device.executeShellCommand(
        @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
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

sealed interface TestServer {
    data class Configured(val server: Server) : TestServer
    object None : TestServer
}

sealed interface TestServerParams {
    data class Configured(
        val baseUrl: String,
        val name: String = "",
        val urlTemplate: String = "",
        val authType: ServerAuthType = ServerAuthType.API_KEY,
        val apiKeyHeader: String = "",
        val challengeUrl: String = "",
        val loginUrl: String = "",
        val registerUrl: String = "",
    ) : TestServerParams {
        override fun toString() = name
    }

    object None : TestServerParams {
        override fun toString() = "No server"
    }
}

fun TestServerParams.getAndAssumeTestServer(): TestServer = when (this) {
    is TestServerParams.Configured ->
        when (authType) {
            ServerAuthType.API_KEY -> {
                val apiKey = InstrumentationRegistry.getArguments().getString(SERVER_API_KEY_ARG)
                    ?: throw AssumptionViolatedException("This test only works when the instrumentation extra argument $SERVER_API_KEY_ARG is set")
                runBlocking {
                    assumeHttpGetReturnsStatus(baseUrl, HttpStatusCode.NotFound)
                }
                TestServer.Configured(
                    Server(
                        name = name,
                        urlTemplate = urlTemplate,
                        authType = authType,
                        apiKey = apiKey,
                        apiKeyHeader = apiKeyHeader,
                    )
                )
            }

            ServerAuthType.ATTESTATION -> {
                assumeNotEmulator()
                runBlocking {
                    assumeHttpGetReturnsStatus(baseUrl, HttpStatusCode.NotFound)
                }
                TestServer.Configured(
                    Server(
                        name = name,
                        urlTemplate = urlTemplate,
                        authType = authType,
                        challengeUrl = challengeUrl,
                        loginUrl = loginUrl,
                        registerUrl = registerUrl,
                    )
                )
            }
        }

    is TestServerParams.None -> TestServer.None
}

fun UiAutomatorTestScope.fillServerForm(server: Server) {
    server.name.takeIf { it.isNotEmpty() }?.let {
        onElement { viewIdResourceName == "geoShareServerFormName" }.setText(it)
    }
    server.urlTemplate.takeIf { it.isNotEmpty() }?.let {
        onElement { viewIdResourceName == "geoShareServerFormUrlTemplate" }.setText(it)
    }
    onElement { viewIdResourceName == "geoShareServerDetailPane" }.let { pane ->
        when (server.authType) {
            ServerAuthType.API_KEY -> {
                server.apiKeyHeader.takeIf { it.isNotEmpty() }?.let {
                    pane.scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormApiKeyHeader" }
                        .setText(it)
                }
                server.apiKey.takeIf { it.isNotEmpty() }?.let {
                    pane.scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormApiKey" }
                        .setText(it)
                }
            }

            ServerAuthType.ATTESTATION -> {
                pane.scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormAuthType_${ServerAuthType.API_KEY}" }
                    .click()
                onElement { viewIdResourceName == "geoShareDropdownFieldMenuItem_${ServerAuthType.ATTESTATION}" }.click()
                server.challengeUrl.takeIf { it.isNotEmpty() }?.let {
                    pane.scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormChallengeUrl" }
                        .setText(it)
                }
                server.loginUrl.takeIf { it.isNotEmpty() }?.let {
                    pane.scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormLoginUrl" }
                        .setText(it)
                }
                server.registerUrl.takeIf { it.isNotEmpty() }?.let {
                    pane.scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormRegisterUrl" }
                        .setText(it)
                }
            }
        }
    }
}

fun UiAutomatorTestScope.saveServerForm() {
    onElement { viewIdResourceName == "geoShareServerDetailPane" }
        .scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerFormSave" }
        .click()
}

fun UiAutomatorTestScope.configureServer(testServer: TestServer) {
    // Go to server list
    goToUserPreferencesDetail(UserPreferenceGroupId.SERVERS)

    when (testServer) {
        is TestServer.Configured -> {
            // Insert a new server
            onElement { viewIdResourceName == "geoShareServerListInsert" }.click()
            fillServerForm(testServer.server)
            saveServerForm()

            // Wait for the insert toast to disappear, because it covers the radio buttons
            runBlocking {
                delay(TOAST_TIMEOUT)
            }

            // Select the server
            onElement { viewIdResourceName == "geoShareServerListPane" }.apply {
                scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerListItem_GoogleMapsAddress_${testServer.server.name}" }.click()
                scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerListItem_GoogleMapsPlace_${testServer.server.name}" }.click()
            }
        }

        is TestServer.None -> {
            // Select no server
            onElement { viewIdResourceName == "geoShareServerListPane" }.apply {
                scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerListItem_GoogleMapsAddress_null" }.click()
                scrollToElement(Direction.DOWN) { viewIdResourceName == "geoShareServerListItem_GoogleMapsPlace_null" }.click()
            }
        }
    }
}

fun UiAutomatorTestScope.enableSystemUIDemoMode() {
    device.executeShellCommand("settings put global sysui_demo_allowed 1")
    device.executeShellCommand("am broadcast -a com.android.systemui.demo -e command enter")
    device.executeShellCommand(
        @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
        "am broadcast -a com.android.systemui.demo -e command clock -e hhmm 1200"
    )
    device.executeShellCommand("am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false")
    device.executeShellCommand("am broadcast -a com.android.systemui.demo -e command network -e fully true -e wifi show -e level 4")
    device.executeShellCommand("am broadcast -a com.android.systemui.demo -e command notifications -e visible false")
}

fun UiAutomatorTestScope.disableSystemUIDemoMode() {
    device.executeShellCommand("am broadcast -a com.android.systemui.demo -e command exit")
}

fun UiAutomatorTestScope.enableDarkMode() {
    device.executeShellCommand(
        @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
        "cmd uimode night yes"
    )
}

fun UiAutomatorTestScope.disableDarkMode() {
    device.executeShellCommand(
        @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
        "cmd uimode night no"
    )
}

fun UiAutomatorTestScope.setAppLocales(locales: String) {
    device.executeShellCommand(
        "cmd locale set-app-locales ${BuildConfig.APPLICATION_ID} --user current --locales $locales"
    )
}

/**
 * Take screenshot and write it to [androidx.test.platform.io.PlatformTestStorage] under [name].
 */
fun UiAutomatorTestScope.saveScreenshot(name: String) {
    uiAutomation.takeScreenshot().apply {
        PlatformTestStorageRegistry.getInstance().openOutputFile("$name.webp").use {
            compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, it)
        }
    }
}

/**
 * Run [block] and if it throws [AssertionError], then run it one more time.
 */
fun retryTest(block: () -> Unit) {
    try {
        block()
    } catch (_: AssertionError) {
        block()
    }
}

private const val SERVER_API_KEY_ARG = "SERVER_API_KEY"
