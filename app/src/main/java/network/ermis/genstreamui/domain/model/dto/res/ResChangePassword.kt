package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/** Response đổi mật khẩu — chỉ có message. */
@Keep
data class ResChangePassword(
    val message: String? = null
)
