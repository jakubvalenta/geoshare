package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import page.ooooo.geoshare.lib.android.DefaultKeyStoreService
import page.ooooo.geoshare.lib.android.Key
import page.ooooo.geoshare.lib.android.KeyStoreService
import java.security.KeyPairGenerator
import java.security.cert.CertificateFactory
import java.security.spec.ECGenParameterSpec
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KeyStoreServiceModule {

    @Singleton
    @Provides
    fun provideKeyStoreService(): KeyStoreService =
        DefaultKeyStoreService()
}

class FakeKeyStoreService(
    private var key: Key? = null,
) : KeyStoreService {
    override fun getKey() = key

    /**
     * Generate a key and certificate chain. The certificate chain is hard-coded and doesn't actually sign the new key.
     */
    override fun generateKey(challenge: ByteArray): Key {
        val keyPair = KeyPairGenerator.getInstance("EC").run {
            initialize(ECGenParameterSpec(@Suppress("SpellCheckingInspection") "secp256r1"))
            generateKeyPair()
        }
        val cert = CertificateFactory.getInstance("X.509").run {
            generateCertificate(CERT_PEM.byteInputStream())
        }
        return Key(keyPair.private, listOf(cert))
            .also { key = it }
    }

    private companion object {
        const val CERT_PEM = @Suppress("SpellCheckingInspection") """
-----BEGIN CERTIFICATE-----
MIIBrzCCATagAwIBAgIUZPRFIKvljY6kBKRSwGF1Zr9eWnMwCgYIKoZIzj0EAwIw
DzENMAsGA1UEAwwEdGVzdDAeFw0yNjA1MjgwOTA0MjFaFw0zNjA1MjUwOTA0MjFa
MA8xDTALBgNVBAMMBHRlc3QwdjAQBgcqhkjOPQIBBgUrgQQAIgNiAARbw3KvjpdA
8pv0CKrwT6l6ECaG6tstwFVEahTTTE7W1TvNhWqR9YNzLgNM4hR+R4rBmj9nW3gh
mRDe5hf+OtZ+8GjdNuctnzdkUoxN9RR72g0Gd+uN9Ut5vgV6PqrXwC+jUzBRMB0G
A1UdDgQWBBST1jmvIjFH8isXmEJoWynU3MtYUzAfBgNVHSMEGDAWgBST1jmvIjFH
8isXmEJoWynU3MtYUzAPBgNVHRMBAf8EBTADAQH/MAoGCCqGSM49BAMCA2cAMGQC
MFDPIWMKwJzLvbrM0LZchhxd34xppSWyL6GSdwzgMj2O3yp5ktSO+d0j/sPYUYoC
nAIwD5UqKD0BVddVyyU0WGhWzCK6BSH+KG5C6Ny/EKyjaV2+jhy8onIvVJOb4A8J
3YeH
-----END CERTIFICATE-----
"""
    }
}
