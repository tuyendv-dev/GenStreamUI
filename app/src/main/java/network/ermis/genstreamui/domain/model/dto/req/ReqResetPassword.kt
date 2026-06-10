package network.ermis.genstreamui.domain.model.dto.req

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Request đặt lại mật khẩu (mã xác minh + email + mật khẩu mới). */
@Keep
data class ReqResetPassword(
    val code: String = "",
    val email: String = "",
    @SerializedName("new_password")
    val password: String = ""
)
