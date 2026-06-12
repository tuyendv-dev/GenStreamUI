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
 * UseCase duyệt game theo store (/games/browse?store=) theo mô hình stale-while-revalidate:
 * phát cache cũ theo store ngay (nếu có), rồi gọi API, ghi lại cache và phát data mới.
 * Cùng schema [Discovery] với discovery nên tái dùng mapper. Pattern port từ [GetDiscoveryUseCase].
 */
class FindGameStoreUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    operator fun invoke(store: String?) = flow<UiState<Discovery>> {
        val cached = gameRepository.cachedStore(store)
        if (cached != null) emit(UiState.Success(cached)) else emit(UiState.Loading)
        try {
            when (val response = gameRepository.findGameStore(store)) {
                is ResultWrapper.Success -> {
                    val data = response.value.data
                    if (data != null) {
                        val discovery = data.toDomain()
                        gameRepository.saveStore(store, discovery)
                        emit(UiState.Success(discovery))
                    } else if (cached == null) {
                        emit(UiState.Error("Dữ liệu store trống"))
                    }
                }
                is ResultWrapper.GenericError -> if (cached == null) emit(
                    UiState.Error(
                        message = response.message ?: "Không lấy được danh sách game",
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
