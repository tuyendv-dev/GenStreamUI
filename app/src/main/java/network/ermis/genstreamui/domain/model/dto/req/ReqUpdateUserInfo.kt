package network.ermis.genstreamui.domain.model.dto.req

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Request cập nhật thông tin người dùng. Field là placeholder — chỉnh theo spec backend thật. */
@Keep
data class ReqUpdateUserInfo(
    @SerializedName("display_name")
    val displayName: String? = null,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null
)
