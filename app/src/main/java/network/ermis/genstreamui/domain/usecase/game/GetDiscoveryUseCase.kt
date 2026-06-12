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
 * UseCase lấy dữ liệu discovery (/games/discovery) theo mô hình stale-while-revalidate:
 * phát cache cũ ngay (nếu có) để UI hiển thị tức thì, rồi gọi API, ghi lại cache và phát data mới.
 * Khi đã có cache thì lỗi mạng được nuốt êm (giữ data cũ); chỉ báo lỗi khi chưa có cache.
 */
class GetDiscoveryUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    operator fun invoke() = flow<UiState<Discovery>> {
        val cached = gameRepository.cachedDiscovery()
        if (cached != null) emit(UiState.Success(cached)) else emit(UiState.Loading)
        try {
            when (val response = gameRepository.getDiscovery()) {
                is ResultWrapper.Success -> {
                    val data = response.value.data
                    if (data != null) {
                        val discovery = data.toDomain()
                        gameRepository.saveDiscovery(discovery)
                        emit(UiState.Success(discovery))
                    } else if (cached == null) {
                        emit(UiState.Error("Dữ liệu discovery trống"))
                    }
                }
                is ResultWrapper.GenericError -> if (cached == null) emit(
                    UiState.Error(
                        message = response.message ?: "Không lấy được discovery",
                        code = response.code?.toString().orEmpty()
                    )
                )
                ResultWrapper.NetworkError ->
                    if (cached == null) emit(UiState.Error("Không có kết nối mạng"))
            }
        } catch (e: Exception) {
            if (cached == null) emit(UiState.Error(e.message ?: "Đã có lỗi xảy ra"))
        }
    }.flowOn(Dispatchers.IO)
}
