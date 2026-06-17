package network.ermis.genstreamui.domain.usecase.subscription

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.Subscription
import network.ermis.genstreamui.domain.model.mapper.toDomain
import network.ermis.genstreamui.domain.repository.SubscriptionRepository
import javax.inject.Inject

/**
 * UseCase lấy danh sách gói thuê bao của user (GET /users/me/subscriptions).
 * Phát [UiState.Loading] rồi [UiState.Success] với danh sách [Subscription] (có thể rỗng).
 */
class GetUserSubscriptionsUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke() = flow<UiState<List<Subscription>>> {
        emit(UiState.Loading)
        try {
            when (val response = subscriptionRepository.getUserSubscriptions()) {
                is ResultWrapper.Success -> {
                    val list = response.value.data.orEmpty().map { it.toDomain() }
                    emit(UiState.Success(list))
                }
                is ResultWrapper.GenericError -> emit(
                    UiState.Error(
                        message = response.message ?: "Không lấy được danh sách gói",
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
