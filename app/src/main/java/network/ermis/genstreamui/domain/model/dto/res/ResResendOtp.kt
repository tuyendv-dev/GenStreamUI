package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/**
 * Response gửi lại OTP.
 * { "data": { "message": "..." }, "message": "Success" }
 */
@Keep
data class ResResendOtp(
    val message: String? = null,
    val data: ResendOtpData? = null
)

@Keep
data class ResendOtpData(
    val message: String? = null
)
