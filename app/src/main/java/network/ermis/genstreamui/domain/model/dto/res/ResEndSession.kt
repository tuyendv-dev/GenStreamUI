package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Response cho POST /sessions/{id}/end. Khác với envelope phiên thường: [data] bọc thêm
 * [EndSessionDataDTO] gồm snapshot [SessionDTO] đã kết thúc + số liệu tính cước.
 */
@Keep
data class ResEndSession(
    val data: EndSessionDataDTO? = null,
    val message: String? = null
)

@Keep
data class EndSessionDataDTO(
    val session: SessionDTO? = null,
    // Số thập phân độ chính xác cao -> giữ String để không mất chính xác (vượt Double).
    @SerializedName("consumed_hours")
    val consumedHours: String? = null,
    @SerializedName("overage_seconds")
    val overageSeconds: Long? = null
)
