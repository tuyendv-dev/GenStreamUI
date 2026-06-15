package network.ermis.genstreamui.domain.repository

import network.ermis.genstreamui.domain.model.ConnectionToken
import network.ermis.genstreamui.domain.model.TokenAuthResult

/**
 * Repository cho các call **trực tiếp tới VM** (gateway), tách khỏi backend
 * (staging-api.genstream.io). Hiện có token-auth; sau sẽ thêm agent launch/close.
 */
interface GatewayRepository {

    /**
     * token-auth: đổi [connection] token + client cert lấy quyền stream từ host, pin server cert (TOFU).
     * @param deviceName hostname máy (hiển thị ở host).
     */
    suspend fun tokenAuth(connection: ConnectionToken, deviceName: String): TokenAuthResult
}
