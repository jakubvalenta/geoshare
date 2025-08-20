package page.ooooo.geoshare.lib

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.util.Log

class IntentTools {

    private val intentUrlRegex = Regex("(https?://|geo:)\\S+")

    fun createViewIntent(packageName: String, data: Uri): Intent = Intent(Intent.ACTION_VIEW, data).apply {
        setPackage(packageName)
    }

    fun createChooserIntent(data: Uri): Intent = Intent.createChooser(
        Intent(Intent.ACTION_VIEW, data),
        "Choose an app",
    ).apply {
        putExtra(
            Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(
                ComponentName("page.ooooo.geoshare", "ConversionActivity"),
            )
        )
    }

    fun getIntentPosition(intent: Intent): Position? =
        intent.data.takeIf { it != null && intent.action == Intent.ACTION_VIEW && intent.scheme == "geo" }?.let {
            Position.fromGeoUriString(it.toString())
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
                val intentUrlMatch = intentUrlRegex.find(intentText)
                if (intentUrlMatch == null) {
                    Log.w(null, "Intent extra text does not contain a URL")
                    return null
                }
                return intentUrlMatch.value
            }

            else -> {
                Log.w(null, "Unsupported intent action $intentAction")
                return null
            }
        }
    }
}
