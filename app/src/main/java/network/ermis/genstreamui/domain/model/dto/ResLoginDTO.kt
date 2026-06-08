package network.ermis.genstreamui.domain.model.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Response đăng nhập. Cấu trúc field là placeholder — chỉnh lại theo spec backend ermis thật.
 */
@Keep
data class ResLoginDTO(
    @SerializedName("access_token")
    val accessToken: String? = null,
    @SerializedName("refresh_token")
    val refreshToken: String? = null,
    @SerializedName("expires_in")
    val expiresIn: Long? = null,
    @SerializedName("user_id")
    val userId: String? = null
)
