package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Token kết nối tới host stream (Moonlight) cho một phiên — data của
 * POST /sessions/{id}/connection-token. [vmEndpoint] là host:port để mở luồng stream.
 */
@Keep
data class ConnectionTokenDTO(
    val token: String? = null,
    @SerializedName("expires_in")
    val expiresIn: Int? = null,
    @SerializedName("vm_endpoint")
    val vmEndpoint: VmEndpointDTO? = null,
    @SerializedName("session_id")
    val sessionId: Int? = null
)

/** Địa chỉ host VM để connect luồng stream. */
@Keep
data class VmEndpointDTO(
    val host: String? = null,
    val port: Int? = null
)
