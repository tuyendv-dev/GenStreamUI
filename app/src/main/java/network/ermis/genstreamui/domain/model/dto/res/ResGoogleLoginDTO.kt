package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/**
 * Response đăng nhập Google (OAuth code + PKCE) — phiên đăng nhập bọc trong "data".
 * { data: { access_token, refresh_token, token_type, expires_in, user }, message }
 */
@Keep
data class ResGoogleLoginDTO(
    val message: String? = null,
    val data: AuthSessionDTO? = null
)
