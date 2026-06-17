package network.ermis.genstreamui.domain.repository

import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.res.ResSession
import network.ermis.genstreamui.domain.model.dto.res.ResUserSubscriptions

/**
 * Repository interface tầng domain cho thông tin thuê bao. Song song với [SessionRepository].
 */
interface SubscriptionRepository {

    /** Lấy danh sách gói thuê bao của user hiện tại — GET /users/me/subscriptions. */
    suspend fun getUserSubscriptions(): ResultWrapper<ResUserSubscriptions>

    /**
     * Lấy phiên chơi đang active của gói [subscriptionId] —
     * GET /users/me/subscriptions/{id}/active-session. `data = null` nếu không có phiên active.
     */
    suspend fun getSessionActiveBySubscription(subscriptionId: Int): ResultWrapper<ResSession>
}
