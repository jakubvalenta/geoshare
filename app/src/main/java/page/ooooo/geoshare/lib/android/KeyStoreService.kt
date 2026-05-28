package page.ooooo.geoshare.lib.android

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.Log
import java.security.GeneralSecurityException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.ECGenParameterSpec
import javax.inject.Inject

class KeyStoreService @Inject constructor(
    private val log: Log = FakeLog,
) {

    /**
     * Try to get a key from the key store.
     */
    fun getKey(): KeyStore.PrivateKeyEntry? {
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
        return entry
    }

    /**
     * Generate new key and save it in the key store.
     *
     * If there was a corrupt key in the key store, overwrite it.
     */
    fun generateKey(challenge: ByteArray): KeyStore.PrivateKeyEntry {
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
            setAttestationChallenge(challenge)
            // Don't use StrongBox, because it's not available on all devices, and we probably don't need that level of
            // security
            build()
        }
        keyPairGenerator.initialize(params)
        keyPairGenerator.generateKeyPair()

        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.PrivateKeyEntry
            ?: throw IllegalStateException("Key generation succeeded but key store entry not found")
    }

    private companion object {
        private const val KEYSTORE_ALIAS = "geoshare_api"
        private const val TAG = "KeyStoreService"
    }
}
