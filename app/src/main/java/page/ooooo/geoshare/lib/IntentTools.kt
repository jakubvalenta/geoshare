package page.ooooo.geoshare.lib

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.util.Log

class IntentTools {

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
}
