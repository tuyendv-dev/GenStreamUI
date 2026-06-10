package network.ermis.genstreamui.domain.usecase.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.req.ReqResetPassword
import network.ermis.genstreamui.domain.model.dto.res.ResResetPassword
import network.ermis.genstreamui.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase đặt lại mật khẩu (mã + email + mật khẩu mới). Port theo GenPlayAndroid.
 */
class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(req: ReqResetPassword) = flow<UiState<ResResetPassword>> {
        emit(UiState.Loading)
        try {
            when (val response = authRepository.resetPassword(req)) {
                is ResultWrapper.Success -> emit(UiState.Success(response.value))
                is ResultWrapper.GenericError -> emit(
                    UiState.Error(
                        message = response.message ?: "Đặt lại mật khẩu thất bại",
                        code = response.code?.toString().orEmpty()
                    )
                )
                ResultWrapper.NetworkError -> emit(UiState.Error("Không có kết nối mạng"))
            }
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Đã có lỗi xảy ra"))
        }
    }.flowOn(Dispatchers.IO)
}
