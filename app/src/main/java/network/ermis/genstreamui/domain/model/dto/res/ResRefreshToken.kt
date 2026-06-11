package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/**
 * Response làm mới token — phiên đăng nhập mới bọc trong "data".
 * { data: { access_token, refresh_token, token_type, expires_in, user }, message }
 */
@Keep
data class ResRefreshToken(
    val message: String? = null,
    val data: AuthSessionDTO? = null
)
