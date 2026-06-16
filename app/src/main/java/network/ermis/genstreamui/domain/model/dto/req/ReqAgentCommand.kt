package network.ermis.genstreamui.domain.model.dto.req

import androidx.annotation.Keep

/**
 * Body cho agent /launch và /close — POST http://host:base+3/{launch|close}
 * (genstream-custom-auth.md §6 Stage 3a / §9). [platform] vd "steam", [appid] = id app trên host.
 */
@Keep
data class ReqAgentCommand(
    val platform: String,
    val appid: Int
)
