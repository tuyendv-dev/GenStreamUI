package network.ermis.genstreamui.domain.usecase.session

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.Session
import network.ermis.genstreamui.domain.model.mapper.toDomain
import network.ermis.genstreamui.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * UseCase giữ nhịp phiên (keep-alive) — POST /sessions/{id}/heartbeat.
 *
 * Suspend one-shot (chỉ trả [UiState.Success]/[UiState.Error], không có Loading) để gọi định kỳ
 * trong lúc đang stream. Trả về snapshot [Session] mới nhất nên caller có thể tự dừng khi phiên
 * đã kết thúc:
 * ```
 * while (isActive) {
 *     when (val r = heartbeat(sessionId)) {
 *         is UiState.Success -> if (r.data.sessionStatus == SessionStatus.STOPPED) break
 *         is UiState.Error   -> { /* log/retry */ }
 *         else -> Unit
 *     }
 *     delay(HEARTBEAT_INTERVAL_MS)
 * }
 * ```
 */
class HeartbeatUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Int): UiState<Session> = withContext(Dispatchers.IO) {
        try {
            when (val response = sessionRepository.heartbeat(sessionId)) {
                is ResultWrapper.Success -> {
                    val data = response.value.data
                    if (data != null) {
                        UiState.Success(data.toDomain())
                    } else {
                        UiState.Error(response.value.message ?: "Heartbeat thất bại")
                    }
                }
                is ResultWrapper.GenericError -> UiState.Error(
                    message = response.message ?: "Heartbeat thất bại",
                    code = response.code?.toString().orEmpty()
                )
                ResultWrapper.NetworkError -> UiState.Error("Không có kết nối mạng")
            }
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Đã có lỗi xảy ra")
        }
    }
}
