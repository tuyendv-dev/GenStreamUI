package network.ermis.genstreamui.presentation.home.discovery

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseViewModel
import network.ermis.genstreamui.domain.model.Discovery
import network.ermis.genstreamui.domain.usecase.game.GetDiscoveryUseCase
import javax.inject.Inject

/**
 * ViewModel màn Discovery: gọi /games/discovery và phát trạng thái qua [Channel] (không replay).
 * Pattern port từ các ViewModel auth.
 */
@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val getDiscoveryUseCase: GetDiscoveryUseCase
) : BaseViewModel() {

    private val _events = Channel<UiState<Discovery>>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun loadDiscovery() = launchIO {
        getDiscoveryUseCase().collect { _events.send(it) }
    }
}
