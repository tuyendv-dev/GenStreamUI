package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/** Response bọc "data" cho /users/me (get/update). Dùng lại [UserDTO] cho phần data. */
@Keep
data class ResUserInfo(
    val data: UserDTO? = null,
    val message: String? = null
)
