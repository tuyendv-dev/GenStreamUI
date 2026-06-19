package network.ermis.genstreamui.domain.usecase.game

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.GameSearchResult
import network.ermis.genstreamui.domain.model.mapper.toDomain
import network.ermis.genstreamui.domain.repository.GameRepository
import javax.inject.Inject

/**
 * UseCase tìm/lọc game (GET /games). Phát [UiState.Loading] rồi gọi API và phát kết quả.
 * Query rỗng được quy về null để không gắn ?q= thừa (trả về toàn bộ game theo filter còn lại).
 */
class SearchGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    operator fun invoke(
        query: String? = null,
        category: String? = null,
        platform: String? = null,
        page: Int = 1,
        perPage: Int = DEFAULT_PER_PAGE
    ) = flow<UiState<GameSearchResult>> {
        emit(UiState.Loading)
        try {
            val response = gameRepository.searchGames(
                q = query?.trim()?.takeIf { it.isNotEmpty() },
                category = category?.takeIf { it.isNotEmpty() },
                platform = platform?.takeIf { it.isNotEmpty() },
                page = page,
                perPage = perPage
            )
            when (response) {
                is ResultWrapper.Success -> emit(UiState.Success(response.value.toDomain()))
                is ResultWrapper.GenericError -> emit(
                    UiState.Error(
                        message = response.message ?: "Không tìm được game",
                        code = response.code?.toString().orEmpty()
                    )
                )
                ResultWrapper.NetworkError -> emit(UiState.Error("Không có kết nối mạng"))
            }
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Đã có lỗi xảy ra"))
        }
    }.flowOn(Dispatchers.IO)

    private companion object {
        const val DEFAULT_PER_PAGE = 40
    }
}
