package page.ooooo.geoshare

import android.app.ActivityManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry
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
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
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

abstract class BaseActivityBehaviorTest {

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

    protected fun launchApplication() = uiAutomator {
        // Use shell command instead of startActivity() to support Xiaomi.
        device.executeShellCommand("monkey -p ${BuildConfig.APPLICATION_ID} 1")

        // Wait for the app to appear
        waitForAppToBeVisible(BuildConfig.APPLICATION_ID)
    }

    protected fun closeApplication() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val activityManager = context.getSystemService(ActivityManager::class.java)
        activityManager.appTasks.forEach { it.finishAndRemoveTask() }
    }

    protected fun closeIntro() = uiAutomator {
        quickWaitForStableInActiveWindow() // Wait for the intro to render, otherwise closing it can fail even with large timeout
        onElement { viewIdResourceName == "geoShareIntroScreenCloseButton" }.click()
    }

    protected fun quickWaitForStableInActiveWindow() = uiAutomator {
        waitForStableInActiveWindow(stableTimeoutMs = 1000L, stableIntervalMs = 100L, requireStableScreenshot = false)
    }

    protected fun onDialog(
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

    protected fun grantLocationPermission() = uiAutomator {
        onElement { isLocationGrantButton(this) }.click()
    }

    protected fun grantLocationPermissionIfNecessary() = uiAutomator {
        onElementOrNull(3_000L) { isLocationGrantButton(this) }?.click()
    }

    protected fun denyLocationPermission() = uiAutomator {
        onElement {
            @Suppress("SpellCheckingInspection") when (textAsString()?.lowercase()) {
                "don't allow", "don’t allow", "ne pas autoriser" -> true
                else -> false
            }
        }.click()
    }

    protected fun assumeAppInstalled(packageName: String) = uiAutomator {
        assumeTrue(
            "This test only works when $packageName is installed on the device",
            device.executeShellCommand("pm path $packageName").isNotEmpty(),
        )
    }

    protected suspend fun assumeDomainResolvable(
        @Suppress("SameParameterValue") domain: String,
        timeoutMs: Long = 1_000L,
    ) {
        // Use futures, because InetAddress.getByName() is not cancellable using simple Kotlin withTimeout()
        val executor = Executors.newSingleThreadExecutor()
        val future = executor.submit<InetAddress> {
            InetAddress.getByName(domain)
        }
        val success = try {
            withContext(Dispatchers.IO) {
                future.get(timeoutMs, TimeUnit.MILLISECONDS)
            }
            true
        } catch (_: TimeoutException) {
            future.cancel(true)
            false
        } catch (_: UnknownHostException) {
            false
        } finally {
            executor.shutdownNow()
        }
        assumeTrue("This test only works when DNS resolves the domain $domain", success)
    }

    protected suspend fun assumeHttpHeadIsSuccess(@Suppress("SameParameterValue") url: String) {
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
    protected fun assertConversionSucceeded(expectedPoints: ImmutableList<Point>, timeoutMs: Long = NETWORK_TIMEOUT) =
        uiAutomator {
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
            val expectedCoords = expectedPoints
                .lastOrNull()
                ?.takeIf { it.hasCoordinates() }
                ?.let { CoordsFormat.formatDegMinSecCoords(it) }
            if (expectedCoords != null) {
                onElement {
                    if (viewIdResourceName == "geoShareResultSuccessLastPointCoordinates") {
                        assertEquals(expectedCoords, textAsString())
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

    protected fun assertConversionSucceeded(expectedPoint: Point, timeoutMs: Long = NETWORK_TIMEOUT) =
        assertConversionSucceeded(persistentListOf(expectedPoint), timeoutMs)

    protected fun waitAndAssertGoogleMapsContainsElement(block: AccessibilityNodeInfo.() -> Boolean) = uiAutomator {
        // Wait for Google Maps
        onElement(20_000L) { packageName == GOOGLE_MAPS_PACKAGE_NAME }

        // If there is a Google Maps sign in screen, skip it
        onElementOrNull(3_000L) {
            packageName == GOOGLE_MAPS_PACKAGE_NAME && @Suppress("SpellCheckingInspection") when (textAsString()) {
                "Make it your map", "Profitez d'une carte personnalisée" -> true
                else -> false
            }
        }?.also {
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

    protected fun waitAndAssertTomTomContainsElement(block: AccessibilityNodeInfo.() -> Boolean) = uiAutomator {
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

    protected fun shareUri(unsafeUriString: String) = uiAutomator {
        // Use shell command instead of startActivity() to support Xiaomi
        device.executeShellCommand(
            @Suppress("SpellCheckingInspection") "am start -a android.intent.action.VIEW -d $unsafeUriString -n ${BuildConfig.APPLICATION_ID}/page.ooooo.geoshare.ConversionActivity ${BuildConfig.APPLICATION_ID}"
        )
    }

    protected fun goToMenuItem(block: AccessibilityNodeInfo.() -> Boolean) = uiAutomator {
        onElement { viewIdResourceName == "geoShareMainMenuButton" }.click()
        onElement(block = block).click()
    }

    protected fun goToInputsScreen() {
        goToMenuItem { viewIdResourceName == "geoShareMainMenuInputs" }
    }

    protected fun goToUserPreferencesScreen() {
        goToMenuItem { viewIdResourceName == "geoShareMainMenuUserPreferences" }
    }

    protected fun goToUserPreferencesDetailConnectionPermissionScreen() = uiAutomator {
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferencesGroupId.CONNECTION_PERMISSION}" }.click()
    }

    protected fun goToUserPreferencesDetailDeveloperScreen() = uiAutomator {
        goToUserPreferencesScreen()
        onElement { viewIdResourceName == "geoShareUserPreferencesGroup_${UserPreferencesGroupId.DEVELOPER_OPTIONS}" }.click()
    }

    protected fun chooseFile() = uiAutomator {
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
}
