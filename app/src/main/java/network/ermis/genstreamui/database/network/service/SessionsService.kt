package network.ermis.genstreamui.database.network.service

import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.req.ReqConnectionToken
import network.ermis.genstreamui.domain.model.dto.req.ReqStartSession
import network.ermis.genstreamui.domain.model.dto.res.ResConnectionToken
import network.ermis.genstreamui.domain.model.dto.res.ResEndSession
import network.ermis.genstreamui.domain.model.dto.res.ResSession
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit service cho phiên chơi (provision host stream). Song song với [GameService].
 *
 * Header Authorization (Bearer) được gắn tự động bởi
 * [network.ermis.genstreamui.database.network.interceptor.AuthInterceptor] (path không thuộc /auth/).
 *
 * Các endpoint khác (lấy/poll trạng thái, dừng phiên...) sẽ bổ sung dần vào đây.
 */
interface SessionsService {

    /** Mở một phiên chơi mới — POST /sessions. */
    @POST("/sessions")
    suspend fun startSession(
        @Body body: ReqStartSession,
        @Header("Accept") accept: String = "application/json"
    ): ResultWrapper<ResSession>

    /**
     * Xin token kết nối tới host stream — POST /sessions/{id}/connection-token.
     * Khi VM chưa sẵn sàng backend trả error "VM_NOT_READY" → client poll lại sau.
     */
    @POST("/sessions/{id}/connection-token")
    suspend fun getConnectionToken(
        @Path("id") sessionId: Int,
        @Body body: ReqConnectionToken,
        @Header("Accept") accept: String = "application/json"
    ): ResultWrapper<ResConnectionToken>

    /**
     * Giữ nhịp phiên (keep-alive) — POST /sessions/{id}/heartbeat (body rỗng).
     * Trả về snapshot phiên mới nhất; gọi định kỳ trong lúc đang stream.
     */
    @POST("/sessions/{id}/heartbeat")
    suspend fun heartbeat(
        @Path("id") sessionId: Int,
        @Header("Accept") accept: String = "application/json"
    ): ResultWrapper<ResSession>

    /**
     * Kết thúc phiên — POST /sessions/{id}/end (body rỗng).
     * Trả snapshot phiên đã kết thúc + số liệu tính cước (consumed_hours, overage_seconds).
     */
    @POST("/sessions/{id}/end")
    suspend fun endSession(
        @Path("id") sessionId: Int,
        @Header("Accept") accept: String = "application/json"
    ): ResultWrapper<ResEndSession>
}
