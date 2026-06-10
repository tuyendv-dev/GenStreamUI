package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/** Response đặt lại mật khẩu. */
@Keep
data class ResResetPassword(
    val code: Int? = null,
    val message: String? = null,
    val data: Any? = null
)
