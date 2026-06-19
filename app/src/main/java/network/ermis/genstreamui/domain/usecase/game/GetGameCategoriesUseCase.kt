package network.ermis.genstreamui.domain.usecase.game

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.repository.GameRepository
import javax.inject.Inject

/**
 * UseCase lấy danh sách category game (GET /games/categories) — dùng cho bộ lọc màn search.
 */
class GetGameCategoriesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    operator fun invoke() = flow<UiState<List<String>>> {
        emit(UiState.Loading)
        try {
            when (val response = gameRepository.getGameCategories()) {
                is ResultWrapper.Success ->
                    emit(UiState.Success(response.value.data ?: emptyList()))
                is ResultWrapper.GenericError -> emit(
                    UiState.Error(
                        message = response.message ?: "Không lấy được danh mục",
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
