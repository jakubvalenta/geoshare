package page.ooooo.geoshare.lib.extensions

import android.util.Base64

fun ByteArray.base64Encode(): String =
    // Use android.util.Base64, because java.util.Base64 is not available on API < 26
    Base64.encodeToString(this, Base64.NO_WRAP)

fun String.base64Decode(): ByteArray =
    // Use android.util.Base64, because java.util.Base64 is not available on API < 26
    Base64.decode(this, Base64.NO_WRAP)
