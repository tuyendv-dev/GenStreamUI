package network.ermis.genstreamui.domain.model.dto.req

import androidx.annotation.Keep

/** Request gửi yêu cầu quên mật khẩu theo email. */
@Keep
data class ReqForgetPasswordDTO(
    val email: String = ""
)
