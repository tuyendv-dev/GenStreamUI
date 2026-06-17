package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/** Response bọc "data" cho GET /users/me/subscriptions — danh sách gói thuê bao của user. */
@Keep
data class ResUserSubscriptions(
    val data: List<SubscriptionDTO>? = null,
    val message: String? = null
)
