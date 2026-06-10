package network.ermis.genstreamui.domain.model.dto.req

import androidx.annotation.Keep

/** Request gửi lại mã OTP theo email. */
@Keep
data class ReqResendOtpDTO(
    val email: String = ""
)
