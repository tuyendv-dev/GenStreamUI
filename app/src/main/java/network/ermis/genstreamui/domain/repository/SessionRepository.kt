package network.ermis.genstreamui.domain.repository

import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.res.ResAgentToken
import network.ermis.genstreamui.domain.model.dto.res.ResConnectionToken
import network.ermis.genstreamui.domain.model.dto.res.ResEndSession
import network.ermis.genstreamui.domain.model.dto.res.ResSession

/**
 * Repository interface tầng domain cho phiên chơi. Song song với [GameRepository].
 * Phục vụ luồng connect tới host stream (Moonlight): mở phiên để backend provision host.
 */
interface SessionRepository {

    /** Mở một phiên chơi mới cho [subscriptionId] — POST /sessions. */
    suspend fun startSession(subscriptionId: Int): ResultWrapper<ResSession>

    /** Xin token kết nối tới host của phiên [sessionId] — POST /sessions/{id}/connection-token. */
    suspend fun getConnectionToken(
        sessionId: Int,
        deviceName: String,
        deviceType: String
    ): ResultWrapper<ResConnectionToken>

    /** Giữ nhịp phiên [sessionId] (keep-alive) — POST /sessions/{id}/heartbeat. */
    suspend fun heartbeat(sessionId: Int): ResultWrapper<ResSession>

    /** Kết thúc phiên [sessionId] — POST /sessions/{id}/end. */
    suspend fun endSession(sessionId: Int): ResultWrapper<ResEndSession>

    /** Probe trạng thái phiên [sessionId] (không side-effect) — GET /sessions/{id}. */
    suspend fun getSession(sessionId: Int): ResultWrapper<ResSession>

    /** Lấy agent token cho phiên [sessionId] — POST /sessions/{id}/agent-token. */
    suspend fun getAgentToken(sessionId: Int): ResultWrapper<ResAgentToken>
}
