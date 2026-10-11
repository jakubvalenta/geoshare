package page.ooooo.geoshare.data.network

import android.os.Build
import android.util.Base64
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

const val SERVICE_NAME = "geoshare-server"

enum class SigningPurpose(val value: String) {
    LOGIN("login"),
    REGISTRATION("registration"),
}

fun PublicKey.fingerprint(): String =
    MessageDigest.getInstance("SHA-256")
        .digest(encoded)
        .base64Encode()

fun PrivateKey.sign(data: ByteArray): ByteArray =
    Signature.getInstance(@Suppress("SpellCheckingInspection", "GrazieInspectionRunner") "SHA256withECDSA").run {
        initSign(this@sign)
        update(data)
        sign()
    }

fun PublicKey.verifySignature(signature: ByteArray, data: ByteArray): Boolean =
    Signature.getInstance(@Suppress("SpellCheckingInspection", "GrazieInspectionRunner") "SHA256withECDSA").run {
        initVerify(this@verifySignature)
        update(data)
        verify(signature)
    }

fun buildSigningPayloadV1(purpose: SigningPurpose, publicKeyFingerprint: String, challenge: ByteArray): String {
    val separator = ':'
    val fields = listOf(SERVICE_NAME, "v1", purpose.value, publicKeyFingerprint, challenge.base64Encode())
    require(fields.none { separator in it }) { "Payload fields must not contain '$separator'" }
    return fields.joinToString(separator.toString())
}

fun ByteArray.base64Encode(): String =
    if (Build.VERSION.SDK_INT in 1..Build.VERSION_CODES.O) {
        Base64.encodeToString(this, Base64.NO_WRAP)
    } else {
        java.util.Base64.getEncoder().encodeToString(this)
    }

fun String.base64Decode(): ByteArray =
    if (Build.VERSION.SDK_INT in 1..Build.VERSION_CODES.O) {
        Base64.decode(this, Base64.NO_WRAP)
    } else {
        java.util.Base64.getDecoder().decode(this)
    }
