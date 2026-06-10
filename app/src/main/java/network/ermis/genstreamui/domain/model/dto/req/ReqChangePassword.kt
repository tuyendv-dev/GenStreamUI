package network.ermis.genstreamui.domain.model.dto.req

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Request đổi mật khẩu. Field là placeholder — chỉnh theo spec backend thật. */
@Keep
data class ReqChangePassword(
    @SerializedName("old_password")
    val oldPassword: String = "",
    @SerializedName("new_password")
    val newPassword: String = ""
)
