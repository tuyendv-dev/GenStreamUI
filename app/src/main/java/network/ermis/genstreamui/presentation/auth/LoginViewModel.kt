package network.ermis.genstreamui.presentation.auth

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import network.ermis.genstreamui.common.base.BaseViewModel
import network.ermis.genstreamui.domain.usecase.auth.LoginUseCase
import javax.inject.Inject

/**
 * ViewModel màn Login: gọi LoginUseCase, đẩy UiState ra StateFlow cho Fragment quan sát.
 * Pattern MVVM + StateFlow port từ GenPlayAndroid (các ViewModel trong presentation).
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun login(email: String, password: String) = launchIO {
        loginUseCase(email, password).collect { uiState ->
            _state.value = _state.value.copy(uiState = uiState)
        }
    }
}
