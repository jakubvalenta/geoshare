package page.ooooo.geoshare.lib

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri

class IntentTools {

    private val extraProcessed = "page.ooooo.geoshare.EXTRA_PROCESSED"
    private val extraOriginalUri = "page.ooooo.geoshare.EXTRA_ORIGINAL_URI"

    private val intentUrlRegex = Regex("https?://\\S+")

    fun isProcessed(intent: Intent): Boolean =
        intent.getStringExtra(extraProcessed) != null

    fun share(context: Context, action: String, uriString: String, originalUri: Uri) {
        context.startActivity(
            Intent.createChooser(
                Intent(action).apply {
                    data = uriString.toUri()
                    putExtra(extraProcessed, "true")
                    putExtra(extraOriginalUri, originalUri)
                },
                "Choose an app",
            )
        )
    }

    fun getIntentGeoUri(intent: Intent): String? {
        return if (intent.action == Intent.ACTION_VIEW && intent.data != null && intent.scheme == "geo") {
            intent.data?.toString()
        } else {
            null
        }
    }

    fun getIntentUrlString(intent: Intent): String? {
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
                val intentText =
                    intent.getStringExtra("android.intent.extra.TEXT")
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

    fun getIntentOriginalUri(intent: Intent): Uri? = intent.getStringExtra(extraOriginalUri)?.toUri()
}
