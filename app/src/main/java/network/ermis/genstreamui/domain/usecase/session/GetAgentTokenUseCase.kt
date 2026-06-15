package network.ermis.genstreamui.domain.usecase.session

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.AgentToken
import network.ermis.genstreamui.domain.model.mapper.toDomain
import network.ermis.genstreamui.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * UseCase lấy agent token — POST /sessions/{id}/agent-token (genstream-custom-auth.md §6 Stage 3a).
 * Chỉ cần cho Play-Now (mở sẵn 1 game qua agent /launch). Suspend one-shot vì được gọi inline
 * trong orchestration; chỉ trả [UiState.Success]/[UiState.Error].
 */
class GetAgentTokenUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Int): UiState<AgentToken> = withContext(Dispatchers.IO) {
        try {
            when (val response = sessionRepository.getAgentToken(sessionId)) {
                is ResultWrapper.Success -> {
                    val data = response.value.data
                    if (data?.token?.isNotEmpty() == true) {
                        UiState.Success(data.toDomain())
                    } else {
                        UiState.Error(response.value.message ?: "Không lấy được agent token")
                    }
                }
                is ResultWrapper.GenericError -> UiState.Error(
                    message = response.message ?: "Không lấy được agent token",
                    code = response.error ?: response.code?.toString().orEmpty()
                )
                ResultWrapper.NetworkError -> UiState.Error("Không có kết nối mạng")
            }
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Đã có lỗi xảy ra")
        }
    }
}
