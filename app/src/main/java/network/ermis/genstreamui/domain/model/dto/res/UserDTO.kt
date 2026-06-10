package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Thông tin người dùng trả về từ backend (dùng chung cho verify/login/me). */
@Keep
data class UserDTO(
    val id: Int? = null,
    val email: String? = null,
    @SerializedName("display_name")
    val displayName: String? = null,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    @SerializedName("email_verified")
    val emailVerified: Boolean? = null,
    @SerializedName("is_active")
    val isActive: Boolean? = null,
    val roles: List<String>? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)
