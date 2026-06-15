package network.ermis.genstreamui.domain.usecase.session

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.Session
import network.ermis.genstreamui.domain.model.mapper.toDomain
import network.ermis.genstreamui.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * UseCase mở một phiên chơi (POST /sessions) để backend provision host stream.
 * Không cache — phát [UiState.Loading] rồi [UiState.Success] với [Session] vừa tạo
 * (thường ở trạng thái "provisioning", ip/port host sẽ có ở các bước poll sau).
 */
class StartSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(subscriptionId: Int) = flow<UiState<Session>> {
        emit(UiState.Loading)
        try {
            when (val response = sessionRepository.startSession(subscriptionId)) {
                is ResultWrapper.Success -> {
                    val data = response.value.data
                    if (data != null) {
                        emit(UiState.Success(data.toDomain()))
                    } else {
                        emit(UiState.Error(response.value.message ?: "Không mở được phiên chơi"))
                    }
                }
                is ResultWrapper.GenericError -> emit(
                    UiState.Error(
                        message = response.message ?: "Không mở được phiên chơi",
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
