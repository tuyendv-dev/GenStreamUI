package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/**
 * Response cho POST /sessions/{id}/connection-token.
 * - Thành công: [data] có token + vm_endpoint.
 * - VM chưa sẵn sàng: backend trả [error] = "VM_NOT_READY" (data rỗng) — client poll lại.
 */
@Keep
data class ResConnectionToken(
    val data: ConnectionTokenDTO? = null,
    val message: String? = null,
    val error: String? = null
)
