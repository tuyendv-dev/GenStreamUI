package network.ermis.genstreamui.presentation.profile

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseViewModel
import network.ermis.genstreamui.domain.model.User
import network.ermis.genstreamui.domain.model.dto.req.ReqChangePassword
import network.ermis.genstreamui.domain.model.dto.req.ReqUpdateUserInfo
import network.ermis.genstreamui.domain.model.dto.res.ResChangePassword
import network.ermis.genstreamui.domain.usecase.user.ChangePasswordUseCase
import network.ermis.genstreamui.domain.usecase.user.GetUserInfoUseCase
import network.ermis.genstreamui.domain.usecase.user.UpdateUserInfoUseCase
import javax.inject.Inject

/**
 * ViewModel màn hồ sơ người dùng — lấy/cập nhật thông tin và đổi mật khẩu.
 * Kết quả phát qua [Channel] (không replay) để sự kiện không bị lặp khi quay lại màn.
 */
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val updateUserInfoUseCase: UpdateUserInfoUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase
) : BaseViewModel() {

    private val _userInfoEvents = Channel<UiState<User>>(Channel.BUFFERED)
    val userInfoEvents = _userInfoEvents.receiveAsFlow()

    private val _updateEvents = Channel<UiState<User>>(Channel.BUFFERED)
    val updateEvents = _updateEvents.receiveAsFlow()

    private val _changePasswordEvents = Channel<UiState<ResChangePassword>>(Channel.BUFFERED)
    val changePasswordEvents = _changePasswordEvents.receiveAsFlow()

    fun getUserInfo() = launchIO {
        getUserInfoUseCase().collect { _userInfoEvents.send(it) }
    }

    fun updateUserInfo(req: ReqUpdateUserInfo) = launchIO {
        updateUserInfoUseCase(req).collect { _updateEvents.send(it) }
    }

    fun changePassword(req: ReqChangePassword) = launchIO {
        changePasswordUseCase(req).collect { _changePasswordEvents.send(it) }
    }
}
