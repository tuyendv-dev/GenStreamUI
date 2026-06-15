package network.ermis.genstreamui.domain.model.dto.req

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Request xin token kết nối tới host stream — POST /sessions/{id}/connection-token. */
@Keep
data class ReqConnectionToken(
    @SerializedName("device_type")
    val deviceType: String = "android",
    @SerializedName("device_name")
    val deviceName: String = ""
)
