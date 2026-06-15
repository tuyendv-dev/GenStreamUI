package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/**
 * Response bọc "data" cho các endpoint thao tác một phiên chơi (vd: POST /sessions).
 * Cùng kiểu envelope với [ResGameDetail].
 */
@Keep
data class ResSession(
    val data: SessionDTO? = null,
    val message: String? = null
)
