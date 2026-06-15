package network.ermis.genstreamui.domain.usecase.session

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.EndedSession
import network.ermis.genstreamui.domain.model.mapper.toDomain
import network.ermis.genstreamui.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * UseCase kết thúc một phiên chơi (POST /sessions/{id}/end). Phát [UiState.Loading] rồi
 * [UiState.Success] với [EndedSession] (snapshot phiên đã dừng + số liệu tính cước).
 */
class EndSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(sessionId: Int) = flow<UiState<EndedSession>> {
        emit(UiState.Loading)
        try {
            when (val response = sessionRepository.endSession(sessionId)) {
                is ResultWrapper.Success -> {
                    val data = response.value.data
                    if (data != null) {
                        emit(UiState.Success(data.toDomain()))
                    } else {
                        emit(UiState.Error(response.value.message ?: "Không kết thúc được phiên"))
                    }
                }
                is ResultWrapper.GenericError -> emit(
                    UiState.Error(
                        message = response.message ?: "Không kết thúc được phiên",
                        code = response.code?.toString().orEmpty()
                    )
                )
                ResultWrapper.NetworkError -> emit(UiState.Error("Không có kết nối mạng"))
            }
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Đã có lỗi xảy ra"))
        }
    }.flowOn(Dispatchers.IO)
}
