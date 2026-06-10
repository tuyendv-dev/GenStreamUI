package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Phiên đăng nhập (token + user) trả về trong "data" của các API auth bọc data:
 * verify-email, google login...
 */
@Keep
data class AuthSessionDTO(
    @SerializedName("access_token")
    val accessToken: String? = null,
    @SerializedName("refresh_token")
    val refreshToken: String? = null,
    @SerializedName("token_type")
    val tokenType: String? = null,
    @SerializedName("expires_in")
    val expiresIn: Int? = null,
    val user: UserDTO? = null
)
