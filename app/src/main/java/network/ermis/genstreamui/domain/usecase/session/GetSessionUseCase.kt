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
 * UseCase probe trạng thái phiên — GET /sessions/{id} (genstream-custom-auth.md §9).
 * Không side-effect, không tốn connection token; dùng để kiểm tra phiên còn `running`
 * (vd khôi phục sau crash) trước khi quyết định reconnect hay tạo phiên mới.
 */
class GetSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(sessionId: Int) = flow<UiState<Session>> {
        emit(UiState.Loading)
        try {
            when (val response = sessionRepository.getSession(sessionId)) {
                is ResultWrapper.Success -> {
                    val data = response.value.data
                    if (data != null) {
                        emit(UiState.Success(data.toDomain()))
                    } else {
                        emit(UiState.Error(response.value.message ?: "Không lấy được phiên"))
                    }
                }
                is ResultWrapper.GenericError -> emit(
                    UiState.Error(
                        message = response.message ?: "Không lấy được phiên",
                        code = response.error ?: response.code?.toString().orEmpty()
                    )
                )
                ResultWrapper.NetworkError -> emit(UiState.Error("Không có kết nối mạng"))
            }
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Đã có lỗi xảy ra"))
        }
    }.flowOn(Dispatchers.IO)
}
