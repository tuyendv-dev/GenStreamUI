package network.ermis.genstreamui.domain.model

/**
 * Kết quả token-auth với host (genstream-custom-auth.md §6 Stage 2).
 *
 * Phân biệt 3 nhánh để caller xử lý đúng:
 * - [Authorized]: host đã authorize client cert + server cert đã được pin → tiếp tục đăng ký host & stream.
 * - [Rejected]: token bị từ chối (status false / 401 / 4xx khác) → phiên/token hỏng, dừng.
 * - [Error]: VM chưa phản hồi sau warm-up retry (502/503/504/unreachable) → coi như fail, báo lỗi.
 */
sealed interface TokenAuthResult {
    data class Authorized(val effectiveName: String) : TokenAuthResult
    data class Rejected(val message: String, val code: String = "") : TokenAuthResult
    data class Error(val message: String) : TokenAuthResult
}
