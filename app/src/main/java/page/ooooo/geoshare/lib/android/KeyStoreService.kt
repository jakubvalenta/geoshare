package page.ooooo.geoshare.lib.android

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.Log
import java.security.GeneralSecurityException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.Certificate
import java.security.spec.ECGenParameterSpec
import javax.inject.Inject

data class Key(
    val privateKey: PrivateKey,
    val publicKey: PublicKey,
    val certificateChain: List<Certificate>,
)

interface KeyStoreService {
    fun getKey(): Key?
    fun generateKey(): Key
}

class DefaultKeyStoreService @Inject constructor(
    private val log: Log = FakeLog,
) : KeyStoreService {
    /**
     * Try to get a key from the key store.
     */
    override fun getKey(): Key? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val entry = try {
            keyStore.getEntry(KEYSTORE_ALIAS, null)
        } catch (tr: GeneralSecurityException) {
            log.e(TAG, "Error when getting key from the key store", tr)
            return null
        }
        if (entry !is KeyStore.PrivateKeyEntry) {
            log.e(TAG, "Got key from the key store but it's not a private key")
            return null
        }
        return Key(
            entry.privateKey,
            entry.certificateChain.first().publicKey,
            entry.certificateChain.toList(),
        )
    }

    /**
     * Generate new key and save it in the key store.
     *
     * If there was a corrupt key in the key store, overwrite it.
     */
    override fun generateKey(): Key {
        val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
        val params = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).run {
            setAlgorithmParameterSpec(
                ECGenParameterSpec(@Suppress("SpellCheckingInspection") "secp256r1")
            )
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            // Setting attestation challenge triggers the signing of the certificate by the attestation certificate
            setAttestationChallenge(byteArrayOf())
            // Don't use StrongBox, because it's not available on all devices, and we probably don't need that level of
            // security
            build()
        }
        keyPairGenerator.initialize(params)
        keyPairGenerator.generateKeyPair()

        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val entry = keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.PrivateKeyEntry
            ?: throw IllegalStateException("Key generation succeeded but key store entry not found")
        return Key(
            entry.privateKey,
            entry.certificateChain.first().publicKey,
            entry.certificateChain.toList(),
        )
    }

    private companion object {
        private const val KEYSTORE_ALIAS = "geoshare_api"
        private const val TAG = "KeyStoreService"
    }
}
