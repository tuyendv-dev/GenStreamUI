package network.ermis.genstreamui.database.network.gateway

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import network.ermis.genstreamui.database.security.TofuTrustManager
import network.ermis.genstreamui.domain.model.dto.req.ReqTokenAuth
import network.ermis.genstreamui.domain.model.dto.res.ResTokenAuth
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext

/**
 * Client HTTP **động theo VM** cho token-auth (genstream-custom-auth.md §6 Stage 2):
 * gọi thẳng `https://host:base+2/api/auth/token` của VM (KHÁC baseUrl backend), TLS host tự ký →
 * dùng [TofuTrustManager] để chấp nhận + capture server cert.
 *
 * Single-shot: chỉ thực hiện 1 request và trả [Outcome] thô; logic warm-up retry / pin nằm ở repository.
 */
@Singleton
class GatewayTokenAuthClient @Inject constructor() {

    /**
     * @param httpStatus mã HTTP, **0** nếu unreachable/IO error.
     * @param body response đã parse (null nếu không có/không parse được).
     * @param serverChain chain cert host đã capture (để pin), null nếu handshake chưa xảy ra.
     */
    data class Outcome(
        val httpStatus: Int,
        val body: ResTokenAuth?,
        val serverChain: Array<X509Certificate>?
    )

    private val gson = Gson()
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    suspend fun tokenAuth(url: String, req: ReqTokenAuth): Outcome = withContext(Dispatchers.IO) {
        val tm = TofuTrustManager()
        val client = buildClient(tm)
        val request = Request.Builder()
            .url(url)
            .post(gson.toJson(req).toRequestBody(jsonMedia))
            .build()
        try {
            client.newCall(request).execute().use { resp ->
                val bodyStr = resp.body?.string()
                val parsed = bodyStr?.let {
                    runCatching { gson.fromJson(it, ResTokenAuth::class.java) }.getOrNull()
                }
                Outcome(resp.code, parsed, tm.capturedChain)
            }
        } catch (e: IOException) {
            // Unreachable / handshake fail / timeout — coi như chưa sẵn sàng (warm-up).
            Outcome(httpStatus = 0, body = null, serverChain = tm.capturedChain)
        }
    }

    private fun buildClient(tm: TofuTrustManager): OkHttpClient {
        val ssl = SSLContext.getInstance("TLS").apply { init(null, arrayOf(tm), SecureRandom()) }
        return OkHttpClient.Builder()
            .sslSocketFactory(ssl.socketFactory, tm)
            // Host là IP + cert tự ký → bỏ hostname verify (TOFU đã giới hạn ở đúng peer cert).
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .build()
    }
}
