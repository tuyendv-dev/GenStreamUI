package network.ermis.genstreamui.domain.usecase.subscription

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.Session
import network.ermis.genstreamui.domain.model.mapper.toDomain
import network.ermis.genstreamui.domain.repository.SubscriptionRepository
import javax.inject.Inject

/**
 * UseCase lấy phiên chơi đang active của một gói — GET /users/me/subscriptions/{id}/active-session.
 *
 * Dùng để "tiếp tục" một phiên đang chạy thay vì tạo mới (vd khi POST /sessions trả 409
 * SESSION_INVALID_STATE "subscription already has an active session").
 *
 * Backend trả `data = null` khi gói không có phiên active → ở ngữ cảnh này coi là [UiState.Error].
 */
class GetActiveSessionBySubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke(subscriptionId: Int) = flow<UiState<Session>> {
        emit(UiState.Loading)
        try {
            when (val response = subscriptionRepository.getSessionActiveBySubscription(subscriptionId)) {
                is ResultWrapper.Success -> {
                    val data = response.value.data
                    if (data != null) {
                        emit(UiState.Success(data.toDomain()))
                    } else {
                        emit(UiState.Error("Không tìm thấy phiên đang hoạt động"))
                    }
                }
                is ResultWrapper.GenericError -> emit(
                    UiState.Error(
                        message = response.message ?: "Không lấy được phiên đang hoạt động",
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
