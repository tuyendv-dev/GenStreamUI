package network.ermis.genstreamui.presentation.windows

/**
 * Các giai đoạn của luồng kết nối tới máy tính (VM) — phản ánh stage 0–3 trong
 * genstream-custom-auth.md §6. UI [WindowsConnectActivity] render theo state này.
 */
sealed interface ConnectStage {
    data object Idle : ConnectStage

    /** Đang lấy danh sách gói thuê bao để xác định subscription mở phiên. */
    data object ResolvingSubscription : ConnectStage

    /**
     * User có nhiều hơn 1 gói → cần chọn 1 gói để đi tiếp. UI hiện popup không thể tắt.
     * Sau khi chọn, gọi [WindowsConnectViewModel.onSubscriptionChosen].
     */
    data class ChoosingSubscription(
        val options: List<network.ermis.genstreamui.domain.model.Subscription>
    ) : ConnectStage

    /** Stage 0 — tạo phiên. */
    data object CreatingSession : ConnectStage

    /**
     * Gói đang có phiên chạy game khác với game user muốn chơi → cần user chọn:
     * tiếp tục [oldGameTitle] (connect phiên cũ) hay chuyển sang [newGameTitle] (end cũ + tạo mới).
     * Sau khi chọn, gọi [WindowsConnectViewModel.onSessionConflictResolved].
     */
    data class ConflictingSession(
        val oldGameTitle: String,
        val newGameTitle: String
    ) : ConnectStage

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

    /** Thất bại ở bất kỳ stage nào. [canRetry] = false khi thử lại vô nghĩa (vd: chưa có gói thuê bao). */
    data class Failed(val message: String, val canRetry: Boolean = true) : ConnectStage
}
