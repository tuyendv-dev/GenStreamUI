package network.ermis.genstreamui.presentation.auth.reset

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseViewModel
import network.ermis.genstreamui.domain.model.dto.req.ReqResetPassword
import network.ermis.genstreamui.domain.model.dto.res.ResResetPassword
import network.ermis.genstreamui.domain.usecase.auth.ResetPasswordUseCase
import javax.inject.Inject

/**
 * ViewModel màn đặt lại mật khẩu — gửi mã + email + mật khẩu mới.
 * Kết quả phát qua [Channel] (không replay) để không lặp toast/điều hướng khi quay lại màn.
 */
@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : BaseViewModel() {

    private val _events = Channel<UiState<ResResetPassword>>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun resetPassword(email: String, code: String, password: String) = launchIO {
        val req = ReqResetPassword(code = code, email = email, password = password)
        resetPasswordUseCase(req).collect { _events.send(it) }
    }
}
