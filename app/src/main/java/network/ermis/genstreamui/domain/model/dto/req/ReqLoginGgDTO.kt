package network.ermis.genstreamui.domain.model.dto.req

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Request đăng nhập Google theo OAuth Authorization Code + PKCE. */
@Keep
data class ReqLoginGgDTO(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("code_verifier")
    val codeVerifier: String = "",
    @SerializedName("redirect_uri")
    val redirectUri: String = ""
)
