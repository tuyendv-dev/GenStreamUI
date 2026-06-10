package network.ermis.genstreamui.presentation.auth.login

import android.content.Context
import android.content.Intent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseViewModel
import network.ermis.genstreamui.domain.model.dto.req.ReqLoginGgDTO
import network.ermis.genstreamui.domain.model.dto.res.ResGoogleLoginDTO
import network.ermis.genstreamui.domain.model.dto.res.ResLoginDTO
import network.ermis.genstreamui.domain.usecase.auth.LoginByGoogleUseCase
import network.ermis.genstreamui.domain.usecase.auth.LoginUseCase
import network.ermis.genstreamui.domain.usecase.auth.LoginWithGoogleUseCase
import javax.inject.Inject

/**
 * ViewModel màn Login: đăng nhập email/mật khẩu và đăng nhập Google (mỗi cái 1 Channel sự kiện).
 * Quên mật khẩu tách sang [network.ermis.genstreamui.presentation.auth.forget.ForgetPasswordViewModel]. Pattern port từ GenPlayAndroid.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val loginByGoogleUseCase: LoginByGoogleUseCase
) : BaseViewModel() {

    private val _events = Channel<UiState<ResLoginDTO>>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _googleEvents = Channel<UiState<ResGoogleLoginDTO>>(Channel.BUFFERED)
    val googleEvents = _googleEvents.receiveAsFlow()

    fun login(email: String, password: String) = launchIO {
        loginUseCase(email, password).collect { _events.send(it) }
    }

    /** Intent để mở màn chọn tài khoản Google (dùng với ActivityResultLauncher). */
    fun googleSignInIntent(context: Context): Intent = loginByGoogleUseCase.signInIntent(context)

    /** Đổi authorization code (OAuth + PKCE) lấy phiên đăng nhập từ backend. */
    fun loginWithGoogle(code: String, codeVerifier: String, redirectUri: String) = launchIO {
        val req = ReqLoginGgDTO(
            code = code,
            codeVerifier = codeVerifier,
            redirectUri = redirectUri
        )
        loginWithGoogleUseCase(req).collect { _googleEvents.send(it) }
    }
}
