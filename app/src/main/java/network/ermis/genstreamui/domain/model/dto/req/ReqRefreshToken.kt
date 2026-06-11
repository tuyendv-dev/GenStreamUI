package network.ermis.genstreamui.domain.model.dto.req

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Request làm mới phiên đăng nhập bằng refresh token. */
@Keep
data class ReqRefreshToken(
    @SerializedName("refresh_token")
    val refreshToken: String = ""
)
