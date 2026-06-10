package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/** Response yêu cầu quên mật khẩu. */
@Keep
data class ResForgetPassword(
    val code: Int? = null,
    val message: String? = null,
    val data: Any? = null
)
