package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/**
 * Response xác minh email — trả về cả phiên đăng nhập (token + user).
 * { data: { access_token, refresh_token, token_type, expires_in, user }, message }
 */
@Keep
data class ResVerificationCode(
    val message: String? = null,
    val data: AuthSessionDTO? = null
)
