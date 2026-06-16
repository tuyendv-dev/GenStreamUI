package network.ermis.genstreamui.domain.usecase.session

import network.ermis.genstreamui.domain.model.AgentLaunchResult
import network.ermis.genstreamui.domain.model.AgentToken
import network.ermis.genstreamui.domain.repository.GatewayRepository
import javax.inject.Inject

/**
 * UseCase mở game qua agent (Play-Now) — POST http://host:base+3/launch (genstream-custom-auth.md §6
 * Stage 3a). Fire-and-forget: caller có thể bỏ qua [AgentLaunchResult.Failed] và vẫn stream Desktop.
 * [agentToken] lấy từ [GetAgentTokenUseCase].
 */
class LaunchGameUseCase @Inject constructor(
    private val gatewayRepository: GatewayRepository
) {
    suspend operator fun invoke(
        agentToken: AgentToken,
        platform: String,
        appid: Int
    ): AgentLaunchResult = gatewayRepository.launchGame(agentToken, platform, appid)
}
