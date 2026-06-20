package network.ermis.genstreamui.domain.model.dto.req

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Request mở một phiên chơi (provision host) — POST /sessions. */
@Keep
data class ReqStartSession(
    @SerializedName("subscription_id")
    val subscriptionId: Int = 0,
    /** Game gắn với phiên (Play-Now); null khi mở phiên không gắn game cụ thể (vd vào từ MineFragment). */
    @SerializedName("game_id")
    val gameId: Int? = null
)
