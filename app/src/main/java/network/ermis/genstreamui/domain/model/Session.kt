package network.ermis.genstreamui.domain.model

/**
 * Model core phiên chơi ở tầng domain — UI/ViewModel làm việc trực tiếp với model này thay vì DTO.
 * Map từ [network.ermis.genstreamui.domain.model.dto.res.SessionDTO] qua SessionMapper.
 *
 * Các trường non-null (có default) để UI khỏi xử lý null rải rác. Riêng [ipAddress]/[sunshinePort]
 * là thông tin host để connect tới luồng stream (Moonlight) — rỗng/0 nghĩa là host chưa sẵn sàng.
 */
data class Session(
    val id: Int = 0,
    val userId: Int = 0,
    val subscriptionId: Int = 0,
    val status: String = "",
    val billingMode: String = "",
    val startedAt: String = "",
    val modeStartedAt: String = "",
    val endedAt: String = "",
    val durationSeconds: Long = 0,
    val overageSeconds: Long = 0,
    val createdAt: String = "",
    val serverId: Int = 0,
    val ipAddress: String = "",
    val sunshinePort: Int = 0,
    val node: String = "",
    val proxmoxVmid: Int = 0,
    val vmUuid: String = "",
    val lastError: String = "",
    val readyAt: String = "",
    val lastHeartbeatAt: String = "",
    val stoppedAt: String = "",
    val userEmail: String = "",
    val serverHostname: String = ""
) {
    /** Trạng thái phiên (theo backend). Dùng [SessionStatus] để so khớp an toàn. */
    val sessionStatus: SessionStatus get() = SessionStatus.from(status)

    /** Host đã có đủ địa chỉ để mở luồng stream (Moonlight) hay chưa. */
    val hasHostInfo: Boolean get() = ipAddress.isNotEmpty() && sunshinePort > 0

    /** Phiên đã sẵn sàng để connect: trạng thái ready và có host info. */
    val isReadyToConnect: Boolean get() = sessionStatus == SessionStatus.READY && hasHostInfo
}

/** Vòng đời một phiên chơi phía backend. [UNKNOWN] cho giá trị chưa biết để client không vỡ. */
enum class SessionStatus(val raw: String) {
    PROVISIONING("provisioning"),
    READY("ready"),
    ACTIVE("active"),
    STOPPED("stopped"),
    ERROR("error"),
    UNKNOWN("");

    companion object {
        fun from(raw: String?): SessionStatus =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: UNKNOWN
    }
}
