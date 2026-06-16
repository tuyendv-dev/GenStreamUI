package network.ermis.genstreamui.domain.repository

import network.ermis.genstreamui.domain.model.AgentCloseResult
import network.ermis.genstreamui.domain.model.AgentLaunchResult
import network.ermis.genstreamui.domain.model.AgentToken
import network.ermis.genstreamui.domain.model.ConnectionToken
import network.ermis.genstreamui.domain.model.TokenAuthResult

/**
 * Repository cho các call **trực tiếp tới VM** (gateway), tách khỏi backend
 * (staging-api.genstream.io): token-auth (HTTPS base+2) và agent launch/close (HTTP base+3).
 */
interface GatewayRepository {

    /**
     * token-auth: đổi [connection] token + client cert lấy quyền stream từ host, pin server cert (TOFU).
     * @param deviceName hostname máy (hiển thị ở host).
     */
    suspend fun tokenAuth(connection: ConnectionToken, deviceName: String): TokenAuthResult

    /** Agent /launch — mở game [appid] (platform vd "steam") trên host. Fire-and-forget. */
    suspend fun launchGame(agentToken: AgentToken, platform: String, appid: Int): AgentLaunchResult

    /** Agent /close — đóng game [appid] trên host. Phải gọi TRƯỚC /end (§9). */
    suspend fun closeGame(agentToken: AgentToken, platform: String, appid: Int): AgentCloseResult
}
