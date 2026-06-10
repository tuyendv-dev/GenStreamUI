package network.ermis.genstreamui.domain.model.dto.req

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Request đăng ký tài khoản: email, password, display_name. */
@Keep
data class ReqRegisterAccount(
    val email: String = "",
    val password: String = "",
    @SerializedName("display_name")
    val displayName: String = ""
)
