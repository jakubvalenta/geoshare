package page.ooooo.geoshare.lib.android

import android.content.Intent
import android.util.Log

private const val TAG = "IntentExtensions"

fun Intent.getUriString(): String? =
    when (val intentAction = action) {
        Intent.ACTION_VIEW -> {
            val intentData: String? = data?.toString()
            if (intentData == null) {
                Log.w(TAG, "Missing intent data")
                null
            } else {
                intentData
            }
        }

        Intent.ACTION_SEND -> {
            val intentText = getStringExtra("android.intent.extra.TEXT")
            if (intentText == null) {
                Log.w(TAG, "Missing intent extra text")
                null
            } else {
                intentText
            }
        }

        else -> {
            Log.w(TAG, "Unsupported intent action $intentAction")
            null
        }
    }
