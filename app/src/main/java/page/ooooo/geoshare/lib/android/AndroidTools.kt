package page.ooooo.geoshare.lib.android

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.core.content.FileProvider
import androidx.core.location.LocationListenerCompat
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.WGS84Point
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

object AndroidTools {

    enum class AppType { GEO_URI, GPX, MAGIC_EARTH }

    data class App(val packageName: String, val type: AppType)

    data class AppDetails(val packageName: String, val label: String, val icon: Drawable)

    fun getIntentUriString(intent: Intent): String? =
        when (val intentAction = intent.action) {
            Intent.ACTION_VIEW -> {
                val intentData: String? = intent.data?.toString()
                if (intentData == null) {
                    Log.w(null, "Missing intent data")
                    null
                } else {
                    intentData
                }
            }

            Intent.ACTION_SEND -> {
                val intentText = intent.getStringExtra("android.intent.extra.TEXT")
                if (intentText == null) {
                    Log.w(null, "Missing intent extra text")
                    null
                } else {
                    intentText
                }
            }

            else -> {
                Log.w(null, "Unsupported intent action $intentAction")
                null
            }
        }

    private fun queryAppDetailsWithoutCache(packageManager: PackageManager, packageName: String): AppDetails? {
        val applicationInfo = try {
            packageManager.getApplicationInfo(packageName, 0)
        } catch (e: Exception) {
            Log.e(null, "Error when querying an app", e)
            return null
        }
        return try {
            AppDetails(
                applicationInfo.packageName,
                applicationInfo.loadLabel(packageManager).toString(),
                applicationInfo.loadIcon(packageManager),
            )
        } catch (e: Exception) {
            Log.e(null, "Error when loading info about an app", e)
            null
        }
    }

    private val appDetailsCache: HashMap<String, AppDetails> = hashMapOf()

    fun queryAppDetails(packageManager: PackageManager, packageName: String): AppDetails? =
        appDetailsCache[packageName] ?: queryAppDetailsWithoutCache(packageManager, packageName)
            ?.also { appDetailsCache[packageName] = it }

    private fun queryPackageNames(packageManager: PackageManager, intent: Intent): List<String> {
        val resolveInfos = try {
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        } catch (e: Exception) {
            Log.e(null, "Error when querying installed apps", e)
            return emptyList()
        }
        return resolveInfos.mapNotNull { resolveInfo ->
            val packageName = try {
                resolveInfo.activityInfo.packageName
            } catch (e: Exception) {
                Log.e(null, "Error when loading info about an installed app", e)
                null
            }
            packageName?.takeUnless { it == BuildConfig.APPLICATION_ID }
        }
    }

    fun queryApps(packageManager: PackageManager): List<App> = buildList {
        val seenPackageNames = mutableSetOf<String>()
        for (packageName in queryPackageNames(
            packageManager,
            Intent(Intent.ACTION_VIEW, "magicearth:".toUri()),
        )) {
            if (packageName !in seenPackageNames) {
                seenPackageNames.add(packageName)
                add(App(packageName, AppType.MAGIC_EARTH))
            }
        }
        for (packageName in queryPackageNames(
            packageManager,
            Intent(Intent.ACTION_VIEW, "geo:".toUri()),
        )) {
            if (packageName !in seenPackageNames) {
                seenPackageNames.add(packageName)
                add(App(packageName, AppType.GEO_URI))
            }
        }
        for (packageName in queryPackageNames(
            packageManager,
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType("content:".toUri(), "application/gpx+xml")
            },
        )) {
            if (packageName !in seenPackageNames) {
                seenPackageNames.add(packageName)
                add(App(packageName, AppType.GPX))
            }
        }
    }

    private fun startActivity(context: Context, intent: Intent): Boolean =
        try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }

    private fun createChooser(intent: Intent): Intent =
        Intent.createChooser(
            intent,
            "Choose an app",
        ).apply {
            putExtra(
                Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(
                    @Suppress("SpellCheckingInspection")
                    (ComponentName(PackageNames.GEO_SHARE, "page.ooooo.geoshare.ConversionActivity")),
                    @Suppress("SpellCheckingInspection")
                    (ComponentName(PackageNames.GEO_SHARE_DEBUG, "page.ooooo.geoshare.ConversionActivity")),
                )
            )
        }

    fun openApp(context: Context, packageName: String, uriString: String): Boolean =
        startActivity(
            context,
            Intent(Intent.ACTION_VIEW, uriString.toUri()).apply {
                setPackage(packageName)
            },
        )

    fun openAppFile(context: Context, packageName: String, file: File): Boolean {
        val uri = try {
            FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.FileProvider", file)
        } catch (e: IllegalArgumentException) {
            Log.e(null, "Error when getting URI for file", e)
            return false
        }
        return startActivity(
            context,
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, context.contentResolver.getType(uri))
                setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage(packageName)
            },
        )
    }

    fun openChooser(context: Context, uriString: String): Boolean =
        startActivity(
            context,
            createChooser(
                Intent(Intent.ACTION_VIEW, uriString.toUri()),
            ),
        )

    fun openChooserFile(context: Context, file: File): Boolean {
        val uri = try {
            FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.FileProvider", file)
        } catch (e: IllegalArgumentException) {
            Log.e(null, "Error when getting URI for file", e)
            return false
        }
        return startActivity(
            context,
            createChooser(
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, context.contentResolver.getType(uri))
                    setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
            ),
        )
    }

    fun hasLocationPermission(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun getCurrentLocation(
        locationManager: LocationManager,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): Point? = withContext(dispatcher) {
        suspendCancellableCoroutine { cont ->
            val cancellationSignal = CancellationSignal()
            try {
                locationManager.getCurrentLocation(
                    LocationManager.GPS_PROVIDER,
                    cancellationSignal,
                    dispatcher.asExecutor(),
                ) { location: Location? ->
                    cont.resume(location?.let { WGS84Point(it.latitude, it.longitude) })
                }
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
            cont.invokeOnCancellation {
                cancellationSignal.cancel()
            }
        }
    }

    /**
     * See https://stackoverflow.com/a/71710276
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun getCurrentLocationPreS(
        locationManager: LocationManager,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): Point? = withContext(dispatcher) {
        withTimeoutOrNull(30.seconds) {
            suspendCancellableCoroutine { cont ->
                try {
                    @Suppress("DEPRECATION")
                    locationManager.requestSingleUpdate(
                        LocationManager.GPS_PROVIDER,
                        object : LocationListenerCompat {
                            // Use LocationListenerCompat instead of LocationListener or lambda, so that we don't have
                            // to override onStatusChanged on Android Q and older.
                            override fun onLocationChanged(location: Location) {
                                cont.resume(location.let {
                                    WGS84Point(it.latitude, it.longitude)
                                })
                            }
                        },
                        Looper.getMainLooper(),
                    )
                } catch (e: Exception) {
                    Log.e(null, "Error when getting location", e)
                    cont.resumeWithException(e)
                }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLastKnownLocation(locationManager: LocationManager, maxAge: Duration = 1.minutes): Point? =
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?.takeIf { (SystemClock.elapsedRealtimeNanos() - it.elapsedRealtimeNanos).nanoseconds <= maxAge }
            ?.let { WGS84Point(it.latitude, it.longitude) }

    suspend fun getLocation(context: Context): Point? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            val lastKnownLocation = getLastKnownLocation(locationManager)
            if (lastKnownLocation != null) {
                lastKnownLocation
            } else {
                // Use a small delay to prevent Android from asking for location permission twice, once for
                // getLastKnownLocation() and once for getCurrentLocation()
                delay(500.milliseconds)
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
                    getCurrentLocation(locationManager)
                } else {
                    getCurrentLocationPreS(locationManager)
                }
            }
        } catch (e: SecurityException) {
            Log.e(null, "Security error when getting location", e)
            null
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

    fun showOpenByDefaultSettings(context: Context, launcher: ActivityResultLauncher<Intent>) {
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

    suspend fun copyToClipboard(clipboard: Clipboard, text: String) =
        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Geographic coordinates", text)))

    suspend fun pasteFromClipboard(clipboard: Clipboard): String =
        clipboard.getClipEntry()?.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString() ?: ""

    /**
     * See [GitHub Gist](https://gist.github.com/starry-shivam/901267c26eb030eb3faf1ccd4d2bdd32)
     */
    fun isMiuiDevice(): Boolean =
        setOf("xiaomi", "redmi", "poco").contains(Build.BRAND.lowercase()) &&
            (!getRuntimeProperty("ro.miui.ui.version.name").isNullOrBlank() ||
                !getRuntimeProperty("ro.mi.os.version.name").isNullOrBlank())

    private fun getRuntimeProperty(property: String): String? = try {
        @Suppress("SpellCheckingInspection")
        Runtime.getRuntime().exec("getprop $property").inputStream.use { input ->
            BufferedReader(InputStreamReader(input), 1024).readLine()
        }
    } catch (_: IOException) {
        null
    }
}
