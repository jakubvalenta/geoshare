package page.ooooo.geoshare.lib.android

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.Log
import java.io.File
import java.io.FileNotFoundException
import java.util.Locale

private const val TAG = "ContextExtensions"

private fun Context.startActivityOrFalse(intent: Intent): Boolean =
    try {
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }

fun Context.openUriInApp(uriString: String, packageName: String): Boolean =
    startActivityOrFalse(
        Intent(Intent.ACTION_VIEW, uriString.toUri()).apply {
            setPackage(packageName)
        },
    )

fun Context.openUriInDefaultApp(uriString: String): Boolean =
    startActivityOrFalse(Intent(Intent.ACTION_VIEW, uriString.toUri()))

fun Context.openFileInApp(file: File, packageName: String, log: Log = DefaultLog): Boolean {
    val uri = try {
        FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.FileProvider", file)
    } catch (e: IllegalArgumentException) {
        log.e(TAG, "Error when getting URI for file", e)
        return false
    }
    return startActivityOrFalse(
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, contentResolver.getType(uri))
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            setPackage(packageName)
        },
    )
}

private fun Intent.createChooser(): Intent =
    Intent.createChooser(
        this,
        "Choose an app",
    ).apply {
        putExtra(
            Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(
                (ComponentName(BuildConfig.APPLICATION_ID, "page.ooooo.geoshare.ConversionActivity")),
                (ComponentName(BuildConfig.APPLICATION_ID + ".debug", "page.ooooo.geoshare.ConversionActivity")),
            )
        )
    }

fun Context.openUriWithChooser(uriString: String): Boolean =
    startActivityOrFalse(
        Intent(Intent.ACTION_VIEW, uriString.toUri()).createChooser()
    )

fun Context.openFileWithChooser(file: File, log: Log = DefaultLog): Boolean {
    val uri = try {
        FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.FileProvider", file)
    } catch (e: IllegalArgumentException) {
        log.e(TAG, "Error when getting URI for file", e)
        return false
    }
    return startActivityOrFalse(
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, contentResolver.getType(uri))
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }.createChooser()
    )
}

fun Context.openSettingsOpenByDefaultForPackage(launcher: ActivityResultLauncher<Intent>, packageName: String) {
    try {
        val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            // Samsung supposedly doesn't allow going to the "Open by default" settings page.
            Build.MANUFACTURER.lowercase(Locale.ROOT) !=
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "samsung"
        ) {
            Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
        } else {
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        }
        val intent = Intent(action, "package:$packageName".toUri())
        launcher.launch(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, R.string.intro_settings_activity_not_found, Toast.LENGTH_LONG).show()
    }
}

fun Context.openSettingsOpenByDefault(launcher: ActivityResultLauncher<Intent>) {
    openSettingsOpenByDefaultForPackage(launcher, BuildConfig.APPLICATION_ID)
}

fun Context.sendTextViaApp(text: String, packageName: String): Boolean =
    startActivityOrFalse(
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage(packageName)
            putExtra(Intent.EXTRA_TEXT, text)
        }
    )

fun Context.insertOrEditContactAddress(address: String): Boolean =
    startActivityOrFalse(
        Intent(Intent.ACTION_INSERT_OR_EDIT).apply {
            type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
            putExtra(ContactsContract.Intents.Insert.POSTAL, address)
            putExtra(
                ContactsContract.Intents.Insert.POSTAL_TYPE,
                ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
            )
        }
    )

fun String.toEmailIntent(): Intent =
    Intent(Intent.ACTION_SENDTO, "mailto:$this".toUri())

fun Context.composeEmail(address: String): Boolean =
    startActivityOrFalse(address.toEmailIntent())

fun Context.openFileUriForWriting(uri: Uri, log: Log = DefaultLog, block: Appendable.() -> Unit): Boolean {
    val outputStream = try {
        contentResolver.openOutputStream(uri)
    } catch (_: FileNotFoundException) {
        log.e(TAG, "Output stream URI $uri could not be opened")
        return false
    }
    if (outputStream == null) {
        log.e(TAG, "Output stream URI $uri recently crashed")
        return false
    }
    outputStream.use { outputStream ->
        outputStream.writer().use { writer ->
            writer.block()
        }
    }
    return true
}
