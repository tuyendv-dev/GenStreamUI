package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Một phiên chơi (session) trong response. Map đủ schema backend trả về cho /sessions —
 * các field provision (ip_address, sunshine_port, status...) phục vụ cho việc connect tới host
 * stream (Moonlight). Field nào null lúc "provisioning" sẽ được điền ở các lần poll sau.
 */
@Keep
data class SessionDTO(
    val id: Int? = null,
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("subscription_id")
    val subscriptionId: Int? = null,
    val status: String? = null,
    @SerializedName("billing_mode")
    val billingMode: String? = null,
    @SerializedName("started_at")
    val startedAt: String? = null,
    @SerializedName("mode_started_at")
    val modeStartedAt: String? = null,
    @SerializedName("ended_at")
    val endedAt: String? = null,
    @SerializedName("duration_seconds")
    val durationSeconds: Long? = null,
    @SerializedName("overage_seconds")
    val overageSeconds: Long? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("server_id")
    val serverId: Int? = null,
    @SerializedName("ip_address")
    val ipAddress: String? = null,
    @SerializedName("sunshine_port")
    val sunshinePort: Int? = null,
    val node: String? = null,
    @SerializedName("proxmox_vmid")
    val proxmoxVmid: Int? = null,
    @SerializedName("vm_uuid")
    val vmUuid: String? = null,
    @SerializedName("last_error")
    val lastError: String? = null,
    @SerializedName("ready_at")
    val readyAt: String? = null,
    @SerializedName("last_heartbeat_at")
    val lastHeartbeatAt: String? = null,
    @SerializedName("stopped_at")
    val stoppedAt: String? = null,
    @SerializedName("user_email")
    val userEmail: String? = null,
    @SerializedName("server_hostname")
    val serverHostname: String? = null
)
