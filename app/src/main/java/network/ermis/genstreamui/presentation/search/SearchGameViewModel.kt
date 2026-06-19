package network.ermis.genstreamui.presentation.search

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseViewModel
import network.ermis.genstreamui.domain.model.GameSearchResult
import network.ermis.genstreamui.domain.usecase.game.GetGameCategoriesUseCase
import network.ermis.genstreamui.domain.usecase.game.SearchGamesUseCase
import javax.inject.Inject

/**
 * ViewModel màn Search: gọi /games (search/filter, có phân trang) và /games/categories,
 * phát trạng thái qua [Channel] (không replay). ViewModel tự giữ trạng thái phân trang
 * (query/category/trang hiện tại) nên Activity chỉ cần gọi [search] và [loadNextPage].
 */
@HiltViewModel
class SearchGameViewModel @Inject constructor(
    private val searchGamesUseCase: SearchGamesUseCase,
    private val getGameCategoriesUseCase: GetGameCategoriesUseCase
) : BaseViewModel() {

    private val _searchEvents = Channel<UiState<GameSearchResult>>(Channel.BUFFERED)
    val searchEvents = _searchEvents.receiveAsFlow()

    private val _categoryEvents = Channel<UiState<List<String>>>(Channel.BUFFERED)
    val categoryEvents = _categoryEvents.receiveAsFlow()

    // Trạng thái phân trang của lần search hiện tại.
    private var currentQuery: String? = null
    private var currentCategory: String? = null
    private var currentPage = 1
    private var totalPages = 1
    private var isLoadingPage = false

    // Job của request đang chạy (search trang 1 hoặc load trang kế) — huỷ khi bắt đầu lượt search mới
    // để response trang cũ không về sau và append nhầm vào lưới mới.
    private var searchJob: Job? = null

    /** Bắt đầu một lượt search mới (reset về trang 1, huỷ request cũ còn dở). */
    fun search(query: String?, category: String?) {
        searchJob?.cancel()
        currentQuery = query
        currentCategory = category
        currentPage = 1
        totalPages = 1
        isLoadingPage = true
        searchJob = launchIO(onCompleted = { isLoadingPage = false }) {
            searchGamesUseCase(query = query, category = category, page = 1).collect { ui ->
                if (ui is UiState.Success) updatePaging(ui.data)
                _searchEvents.send(ui)
            }
        }
    }

    /** Load trang kế tiếp (nếu còn và không có request đang chạy). Kết quả phát kèm [GameSearchResult.page]. */
    fun loadNextPage() {
        if (isLoadingPage || currentPage >= totalPages) return
        val nextPage = currentPage + 1
        isLoadingPage = true
        searchJob = launchIO(onCompleted = { isLoadingPage = false }) {
            searchGamesUseCase(query = currentQuery, category = currentCategory, page = nextPage)
                .collect { ui ->
                    if (ui is UiState.Success) updatePaging(ui.data)
                    _searchEvents.send(ui)
                }
        }
    }

    private fun updatePaging(result: GameSearchResult) {
        currentPage = result.page
        totalPages = result.totalPages
    }

    fun loadCategories() = launchIO {
        getGameCategoriesUseCase().collect { _categoryEvents.send(it) }
    }
}
