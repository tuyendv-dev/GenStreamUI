package network.ermis.genstreamui.domain.model.dto.req

import androidx.annotation.Keep

/**
 * Body token-auth — POST https://host:base+2/api/auth/token (genstream-custom-auth.md §6 Stage 2).
 * - [token]: connection token (single-use, 60s).
 * - [cert]: PEM đầy đủ của **client cert** (-----BEGIN/END CERTIFICATE-----), không phải DER/fingerprint.
 * - [name]: device name (hostname máy).
 */
@Keep
data class ReqTokenAuth(
    val token: String,
    val cert: String,
    val name: String
)
