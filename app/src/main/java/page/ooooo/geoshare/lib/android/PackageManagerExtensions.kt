package page.ooooo.geoshare.lib.android

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import page.ooooo.geoshare.BuildConfig

private const val TAG = "PackageManagerExtensions"

fun PackageManager.queryApps(
    allowedMessagingApps: Set<String> = setOf(
        PackageNames.CONVERSATIONS,
        PackageNames.SIGNAL,
        PackageNames.TELEGRAM,
        PackageNames.TELEGRAM_FORK,
        PackageNames.WHATSAPP,
    ),
): Apps =
    buildMap {
        for (packageName in queryPackageNames(Intent(Intent.ACTION_VIEW, "geo:".toUri()))) {
            if (packageName != PackageNames.CARTES_IGN) {
                getOrPut(packageName) { mutableSetOf() }.add(DataType.GEO_URI)
            } else {
                // Replace geo: URIs with HTTPs URLs for Cartes IGN, because it doesn't support geo: URIs well
                getOrPut(packageName) { mutableSetOf() }.add(DataType.CARTES_IGN_URL)
            }
        }
        for (packageName in queryPackageNames(Intent(Intent.ACTION_VIEW, "google.navigation:".toUri()))) {
            getOrPut(packageName) { mutableSetOf() }.add(DataType.GOOGLE_NAVIGATION_URI)
        }
        for (packageName in queryPackageNames(Intent(Intent.ACTION_VIEW, "google.streetview:".toUri()))) {
            getOrPut(packageName) { mutableSetOf() }.add(DataType.GOOGLE_STREET_VIEW_URI)
        }
        for (packageName in queryPackageNames(Intent(Intent.ACTION_VIEW, "magicearth:".toUri()))) {
            getOrPut(packageName) { mutableSetOf() }.apply {
                add(DataType.MAGIC_EARTH_URI)
                // Remove support for geo: and google.navigation: URIs from the Magic Earth app, because it doesn't
                // support these URIs well
                remove(DataType.GEO_URI)
                remove(DataType.GOOGLE_NAVIGATION_URI)
            }
        }
        for (packageName in queryPackageNames(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType("content:".toUri(), "application/gpx+xml")
            },
        )) {
            getOrPut(packageName) { mutableSetOf() }.add(
                if (packageName.startsWith(PackageNames.TOMTOM_PREFIX)) {
                    DataType.GPX_ONE_POINT_DATA
                } else {
                    DataType.GPX_DATA
                }
            )
        }
        for (packageName in queryPackageNames(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
            },
        )) {
            // Allow only selected messaging apps, so that the app list is not flooded with apps no one will use
            if (packageName in allowedMessagingApps) {
                getOrPut(packageName) { mutableSetOf() }.add(DataType.SEND_PLAIN_TEXT)
            }
        }
    }
        .mapValues { (packageName, dataTypes) -> App(packageName = packageName, dataTypes = dataTypes) }

fun PackageManager.queryActivities(
    allowedMessagingApps: Set<String> = setOf(
        PackageNames.CONVERSATIONS,
        PackageNames.SIGNAL,
        PackageNames.TELEGRAM,
        PackageNames.TELEGRAM_FORK,
        PackageNames.WHATSAPP,
    ),
): List<AppActivity> = buildList {
    for (packageName in queryPackageNames(Intent(Intent.ACTION_VIEW, "geo:".toUri()))) {
        when (packageName) {
            // Replace geo: URIs with HTTPs URLs for Cartes IGN, because it doesn't support geo: URIs well
            PackageNames.CARTES_IGN -> add(UriActivity(packageName, UriScheme.CARTES_IGN))
            // Exclude geo: URIs from Magic Earth, because it doesn't support these URIs well
            PackageNames.MAGIC_EARTH -> {}
            else -> add(UriActivity(packageName, UriScheme.GEO))
        }
    }
    for (packageName in queryPackageNames(Intent(Intent.ACTION_VIEW, "google.navigation:".toUri()))) {
        when (packageName) {
            // Exclude google.navigation: URIs from Magic Earth, because it doesn't support these URIs well
            PackageNames.MAGIC_EARTH -> {}
            else -> add(UriActivity(packageName, UriScheme.GOOGLE_NAVIGATION))
        }
    }
    for (packageName in queryPackageNames(Intent(Intent.ACTION_VIEW, "google.streetview:".toUri()))) {
        add(UriActivity(packageName, UriScheme.GOOGLE_STREET_VIEW))
    }
    for (packageName in queryPackageNames(Intent(Intent.ACTION_VIEW, "magicearth:".toUri()))) {
        add(UriActivity(packageName, UriScheme.MAGIC_EARTH))
    }
    for (packageName in queryPackageNames(
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType("content:".toUri(), "application/gpx+xml")
        },
    )) {
        if (packageName.startsWith(PackageNames.TOMTOM_PREFIX)) {
            add(FileActivity(packageName, FileType.GPX_ONE_POINT))
        } else {
            add(FileActivity(packageName, FileType.GPX))
        }
    }
    for (packageName in queryPackageNames(
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
        },
    )) {
        // Allow only selected messaging apps, so that the app list is not flooded with apps no one will use
        if (packageName in allowedMessagingApps) {
            add(TextActivity(packageName, mimeType = "text/plain"))
        }
    }
}

private fun PackageManager.queryAppDetails(packageName: String): AppDetail? {
    val applicationInfo = try {
        getApplicationInfo(packageName, 0)
    } catch (e: Exception) {
        Log.e(TAG, "Error when querying an app", e)
        return null
    }
    return try {
        AppDetail(
            packageName = packageName,
            label = applicationInfo.loadLabel(this).toString(),
            icon = applicationInfo.loadIcon(this),
        )
    } catch (e: Exception) {
        Log.e(TAG, "Error when loading info about an app", e)
        null
    }
}

/**
 * Query package manager for labels and icons of [packageNames].
 *
 * It is executed on a non-main thread, because it takes about 50ms and maybe that's too much.
 */
suspend fun PackageManager.queryAppDetails(packageNames: Iterable<String>): AppDetails =
    withContext(Dispatchers.Default) {
        packageNames.associateWith { packageName -> queryAppDetails(packageName) }
    }

/**
 * Query package manager for package names of apps that can open [uriString].
 */
fun PackageManager.queryAppsForUri(uriString: String): Apps =
    uriString.toUri().let { uri ->
        // Use flag MATCH_ALL, so that all apps are returned, even if GeoShare is set to open the URI by default
        if (uri.scheme != null) {
            queryPackageNames(
                Intent(Intent.ACTION_VIEW, uri),
                flags = PackageManager.MATCH_ALL,
            )
                .associateWith { packageName -> App(packageName, setOf(DataType.VIEW_URI)) }
        } else {
            queryPackageNames(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                },
                flags = PackageManager.MATCH_ALL,
            )
                .associateWith { packageName -> App(packageName, setOf(DataType.SEND_PLAIN_TEXT)) }
        }
    }

private fun PackageManager.queryPackageNames(
    intent: Intent,
    flags: Int = PackageManager.MATCH_DEFAULT_ONLY,
): Set<String> {
    val resolveInfos = try {
        queryIntentActivities(intent, flags)
    } catch (e: Exception) {
        Log.e(TAG, "Error when querying installed apps", e)
        return emptySet()
    }
    return resolveInfos
        .mapNotNull { resolveInfo ->
            val packageName = try {
                resolveInfo.activityInfo.packageName
            } catch (e: Exception) {
                Log.e(TAG, "Error when loading info about an installed app", e)
                null
            }
            // Exclude GeoShare itself and all its build flavors
            packageName?.takeUnless { it == PackageNames.GEOSHARE || it.startsWith(PackageNames.GEOSHARE_PREFIX) }
        }
        .toSet()
}

fun PackageManager.isDefaultHandlerEnabled(uriString: String): Boolean {
    val resolveInfo = try {
        resolveActivity(
            Intent(Intent.ACTION_VIEW, uriString.toUri()),
            PackageManager.MATCH_DEFAULT_ONLY,
        )
    } catch (e: Exception) {
        Log.e(TAG, "Error when querying which app is the default handler for a URI", e)
        return false
    }
    val packageName = try {
        resolveInfo?.activityInfo?.packageName
    } catch (e: Exception) {
        Log.e(TAG, "Error when loading info about an app that is the default handler for URI", e)
        null
    }
    return packageName == BuildConfig.APPLICATION_ID
}

fun PackageManager.hasEmailApp(): Boolean =
    "".toEmailIntent().resolveActivity(this) != null
