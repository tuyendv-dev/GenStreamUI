package network.ermis.genstreamui.domain.usecase.game

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.Discovery
import network.ermis.genstreamui.domain.model.mapper.toDomain
import network.ermis.genstreamui.domain.repository.GameRepository
import javax.inject.Inject

/**
 * UseCase lấy dữ liệu discovery (/games/discovery): map ResultWrapper -> UiState, phát Loading trước.
 * Pattern Flow của UiState port từ các UseCase auth/user.
 */
class GetDiscoveryUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    operator fun invoke() = flow<UiState<Discovery>> {
        emit(UiState.Loading)
        try {
            when (val response = gameRepository.getDiscovery()) {
                is ResultWrapper.Success -> {
                    val data = response.value.data
                    if (data != null) emit(UiState.Success(data.toDomain()))
                    else emit(UiState.Error("Dữ liệu discovery trống"))
                }
                is ResultWrapper.GenericError -> emit(
                    UiState.Error(
                        message = response.message ?: "Không lấy được discovery",
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
