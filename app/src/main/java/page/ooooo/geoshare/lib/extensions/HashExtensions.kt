package page.ooooo.geoshare.lib.extensions

import android.os.Build

fun ByteArray.base64Encode(): String =
    if (Build.VERSION.SDK_INT in 1..Build.VERSION_CODES.O) {
        android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)
    } else {
        java.util.Base64.getEncoder().encodeToString(this)
    }

fun String.base64Decode(): ByteArray =
    if (Build.VERSION.SDK_INT in 1..Build.VERSION_CODES.O) {
        android.util.Base64.decode(this, android.util.Base64.NO_WRAP)
    } else {
        java.util.Base64.getDecoder().decode(this)
    }
