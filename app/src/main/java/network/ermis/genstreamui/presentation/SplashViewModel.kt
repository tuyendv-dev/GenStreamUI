package network.ermis.genstreamui.presentation

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseViewModel
import network.ermis.genstreamui.database.cache.SharedPrefCommon
import network.ermis.genstreamui.database.cache.clearSession
import network.ermis.genstreamui.database.cache.saveUser
import network.ermis.genstreamui.domain.usecase.user.GetUserInfoUseCase
import javax.inject.Inject

/**
 * ViewModel màn Splash: quyết định điều hướng khi mở app.
 *
 * - Chưa có access token -> [Destination.Login].
 * - Có token -> xác thực bằng /users/me. Nếu access token hết hạn (401), [TokenAuthenticator]
 *   sẽ tự gọi /auth/refresh và retry: refresh OK -> Success -> [Destination.Main];
 *   refresh hỏng -> phiên bị xoá, trả Error -> [Destination.Login].
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase
) : BaseViewModel() {

    sealed interface Destination {
        data object Main : Destination
        data object Login : Destination
    }

    private val _destination = Channel<Destination>(Channel.BUFFERED)
    val destination = _destination.receiveAsFlow()

    fun checkSession() = launchIO {
        if (SharedPrefCommon.accessToken.isEmpty()) {
            _destination.send(Destination.Login)
            return@launchIO
        }

        getUserInfoUseCase().collect { state ->
            when (state) {
                is UiState.Success -> {
                    SharedPrefCommon.saveUser(state.data)
                    _destination.send(Destination.Main)
                }
                is UiState.Error -> {
                    // Token không hợp lệ và refresh cũng thất bại -> xoá phiên, về Login.
                    SharedPrefCommon.clearSession()
                    _destination.send(Destination.Login)
                }
                UiState.Loading, UiState.Idle -> Unit
            }
        }
    }
}
