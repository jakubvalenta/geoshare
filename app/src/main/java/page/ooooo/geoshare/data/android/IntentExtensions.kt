package page.ooooo.geoshare.data.android

import android.content.Intent
import page.ooooo.geoshare.data.log.DefaultLog
import page.ooooo.geoshare.data.log.Log

private const val TAG = "IntentExtensions"

fun Intent.getUriString(log: Log = DefaultLog): String? =
    when (action) {
        Intent.ACTION_VIEW -> data?.toString() ?: run {
            log.w(TAG, "Missing intent data")
            null
        }

        Intent.ACTION_SEND -> getStringExtra(Intent.EXTRA_TEXT) ?: run {
            log.w(TAG, "Missing intent EXTRA_TEXT")
            null
        }

        Intent.ACTION_PROCESS_TEXT -> getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: run {
            log.w(TAG, "Missing intent EXTRA_PROCESS_TEXT")
            null
        }

        else -> {
            log.w(TAG, "Unsupported intent action $action")
            null
        }
    }
