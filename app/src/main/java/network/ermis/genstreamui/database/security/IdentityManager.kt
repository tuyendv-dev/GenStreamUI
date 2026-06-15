package network.ermis.genstreamui.database.security

import android.content.Context
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.util.io.pem.PemObject
import java.io.File
import java.io.StringWriter
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Quản lý **danh tính thiết bị** cho GenStream: một cặp client cert + private key (RSA 2048, tự ký)
 * sinh **một lần** và lưu bền trong filesDir, tái dùng vĩnh viễn (genstream-custom-auth.md §2, §8).
 *
 * Dùng cho:
 * - token-auth: gửi [certificatePem] trong body để host authorize (thay PIN pairing).
 * - mTLS (giai đoạn sau): trình cert + [privateKey] khi gọi serverinfo/launch/resume.
 *
 * Idempotent: token-auth lại bằng **cùng cert** → host trả "already authorized" (tiện reconnect).
 */
@Singleton
class IdentityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class Identity(
        val certificate: X509Certificate,
        val privateKey: PrivateKey,
        val certificatePem: String
    )

    private val certFile: File by lazy { File(context.filesDir, CERT_FILE) }
    private val keyFile: File by lazy { File(context.filesDir, KEY_FILE) }
    private val lock = Any()

    @Volatile
    private var cached: Identity? = null

    /** Lấy (hoặc sinh lần đầu) danh tính thiết bị. An toàn đa luồng. */
    fun identity(): Identity {
        cached?.let { return it }
        return synchronized(lock) {
            cached ?: (if (certFile.exists() && keyFile.exists()) load() else generateAndStore())
                .also { cached = it }
        }
    }

    val certificatePem: String get() = identity().certificatePem
    val privateKey: PrivateKey get() = identity().privateKey
    val certificate: X509Certificate get() = identity().certificate

    private fun load(): Identity {
        val certPem = certFile.readText()
        val cert = CertificateFactory.getInstance("X.509")
            .generateCertificate(certPem.byteInputStream()) as X509Certificate
        val keyDer = decodePemBody(keyFile.readText())
        val key = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(keyDer))
        return Identity(cert, key, certPem)
    }

    private fun generateAndStore(): Identity {
        ensureProvider()
        val keyPair = KeyPairGenerator.getInstance("RSA")
            .apply { initialize(KEY_SIZE, SecureRandom()) }
            .generateKeyPair()
        val cert = selfSignedCert(keyPair)
        val certPem = toPem("CERTIFICATE", cert.encoded)
        certFile.writeText(certPem)
        keyFile.writeText(toPem("PRIVATE KEY", keyPair.private.encoded))
        return Identity(cert, keyPair.private, certPem)
    }

    private fun selfSignedCert(keyPair: KeyPair): X509Certificate {
        val now = System.currentTimeMillis()
        val notBefore = Date(now - ONE_DAY_MS)
        val notAfter = Date(now + TWENTY_YEARS_MS)
        val subject = X500Name(SUBJECT_DN)
        val builder = JcaX509v3CertificateBuilder(
            subject, BigInteger.valueOf(now), notBefore, notAfter, subject, keyPair.public
        )
        val signer = JcaContentSignerBuilder("SHA256withRSA").setProvider(PROVIDER).build(keyPair.private)
        return JcaX509CertificateConverter().setProvider(PROVIDER).getCertificate(builder.build(signer))
    }

    private fun toPem(type: String, der: ByteArray): String {
        val sw = StringWriter()
        JcaPEMWriter(sw).use { it.writeObject(PemObject(type, der)) }
        return sw.toString()
    }

    private fun decodePemBody(pem: String): ByteArray {
        val base64 = pem.lineSequence()
            .filterNot { it.startsWith("-----") }
            .joinToString("")
            .trim()
        return Base64.decode(base64, Base64.DEFAULT)
    }

    private fun ensureProvider() {
        if (Security.getProvider(PROVIDER) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    private companion object {
        const val CERT_FILE = "genstream_client.crt"
        const val KEY_FILE = "genstream_client.key"
        const val PROVIDER = "BC"
        const val KEY_SIZE = 2048
        const val SUBJECT_DN = "CN=GenStream Client"
        const val ONE_DAY_MS = 24L * 60 * 60 * 1000
        const val TWENTY_YEARS_MS = 20L * 365 * 24 * 60 * 60 * 1000
    }
}
