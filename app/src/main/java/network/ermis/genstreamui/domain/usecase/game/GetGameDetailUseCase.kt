package network.ermis.genstreamui.domain.usecase.game

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.Game
import network.ermis.genstreamui.domain.model.mapper.toDomain
import network.ermis.genstreamui.domain.repository.GameRepository
import javax.inject.Inject

/**
 * UseCase lấy chi tiết một game (/games/{id}) theo mô hình stale-while-revalidate:
 * phát game đã cache trong Database ngay (nếu có) để UI hiển thị tức thì, rồi gọi API,
 * ghi đè lại game đó trong Database và phát data mới.
 * Khi đã có cache thì lỗi mạng được nuốt êm (giữ data cũ); chỉ báo lỗi khi chưa có cache.
 */
class GetGameDetailUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    operator fun invoke(id: Int) = flow<UiState<Game>> {
        val cached = gameRepository.getGameById(id)
        if (cached != null) emit(UiState.Success(cached)) else emit(UiState.Loading)
        try {
            when (val response = gameRepository.getGameDetail(id)) {
                is ResultWrapper.Success -> {
                    val data = response.value.data
                    if (data != null) {
                        val game = data.toDomain()
                        gameRepository.saveGame(game)
                        emit(UiState.Success(game))
                    } else if (cached == null) {
                        emit(UiState.Error("Dữ liệu game trống"))
                    }
                }
                is ResultWrapper.GenericError -> if (cached == null) emit(
                    UiState.Error(
                        message = response.message ?: "Không lấy được chi tiết game",
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
