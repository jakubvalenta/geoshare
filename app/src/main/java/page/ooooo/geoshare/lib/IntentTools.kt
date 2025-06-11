package page.ooooo.geoshare.lib

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.chooser.ChooserAction
import android.util.Log
import androidx.core.net.toUri
import page.ooooo.geoshare.R

class IntentTools {

    private val extraProcessed = "page.ooooo.geoshare.EXTRA_PROCESSED"

    private val intentUrlRegex = Regex("https?://\\S+")

    fun isProcessed(intent: Intent): Boolean =
        intent.getStringExtra(extraProcessed) != null

    fun share(context: Context, action: String, uriString: String) {
        context.startActivity(
            Intent.createChooser(
                Intent(action).apply {
                    data = uriString.toUri()
                    putExtra(extraProcessed, "true")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        putExtra(
                            Intent.EXTRA_CHOOSER_CUSTOM_ACTIONS, arrayOf(
                                ChooserAction.Builder(
                                    Icon.createWithResource(context, R.drawable.content_copy_24px),
                                    "Copy to clipboard",
                                    PendingIntent.getBroadcast(
                                        context,
                                        1,
                                        Intent(Intent.ACTION_VIEW),
                                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                                    )
                                ).build(),
                                ChooserAction.Builder(
                                    Icon.createWithResource(context, R.drawable.close_24px),
                                    "Do nothing",
                                    PendingIntent.getBroadcast(
                                        context,
                                        1,
                                        Intent(Intent.ACTION_VIEW),
                                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                                    )
                                ).build(),
                            )
                        )
                    }
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
}
