package network.ermis.genstreamui.database.network.service

import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.res.ResSession
import network.ermis.genstreamui.domain.model.dto.res.ResUserSubscriptions
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * Retrofit service cho thông tin thuê bao (subscription) của người dùng.
 *
 * Header Authorization (Bearer) được gắn tự động bởi
 * [network.ermis.genstreamui.database.network.interceptor.AuthInterceptor] (path không thuộc /auth/).
 *
 * Các endpoint khác (chi tiết gói, mua/gia hạn, huỷ...) sẽ bổ sung dần vào đây.
 */
interface SubscriptionsService {

    /** Lấy danh sách gói thuê bao của user hiện tại — GET /users/me/subscriptions. */
    @GET("/users/me/subscriptions")
    suspend fun getUserSubscriptions(
        @Header("Accept") accept: String = "application/json"
    ): ResultWrapper<ResUserSubscriptions>

    /**
     * Lấy phiên chơi đang active của gói [subscriptionId] —
     * GET /users/me/subscriptions/{id}/active-session.
     *
     * Nếu gói không có phiên active, backend trả `data = null` (vẫn message "Success") →
     * [ResSession.data] sẽ là null, không phải lỗi.
     */
    @GET("/users/me/subscriptions/{id}/active-session")
    suspend fun getSessionActiveBySubscription(
        @Path("id") subscriptionId: Int,
        @Header("Accept") accept: String = "application/json"
    ): ResultWrapper<ResSession>
}
