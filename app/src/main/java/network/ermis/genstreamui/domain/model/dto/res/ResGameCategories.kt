package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/** Response cho GET /games/categories. Trả về danh sách tên category. */
@Keep
data class ResGameCategories(
    val data: List<String>? = null,
    val message: String? = null
)
