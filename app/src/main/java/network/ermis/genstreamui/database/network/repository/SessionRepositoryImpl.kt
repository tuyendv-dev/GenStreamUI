package network.ermis.genstreamui.database.network.repository

import network.ermis.genstreamui.database.network.service.SessionsService
import network.ermis.genstreamui.domain.model.dto.req.ReqConnectionToken
import network.ermis.genstreamui.domain.model.dto.req.ReqStartSession
import network.ermis.genstreamui.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * Triển khai [SessionRepository]: delegate API sang [SessionsService].
 * Phiên chơi không cache cục bộ (state đổi liên tục phía server) nên không đụng Room.
 */
class SessionRepositoryImpl @Inject constructor(
    private val sessionsService: SessionsService
) : SessionRepository {

    override suspend fun startSession(subscriptionId: Int) =
        sessionsService.startSession(ReqStartSession(subscriptionId = subscriptionId))

    override suspend fun getConnectionToken(
        sessionId: Int,
        deviceName: String,
        deviceType: String
    ) = sessionsService.getConnectionToken(
        sessionId = sessionId,
        body = ReqConnectionToken(deviceType = deviceType, deviceName = deviceName)
    )

    override suspend fun heartbeat(sessionId: Int) =
        sessionsService.heartbeat(sessionId)

    override suspend fun endSession(sessionId: Int) =
        sessionsService.endSession(sessionId)
}
