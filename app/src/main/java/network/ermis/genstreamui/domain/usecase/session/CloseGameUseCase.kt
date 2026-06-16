package network.ermis.genstreamui.domain.usecase.session

import network.ermis.genstreamui.domain.model.AgentCloseResult
import network.ermis.genstreamui.domain.model.AgentToken
import network.ermis.genstreamui.domain.repository.GatewayRepository
import javax.inject.Inject

/**
 * UseCase đóng game qua agent — POST http://host:base+3/close (genstream-custom-auth.md §9).
 * ⚠️ Phải gọi TRƯỚC [EndSessionUseCase] (/end làm phiên `stopped` → agent verify token fail).
 */
class CloseGameUseCase @Inject constructor(
    private val gatewayRepository: GatewayRepository
) {
    suspend operator fun invoke(
        agentToken: AgentToken,
        platform: String,
        appid: Int
    ): AgentCloseResult = gatewayRepository.closeGame(agentToken, platform, appid)
}
