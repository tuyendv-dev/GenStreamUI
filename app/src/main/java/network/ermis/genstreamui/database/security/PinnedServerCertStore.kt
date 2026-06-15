package network.ermis.genstreamui.database.security

import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Kho cert host đã **pin theo TOFU** (Trust On First Use) — genstream-custom-auth.md §8.
 *
 * Ở token-auth (Stage 2), client chấp nhận server cert tự ký lần đầu, capture rồi pin vào đây theo
 * host. Mọi HTTPS sau tới host đó (serverinfo/launch/resume qua NvHTTP/mTLS) phải tin **đúng** cert
 * đã pin này thay vì tin mọi cert. In-memory theo vòng đời phiên (đủ cho 1 phiên = 1 VM).
 */
@Singleton
class PinnedServerCertStore @Inject constructor() {
    private val pins = ConcurrentHashMap<String, X509Certificate>()

    fun pin(host: String, cert: X509Certificate) {
        pins[host] = cert
    }

    fun get(host: String): X509Certificate? = pins[host]

    fun isPinned(host: String): Boolean = pins.containsKey(host)

    fun clear(host: String) {
        pins.remove(host)
    }
}
