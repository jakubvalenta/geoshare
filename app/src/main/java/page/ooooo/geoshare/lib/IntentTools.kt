package page.ooooo.geoshare.lib

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import page.ooooo.geoshare.CopyActivity
import page.ooooo.geoshare.SkipActivity

class IntentTools {

    private val intentUrlRegex = Regex("https?://\\S+")

    fun share(context: Context, action: String, uriString: String, originalData: Uri?) {
        context.startActivity(
            Intent.createChooser(
                Intent(action, uriString.toUri()),
                "Choose an app",
            ).apply {
                putExtra(
                    Intent.EXTRA_INITIAL_INTENTS, arrayOf(
                        Intent(context, CopyActivity::class.java).apply {
                            data = uriString.toUri()
                        },
                        Intent(context, SkipActivity::class.java).apply {
                            data = originalData
                        },
                    )
                )
                putExtra(
                    Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(
                        ComponentName("page.ooooo.geoshare", "ShareActivity"),
                    )
                )
            })
    }

    fun view(context: Context, data: Uri?) {
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_VIEW, data),
                "Choose an app",
            ).apply {
                putExtra(
                    Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(
                        ComponentName("page.ooooo.geoshare", "ShareActivity"),
                    )
                )
            })
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
