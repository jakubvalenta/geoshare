package page.ooooo.geoshare

import android.app.ActivityManager
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.onElement
import androidx.test.uiautomator.scrollToElement
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
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.AssumptionViolatedException
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.data.local.preferences.CoordinateFormat
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.calcExponentialBackoffMillis
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.CONNECT_TIMEOUT
import page.ooooo.geoshare.lib.network.REQUEST_TIMEOUT
import page.ooooo.geoshare.ui.UserPreferenceGroupId
import java.net.InetAddress
import java.net.SocketException
import java.net.UnknownHostException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

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
const val TOAST_TIMEOUT = 5_000L
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
    onElement { viewIdResourceName == "geoShareIntroScreenCloseButton" }.click()
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

private fun AccessibilityNodeInfo.isPermissionButtonOnlyThisTime(): Boolean =
    textAsString()?.lowercase() in setOf(
        "only this time",
        @Suppress("SpellCheckingInspection") "uniquement cette fois-ci",
    )

private fun AccessibilityNodeInfo.isPermissionButtonDoNotAllow(): Boolean =
    textAsString()?.lowercase() in setOf(
        "don't allow",
        "don’t allow", // Notice the different quote character
        @Suppress("SpellCheckingInspection") "ne pas autoriser"
    )

fun UiAutomatorTestScope.grantSystemPermission() {
    onElement { isPermissionButtonOnlyThisTime() }.click()
}

fun UiAutomatorTestScope.grantSystemPermissionIfNecessary() {
    onElementOrNull(3_000L) { isPermissionButtonOnlyThisTime() }?.click()
}

fun UiAutomatorTestScope.denySystemPermission() {
    onElement { isPermissionButtonDoNotAllow() }.click()
}

fun UiAutomatorTestScope.denySystemPermissionIfNecessary() {
    onElementOrNull(3_000L) { isPermissionButtonDoNotAllow() }?.click()
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
            "geoShareResultSuccessLastPointName" -> true
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
            setOf("Coordinates", @Suppress("SpellCheckingInspection") "Coordonnées")
        }
        onElement {
            if (viewIdResourceName == "geoShareResultSuccessLastPointName") {
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
                        """Expected "${textAsString()}" to equal one of ${expectedCoordinatesOptions.joinToString()}""",
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
        if (!(accurate ?: lastPoint.isAccurate())) {
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
            @Suppress("SpellCheckingInspection") "Profitez d'une carte personnalisée"
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

fun UiAutomatorTestScope.assertConversionSucceedsAnyCoordinates(timeoutMs: Long = NETWORK_TIMEOUT) {
    onElement(timeoutMs) {
        if (viewIdResourceName == "geoShareResultSuccessLastPointName") {
            assertTrue(
                @Suppress("SpellCheckingInspection") """Expected "${textAsString()}" to equal "Coordinates" or "Coordonnées""""",
                textAsString() in setOf(
                    "Coordinates", @Suppress("SpellCheckingInspection") "Coordonnées"
                ),
            )
            true
        } else {
            false
        }
    }
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

    // If there is location permission dialog, confirm it
    grantSystemPermissionIfNecessary()

    // If there is "Importing GPX tracks" dialog, confirm it
    onElementOrNull(5_000L) {
        textAsString() in setOf(
            "Got it",
            @Suppress("SpellCheckingInspection") "J'ai compris"
        )
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

fun UiAutomatorTestScope.testUriAnyCoordinates(
    unsafeUriString: String,
    timeoutMs: Long = NETWORK_TIMEOUT,
) {
    shareUri(unsafeUriString)
    quickWaitForStableInActiveWindow() // Wait for the result to render, because there might be the old result
    assertConversionSucceedsAnyCoordinates(timeoutMs)
}

fun UiAutomatorTestScope.testText(expectedPoints: Points, unsafeText: String) {
    // It would be preferable to test sharing of the text with the app, but this shell command doesn't work when
    // there are spaces in the text. So instead, we type the text in the main form of the app.
    // device.executeShellCommand(
    //     "am start -a android.intent.action.SEND -t text/plain -e android.intent.extra.TEXT $unsafeText -n ${BuildConfig.APPLICATION_ID}.debug/${BuildConfig.APPLICATION_ID}.ConversionActivity ${BuildConfig.APPLICATION_ID}.debug"
    // )

    // Go to main form, if we're on the result screen
    onElementOrNull(1_000L) { viewIdResourceName == "geoShareBack" }?.click()

    // Set main input
    val mainInput = onElement { viewIdResourceName == "geoShareMainSourceTextField" }
    mainInput.setText(unsafeText)
    quickWaitForStableInActiveWindow() // Wait for the submit button to get its final position, after setting text

    // Submit main form
    if (mainInput.isFocused) {
        // If the field is focused, the submit button can be covered by IME, so submit by pressing Enter
        pressEnter()
    } else {
        // If the field is not focused, then pressing Enter doesn't submit, so submit by clicking the submit button
        onElement { viewIdResourceName == "geoShareMainSubmitButton" }.click()
    }

    // Shows points
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

fun UiAutomatorTestScope.goToMainScreenFromUserPreferencesDetail() {
    onElement { viewIdResourceName == "geoShareBack" }.click()
    onElementOrNull(1_000L) {
        viewIdResourceName == "geoShareLinkListPane" || viewIdResourceName == "geoShareUserPreferencesListPane"
    }?.also {
        // On a non-tablet screen, we need to tap the back button one more time to get from the user preferences
        // list screen to the main screen
        onElement { viewIdResourceName == "geoShareBack" }.click()
    }
}

/**
 * Returns the scrollable element that contains app icons. Works on phone as well as tablet.
 */
fun UiAutomatorTestScope.onMainScrollablePane(): UiObject2 = onElement {
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
    onElement {
        textAsString()?.lowercase() in setOf(
            "save",
            @Suppress("SpellCheckingInspection") "enregistrer",
        )
    }.click()
}

fun UiAutomatorTestScope.insertOrEditContact(name: String = "GeoShare Test Contact") {
    // If using the Android open-source contacts app, click the search button
    onElementOrNull(3_000L) {
        packageName == "com.android.contacts" && contentDescription in setOf(
            "Search contacts",
            @Suppress("SpellCheckingInspection") "Rechercher dans vos contacts",
        )
    }?.click()
    type(name.split(' ').first())
    val existingContact = onElementOrNull(3_000L) { textAsString() == name }
    if (existingContact != null) {
        existingContact.click()
    } else {
        // If using the Android open-source contacts app, click the back button
        onElementOrNull(3_000L) {
            packageName == "com.android.contacts" && contentDescription in setOf(
                "stop searching",
                @Suppress("SpellCheckingInspection") "arrêter la recherche",
            )
        }?.click()
        onElement {
            textAsString() in setOf(
                "Create new contact",
                "Create a new contact",
                @Suppress("SpellCheckingInspection") "Créer un contact",
            )
        }.click()
        onElement { textAsString() in setOf("First name", "Prénom") }.setText(name)
    }
    onElement {
        textAsString()?.lowercase() in setOf(
            "save",
            @Suppress("SpellCheckingInspection") "enregistrer",
        )
    }.click()
}

fun UiAutomatorTestScope.openContact(name: String = "GeoShare Test Contact") {
    setOf("com.android.contacts", "com.google.android.contacts").first { packageName ->
        launchApplication(packageName)

        // If there is "Allow contacts to send you notifications" dialog, dismiss it
        denySystemPermissionIfNecessary()

        waitForAppToBeVisible(packageName, 3_000L)
    }

    // If there's a "Some menu items have moved..." popup, close it
    onElementOrNull(3_000L) { packageName == "com.google.android.contacts" && viewIdResourceName == "android:id/closeButton" }
        ?.click()

    val contactDetailOpen = onElementOrNull(3_000L) {
        packageName == "com.android.contacts" && viewIdResourceName == "com.android.contacts:id/menu_edit" ||
            packageName == "com.google.android.contacts" && viewIdResourceName == "com.google.android.contacts:id/menu_insert_or_edit"
    } != null
    if (contactDetailOpen) {
        // If the contacts app is already open on the contact detail screen, do nothing
    } else {
        // Scroll to the test contact in the list of contacts, and click it
        onElement { isScrollable }
            .scrollToElement(Direction.DOWN) { textAsString() == name && isVisibleToUser }
            .click()
    }
}

fun UiAutomatorTestScope.mockLocation(block: MockLocationScope.() -> Unit) {
    device.executeShellCommand(
        @Suppress("SpellCheckingInspection") "appops set ${BuildConfig.APPLICATION_ID} android:mock_location allow"
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

fun UiAutomatorTestScope.configureServer(testServer: TestServer) {
    // Go to server list
    goToUserPreferencesDetail(UserPreferenceGroupId.SERVERS)

    when (testServer) {
        is TestServer.Configured -> {
            // Insert a new server
            onElement { viewIdResourceName == "geoShareServerListInsert" }.click()
            fillAndSaveServerForm(testServer.server)

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

private const val SERVER_API_KEY_ARG = "SERVER_API_KEY"
