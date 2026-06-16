package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Response agent /launch (genstream-custom-auth.md §6 Stage 3a). KHÔNG bọc envelope `data`.
 * Launch là fire-and-forget; fail thì client vẫn tiếp tục stream Desktop.
 */
@Keep
data class ResAgentLaunch(
    val ok: Boolean = false,
    val pid: Int? = null,
    @SerializedName("save_status")
    val saveStatus: String? = null,
    val message: String? = null,
    val error: String? = null
)
