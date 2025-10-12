package page.ooooo.geoshare.lib

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.core.net.toUri
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import java.text.SimpleDateFormat
import java.util.*

class IntentTools {
    companion object {
        const val GOOGLE_MAPS_PACKAGE_NAME = "com.google.android.apps.maps"
    }

    data class App(val packageName: String, val label: String, val icon: Drawable)

    fun createViewIntent(packageName: String, data: Uri): Intent = Intent(Intent.ACTION_VIEW, data).apply {
        setPackage(packageName)
    }

    fun createChooserIntent(data: Uri): Intent = Intent.createChooser(
        Intent(Intent.ACTION_VIEW, data),
        "Choose an app",
    ).apply {
        putExtra(
            Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(
                ComponentName("page.ooooo.geoshare", "page.ooooo.geoshare.ConversionActivity"),
                ComponentName("page.ooooo.geoshare.debug", "page.ooooo.geoshare.ConversionActivity"),
            )
        )
    }

    fun getIntentUriString(intent: Intent): String? {
        when (val intentAction = intent.action) {
            Intent.ACTION_VIEW -> {
                val intentData: String? = intent.data?.toString()
                if (intentData == null) {
                    Log.w(null, "Missing intent data")
                    return null
                }
                return intentData
            }

            Intent.ACTION_SEND -> {
                val intentText = intent.getStringExtra("android.intent.extra.TEXT")
                if (intentText == null) {
                    Log.w(null, "Missing intent extra text")
                    return null
                }
                return intentText
            }

            else -> {
                Log.w(null, "Unsupported intent action $intentAction")
                return null
            }
        }
    }

    fun queryApp(packageManager: PackageManager, packageName: String): App? {
        val applicationInfo = try {
            packageManager.getApplicationInfo(packageName, 0)
        } catch (e: Exception) {
            Log.e(null, "Error when querying an app", e)
            return null
        }
        return try {
            App(
                applicationInfo.packageName,
                applicationInfo.loadLabel(packageManager).toString(),
                applicationInfo.loadIcon(packageManager),
            )
        } catch (e: Exception) {
            Log.e(null, "Error when loading info about an app", e)
            null
        }
    }

    fun queryGeoUriApps(packageManager: PackageManager): List<App> {
        val resolveInfos = try {
            packageManager.queryIntentActivities(
                Intent(Intent.ACTION_VIEW, "geo:".toUri()),
                PackageManager.MATCH_DEFAULT_ONLY,
            )
        } catch (e: Exception) {
            Log.e(null, "Error when querying apps that support geo: URIs", e)
            return emptyList()
        }
        return resolveInfos.mapNotNull {
            try {
                App(
                    it.activityInfo.packageName,
                    it.activityInfo.loadLabel(packageManager).toString(),
                    it.activityInfo.loadIcon(packageManager),
                )
            } catch (e: Exception) {
                Log.e(null, "Error when loading info about an app that supports geo: URIs", e)
                null
            }
        }.filterNot { it.packageName == BuildConfig.APPLICATION_ID }.sortedBy { it.label }
    }

    fun startActivity(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }

    fun openApp(context: Context, packageName: String, uriString: String): Boolean =
        startActivity(context, createViewIntent(packageName, uriString.toUri()))

    fun openChooser(context: Context, uriString: String): Boolean =
        startActivity(context, createChooserIntent(uriString.toUri()))

    fun launchSaveGpx(context: Context, saveGpxLauncher: ActivityResultLauncher<Intent>): Boolean {
        @Suppress("SpellCheckingInspection")
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(System.currentTimeMillis())
        val filename = context.resources.getString(
            R.string.conversion_succeeded_save_gpx_filename,
            context.resources.getString(R.string.app_name),
            timestamp,
        )
        return try {
            saveGpxLauncher.launch(
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/xml"
                    putExtra(Intent.EXTRA_TITLE, filename)
                }
            )
            true
        } catch (_: IllegalStateException) {
            // TODO Fix launchSaveGpx crash
            false
        }
    }

    fun isDefaultHandlerEnabled(packageManager: PackageManager, uriString: String): Boolean {
        val resolveInfo = try {
            packageManager.resolveActivity(
                Intent(Intent.ACTION_VIEW, uriString.toUri()),
                PackageManager.MATCH_DEFAULT_ONLY,
            )
        } catch (e: Exception) {
            Log.e(null, "Error when querying which app is the default handler for a URI", e)
            return false
        }
        val packageName = try {
            resolveInfo?.activityInfo?.packageName
        } catch (e: Exception) {
            Log.e(null, "Error when loading info about an app that is the default handler for URI", e)
            null
        }
        return packageName == BuildConfig.APPLICATION_ID
    }

    fun showOpenByDefaultSettings(
        context: Context,
        launcher: ActivityResultLauncher<Intent>,
    ) {
        showOpenByDefaultSettingsForPackage(context, launcher, BuildConfig.APPLICATION_ID)
    }

    fun showOpenByDefaultSettingsForPackage(
        context: Context,
        launcher: ActivityResultLauncher<Intent>,
        packageName: String,
    ) {
        try {
            val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                // Samsung supposedly doesn't allow going to the "Open by default" settings page.
                Build.MANUFACTURER.lowercase(Locale.ROOT) != "samsung"
            ) {
                Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
            } else {
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            }
            val intent = Intent(action, "package:$packageName".toUri())
            launcher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                R.string.intro_settings_activity_not_found,
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    suspend fun copyToClipboard(context: Context, clipboard: Clipboard, text: String) {
        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Geographic coordinates", text)))
        val systemHasClipboardEditor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        if (!systemHasClipboardEditor) {
            Toast.makeText(context, R.string.copying_finished, Toast.LENGTH_SHORT).show()
        }
    }
}
