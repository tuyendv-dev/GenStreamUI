package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Response cho POST /sessions/{id}/agent-token — token (Bearer) để gọi genstream-agent
 * (/launch, /close) trên VM. TTL 3600s, reusable (genstream-custom-auth.md §6 Stage 3a).
 */
@Keep
data class ResAgentToken(
    val data: AgentTokenDTO? = null,
    val message: String? = null
)

@Keep
data class AgentTokenDTO(
    val token: String? = null,
    @SerializedName("expires_in")
    val expiresIn: Int? = null,
    // Dùng lại [VmEndpointDTO] khai báo ở ConnectionTokenDTO.kt.
    @SerializedName("vm_endpoint")
    val vmEndpoint: VmEndpointDTO? = null
)
