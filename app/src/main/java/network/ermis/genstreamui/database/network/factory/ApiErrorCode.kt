package network.ermis.genstreamui.database.network.factory

/**
 * Error code chuẩn hoá từ field `error` của envelope lỗi backend
 * (`{ "error": "ERROR_CODE", "message": "...", "detail": null }`) — xem genstream-custom-auth.md §3.
 *
 * Client phải switch theo code này thay vì chỉ theo HTTP status: vd cả [VM_NOT_READY] và
 * [SESSION_NOT_READY] đều là HTTP 409 nhưng ý nghĩa trái ngược (poll tiếp vs phiên đã chết).
 */
enum class ApiErrorCode(val raw: String) {
    VALIDATION_ERROR("VALIDATION_ERROR"),
    TOKEN_FORMAT_INVALID("TOKEN_FORMAT_INVALID"),
    AUTH_INVALID_CREDENTIALS("AUTH_INVALID_CREDENTIALS"),
    AUTH_TOKEN_INVALID("AUTH_TOKEN_INVALID"),
    AUTH_TOKEN_EXPIRED("AUTH_TOKEN_EXPIRED"),
    AUTH_EMAIL_NOT_VERIFIED("AUTH_EMAIL_NOT_VERIFIED"),
    AUTH_FORBIDDEN("AUTH_FORBIDDEN"),
    SESSION_NOT_OWNED("SESSION_NOT_OWNED"),
    SUBSCRIPTION_NOT_ACTIVE("SUBSCRIPTION_NOT_ACTIVE"),
    SUBSCRIPTION_NOT_FOUND("SUBSCRIPTION_NOT_FOUND"),
    SESSION_NOT_FOUND("SESSION_NOT_FOUND"),
    TOKEN_NOT_FOUND("TOKEN_NOT_FOUND"),

    /** VM đang provisioning → poll tiếp connection-token. */
    VM_NOT_READY("VM_NOT_READY"),

    /** Phiên đã stopped/error/sai trạng thái → dừng, không poll. */
    SESSION_NOT_READY("SESSION_NOT_READY"),
    SESSION_INVALID_STATE("SESSION_INVALID_STATE"),
    SESSION_ENDED("SESSION_ENDED"),
    SUBSCRIPTION_NO_HOURS("SUBSCRIPTION_NO_HOURS"),
    RATE_LIMITED("RATE_LIMITED"),

    /** Không khớp code đã biết (hoặc lỗi không có field error, vd lỗi HTTP/transport). */
    UNKNOWN("");

    companion object {
        fun from(raw: String?): ApiErrorCode =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: UNKNOWN
    }
}
