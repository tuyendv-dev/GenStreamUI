package network.ermis.genstreamui.presentation.auth.forget

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseViewModel
import network.ermis.genstreamui.domain.model.dto.req.ReqForgetPasswordDTO
import network.ermis.genstreamui.domain.model.dto.res.ResForgetPassword
import network.ermis.genstreamui.domain.usecase.auth.ForgetPasswordUseCase
import javax.inject.Inject

/**
 * ViewModel màn quên mật khẩu — gửi email yêu cầu mã đặt lại.
 * Kết quả phát qua [Channel] (không replay) để không lặp toast/điều hướng khi quay lại màn.
 */
@HiltViewModel
class ForgetPasswordViewModel @Inject constructor(
    private val forgetPasswordUseCase: ForgetPasswordUseCase
) : BaseViewModel() {

    private val _events = Channel<UiState<ResForgetPassword>>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun forgetPassword(email: String) = launchIO {
        forgetPasswordUseCase(ReqForgetPasswordDTO(email = email)).collect { _events.send(it) }
    }
}
