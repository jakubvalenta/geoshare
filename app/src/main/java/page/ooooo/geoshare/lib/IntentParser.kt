package page.ooooo.geoshare.lib

import android.content.Intent
import android.util.Log
import java.net.MalformedURLException
import java.net.URL

class IntentParser {

    private val intentUrlRegex = Regex("https?://\\S+")

    fun getIntentGeoUri(intent: Intent): String? {
        return if (intent.action == Intent.ACTION_VIEW && intent.data != null && intent.scheme == "geo") {
            intent.data?.toString()
        } else {
            null
        }
    }

    fun getIntentUrl(intent: Intent): URL? {
        val intentAction = intent.action
        val urlString = if (intentAction == Intent.ACTION_VIEW) {
            val intentData: String? = intent.data?.toString()
            if (intentData == null) {
                Log.w(null, "Missing intent data")
                return null
            }
            intentData.toString()
        } else if (intentAction == Intent.ACTION_SEND) {
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
            intentUrlMatch.value
        } else {
            Log.w(null, "Unsupported intent action $intentAction")
            return null
        }
        return try {
            URL(urlString)
        } catch (_: MalformedURLException) {
            Log.w(null, "Invalid URL $urlString")
            return null
        }
    }
}
