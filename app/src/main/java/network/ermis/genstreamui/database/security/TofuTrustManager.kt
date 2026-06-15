package network.ermis.genstreamui.database.security

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * TrustManager kiểu **TOFU** cho token-auth: chấp nhận server cert tự ký của host (lần đầu chưa pin),
 * đồng thời **capture** chain để pin lại (xem [PinnedServerCertStore]) cho mọi HTTPS sau.
 *
 * ⚠️ Chỉ dùng cho riêng endpoint token-auth của VM GenStream (host do backend cấp). KHÔNG dùng làm
 * TrustManager chung của app.
 */
class TofuTrustManager : X509TrustManager {

    @Volatile
    var capturedChain: Array<X509Certificate>? = null
        private set

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        // Endpoint token-auth không yêu cầu client cert ở tầng TLS.
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        // TOFU: chấp nhận mọi cert (host tự ký), nhưng giữ lại để pin.
        val certs = chain?.filterIsInstance<X509Certificate>()?.toTypedArray()
        if (!certs.isNullOrEmpty()) capturedChain = certs
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
}
