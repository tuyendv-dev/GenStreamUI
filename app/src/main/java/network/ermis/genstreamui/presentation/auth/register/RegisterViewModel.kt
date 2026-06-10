package network.ermis.genstreamui.presentation.auth.register

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseViewModel
import network.ermis.genstreamui.domain.model.dto.req.ReqRegisterAccount
import network.ermis.genstreamui.domain.model.dto.res.ResRegisterAccount
import network.ermis.genstreamui.domain.usecase.auth.RegisterUseCase
import javax.inject.Inject

/**
 * ViewModel màn đăng ký. Parallel với [network.ermis.genstreamui.presentation.auth.login.LoginViewModel].
 *
 * Kết quả phát qua [Channel] (không replay) thay vì StateFlow, nên khi Fragment quay lại từ back stack
 * sự kiện cũ không bị phát lại — tránh toast/điều hướng lặp.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : BaseViewModel() {

    private val _events = Channel<UiState<ResRegisterAccount>>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun register(email: String, displayName: String, password: String) = launchIO {
        val req = ReqRegisterAccount(
            email = email,
            password = password,
            displayName = displayName
        )
        registerUseCase(req).collect { _events.send(it) }
    }
}
