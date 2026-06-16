package network.ermis.genstreamui.domain.model

/**
 * Model core token kết nối ở tầng domain — dùng để mở luồng stream (Moonlight) tới host VM.
 * Map từ [network.ermis.genstreamui.domain.model.dto.res.ConnectionTokenDTO] qua ConnectionTokenMapper.
 *
 * [token] có hạn ngắn ([expiresIn] giây) nên xin xong cần connect ngay.
 *
 * Mô hình kết nối là **token-auth + cert pinning** (thay PIN pairing của Moonlight): sau khi có token,
 * client POST token + client cert PEM tới token-auth endpoint của VM để host authorize cert đó.
 *
 * [port] là **base port** (cổng HTTP GameStream qua relay). Mọi cổng dịch vụ khác suy ra từ base
 * qua [vmPorts] (xem genstream-custom-auth.md §7). token-auth nằm ở **base + 2**.
 */
data class ConnectionToken(
    val token: String = "",
    val expiresIn: Int = 0,
    val host: String = "",
    val port: Int = 0,
    val sessionId: Int = 0
) {
    /** Đủ thông tin host + base port + token để bắt đầu connect. */
    val isValid: Boolean get() = token.isNotEmpty() && host.isNotEmpty() && port > 0

    /** Bản đồ port của VM dẫn xuất từ base [port]. */
    val vmPorts: VmPorts get() = VmPorts(port)

    /** Endpoint token-auth — POST https://{host}:{base+2}/api/auth/token (cert host tự ký, TOFU). */
    val authTokenUrl: String get() = "https://$host:${vmPorts.tokenAuth}/api/auth/token"
}
