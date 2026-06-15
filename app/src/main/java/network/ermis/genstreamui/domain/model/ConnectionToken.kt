package network.ermis.genstreamui.domain.model

/**
 * Model core token kết nối ở tầng domain — dùng để mở luồng stream (Moonlight) tới host VM.
 * Map từ [network.ermis.genstreamui.domain.model.dto.res.ConnectionTokenDTO] qua ConnectionTokenMapper.
 *
 * [token] có hạn ngắn ([expiresIn] giây) nên xin xong cần connect ngay.
 */
data class ConnectionToken(
    val token: String = "",
    val expiresIn: Int = 0,
    val host: String = "",
    val port: Int = 0,
    val sessionId: Int = 0
) {
    /** Đủ thông tin host:port + token để bắt đầu connect. */
    val isValid: Boolean get() = token.isNotEmpty() && host.isNotEmpty() && port > 0
}
