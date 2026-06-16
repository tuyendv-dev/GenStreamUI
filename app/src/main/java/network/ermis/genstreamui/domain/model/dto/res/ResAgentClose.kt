package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Response agent /close (genstream-custom-auth.md §9). KHÔNG bọc envelope `data`.
 * Phải gọi /close TRƯỚC /end (vì agent verify token với backend, /end làm phiên stopped → verify fail).
 */
@Keep
data class ResAgentClose(
    val ok: Boolean = false,
    val killed: Boolean? = null,
    @SerializedName("backup_status")
    val backupStatus: String? = null,
    val message: String? = null,
    val error: String? = null
)
