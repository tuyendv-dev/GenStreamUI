package network.ermis.genstreamui.presentation

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseViewModel
import network.ermis.genstreamui.domain.model.Game
import network.ermis.genstreamui.domain.usecase.game.GetGameDetailUseCase
import javax.inject.Inject

/**
 * ViewModel màn PlayGame: lấy chi tiết game theo id (/games/{id}) và phát trạng thái qua
 * [Channel] (không replay). Cache trong Database được phát trước, sau đó là data mới từ API.
 * Pattern port từ [network.ermis.genstreamui.presentation.home.discovery.DiscoveryViewModel].
 */
@HiltViewModel
class PlayGameViewModel @Inject constructor(
    private val getGameDetailUseCase: GetGameDetailUseCase
) : BaseViewModel() {

    private val _events = Channel<UiState<Game>>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun loadGameDetail(id: Int) = launchIO {
        getGameDetailUseCase(id).collect { _events.send(it) }
    }
}
