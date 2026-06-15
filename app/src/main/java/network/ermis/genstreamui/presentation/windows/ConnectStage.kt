package network.ermis.genstreamui.presentation.windows

/**
 * Các giai đoạn của luồng kết nối tới máy tính (VM) — phản ánh stage 0–3 trong
 * genstream-custom-auth.md §6. UI [WindowsConnectActivity] render theo state này.
 */
sealed interface ConnectStage {
    data object Idle : ConnectStage

    /** Stage 0 — tạo phiên. */
    data object CreatingSession : ConnectStage

    /** Stage 1 — chờ VM provision (đang poll connection-token). [attempt] = lần poll thứ mấy. */
    data class WaitingForVm(val attempt: Int) : ConnectStage

    /** Stage 2 — token-auth: host đang authorize thiết bị. */
    data object Authorizing : ConnectStage

    /** Đã token-auth xong, sẵn sàng vào stream (Stage 3b — serverinfo/stream sẽ nối ở phần native). */
    data class Connected(
        val deviceName: String,
        val host: String,
        val basePort: Int
    ) : ConnectStage

    /** Thất bại ở bất kỳ stage nào. */
    data class Failed(val message: String) : ConnectStage
}
