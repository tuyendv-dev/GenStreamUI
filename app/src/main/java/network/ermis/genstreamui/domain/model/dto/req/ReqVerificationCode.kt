package network.ermis.genstreamui.domain.model.dto.req

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Request xác minh email: { user_id, otp }. */
@Keep
data class ReqVerificationCode(
    @SerializedName("user_id")
    val userId: Int = 0,
    val otp: String = ""
)
