package page.ooooo.geoshare.lib.extensions

import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

fun PrivateKey.sign(data: ByteArray): ByteArray =
    Signature.getInstance("SHA256withECDSA").run {
        initSign(this@sign)
        update(data)
        sign()
    }

fun PublicKey.verifySignature(signature: ByteArray, data: ByteArray): Boolean =
    Signature.getInstance("SHA256withECDSA").run {
        initVerify(this@verifySignature)
        update(data)
        verify(signature)
    }
