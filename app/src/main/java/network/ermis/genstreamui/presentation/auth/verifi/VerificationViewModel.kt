package network.ermis.genstreamui.presentation.auth.verifi

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseViewModel
import network.ermis.genstreamui.domain.model.dto.req.ReqVerificationCode
import network.ermis.genstreamui.domain.model.dto.res.ResResendOtp
import network.ermis.genstreamui.domain.model.dto.res.ResVerificationCode
import network.ermis.genstreamui.domain.usecase.auth.ResendOtpUseCase
import network.ermis.genstreamui.domain.usecase.auth.VerificationCodeUseCase
import javax.inject.Inject

/**
 * ViewModel xác minh mã: verify email (user_id + otp) và gửi lại mã (resend-otp theo email).
 * Mỗi hành động phát qua [Channel] riêng (không replay) để sự kiện không bị phát lại khi quay lại màn.
 */
@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val verificationCodeUseCase: VerificationCodeUseCase,
    private val resendOtpUseCase: ResendOtpUseCase
) : BaseViewModel() {

    private val _events = Channel<UiState<ResVerificationCode>>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _resendEvents = Channel<UiState<ResResendOtp>>(Channel.BUFFERED)
    val resendEvents = _resendEvents.receiveAsFlow()

    fun verify(userId: Int, otp: String) = launchIO {
        val req = ReqVerificationCode(userId = userId, otp = otp)
        verificationCodeUseCase(req).collect { _events.send(it) }
    }

    fun resendOtp(email: String) = launchIO {
        resendOtpUseCase(email).collect { _resendEvents.send(it) }
    }
}
