package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/**
 * Response token-auth (genstream-custom-auth.md §6 Stage 2). KHÔNG bọc envelope `data`:
 * - Thành công: `{ "status": true, "name": "<effective name>" }`
 * - Thất bại: `{ "status": false, "error": "...", "code": "..." }`
 */
@Keep
data class ResTokenAuth(
    val status: Boolean = false,
    val name: String? = null,
    val error: String? = null,
    val code: String? = null
)
