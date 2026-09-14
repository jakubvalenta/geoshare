package page.ooooo.geoshare.lib.android

import android.content.Intent
import android.util.Log

private const val TAG = "IntentExtensions"

fun Intent.getUriString(): String? =
    when (action) {
        Intent.ACTION_VIEW -> data?.toString()
            .also { if (it == null) Log.w(TAG, "Missing intent data") }

        Intent.ACTION_SEND -> getStringExtra(Intent.EXTRA_TEXT)
            .also { if (it == null) Log.w(TAG, "Missing intent extra text") }

        else -> null
            .also { Log.w(TAG, "Unsupported intent action $action") }
    }
