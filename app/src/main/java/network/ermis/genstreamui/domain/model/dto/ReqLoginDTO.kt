package network.ermis.genstreamui.domain.model.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Request body đăng nhập. */
@Keep
data class ReqLoginDTO(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)
