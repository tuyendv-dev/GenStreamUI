package network.ermis.genstreamui.database.network.repository

import kotlinx.coroutines.delay
import network.ermis.genstreamui.database.network.gateway.GatewayTokenAuthClient
import network.ermis.genstreamui.database.security.IdentityManager
import network.ermis.genstreamui.database.security.PinnedServerCertStore
import network.ermis.genstreamui.domain.model.ConnectionToken
import network.ermis.genstreamui.domain.model.TokenAuthResult
import network.ermis.genstreamui.domain.model.dto.req.ReqTokenAuth
import network.ermis.genstreamui.domain.repository.GatewayRepository
import javax.inject.Inject

/**
 * Triển khai [GatewayRepository]. token-auth (genstream-custom-auth.md §6 Stage 2 + §8):
 * - Gửi connection token + client cert PEM ([IdentityManager]) tới host.
 * - **Warm-up retry**: VM vừa lên có thể trả 502/503/504 hoặc unreachable → thử lại 3s/lần × 10
 *   (~30s, < 60s TTL token). 4xx khác = token bị từ chối → fail luôn.
 * - Thành công → **pin** server cert đã capture vào [PinnedServerCertStore] cho các HTTPS sau.
 */
class GatewayRepositoryImpl @Inject constructor(
    private val identityManager: IdentityManager,
    private val client: GatewayTokenAuthClient,
    private val pinnedStore: PinnedServerCertStore
) : GatewayRepository {

    override suspend fun tokenAuth(connection: ConnectionToken, deviceName: String): TokenAuthResult {
        val req = ReqTokenAuth(
            token = connection.token,
            cert = identityManager.certificatePem,
            name = deviceName
        )
        val url = connection.authTokenUrl

        var attempt = 0
        while (true) {
            val outcome = client.tokenAuth(url, req)
            when {
                outcome.httpStatus == 200 -> {
                    val body = outcome.body
                    return if (body?.status == true) {
                        outcome.serverChain?.firstOrNull()?.let { pinnedStore.pin(connection.host, it) }
                        TokenAuthResult.Authorized(body.name ?: deviceName)
                    } else {
                        TokenAuthResult.Rejected(
                            message = body?.error ?: "Token bị từ chối",
                            code = body?.code.orEmpty()
                        )
                    }
                }
                // Warm-up: VM chưa trả lời → retry trong ngân sách TTL token.
                outcome.httpStatus == 0 || outcome.httpStatus in WARMUP_RETRY_STATUS -> {
                    if (++attempt >= MAX_WARMUP_RETRIES) {
                        return TokenAuthResult.Error("VM chưa phản hồi token-auth sau $MAX_WARMUP_RETRIES lần thử")
                    }
                    delay(WARMUP_RETRY_DELAY_MS)
                }
                // 401 / 4xx khác → token bị từ chối, dừng.
                else -> return TokenAuthResult.Rejected(
                    message = outcome.body?.error ?: "Token-auth thất bại (HTTP ${outcome.httpStatus})",
                    code = outcome.body?.code ?: outcome.httpStatus.toString()
                )
            }
        }
    }

    private companion object {
        val WARMUP_RETRY_STATUS = setOf(502, 503, 504)
        const val MAX_WARMUP_RETRIES = 10
        const val WARMUP_RETRY_DELAY_MS = 3_000L
    }
}
