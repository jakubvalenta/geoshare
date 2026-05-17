package page.ooooo.geoshare.lib.extensions

import java.security.MessageDigest
import java.util.Base64

fun ByteArray.sha256Hex(): String =
    MessageDigest.getInstance("SHA-256")
        .digest(this)
        .joinToString("") { "%02x".format(it) }

fun ByteArray.base64Encode(): String =
    Base64.getEncoder().encodeToString(this)

fun String.sha256Hex(): String =
    this.toByteArray().sha256Hex()

fun String.base64Decode(): ByteArray =
    Base64.getDecoder().decode(this)
