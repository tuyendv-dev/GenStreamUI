package network.ermis.genstreamui.presentation.home.find

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseViewModel
import network.ermis.genstreamui.domain.model.Discovery
import network.ermis.genstreamui.domain.usecase.game.FindGameStoreUseCase
import javax.inject.Inject

/**
 * ViewModel màn Find game: gọi /games/browse?store= và phát trạng thái qua [Channel] (không replay).
 * Pattern port từ [network.ermis.genstreamui.presentation.home.discovery.DiscoveryViewModel].
 */
@HiltViewModel
class FindGameViewModel @Inject constructor(
    private val findGameStoreUseCase: FindGameStoreUseCase
) : BaseViewModel() {

    private val _events = Channel<UiState<Discovery>>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun loadStore(store: String?) = launchIO {
        findGameStoreUseCase(store).collect { _events.send(it) }
    }
}
