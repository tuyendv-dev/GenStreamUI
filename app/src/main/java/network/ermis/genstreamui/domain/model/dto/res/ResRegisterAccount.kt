package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Response đăng ký tài khoản.
 * Ví dụ:
 * { "data": { "user_id": 5, "email": "..." },
 *   "message": "Registration successful. Please check your email to verify your account." }
 */
@Keep
data class ResRegisterAccount(
    val message: String? = null,
    val data: RegisterData? = null
)

@Keep
data class RegisterData(
    @SerializedName("user_id")
    val userId: Int? = null,
    val email: String? = null
)
