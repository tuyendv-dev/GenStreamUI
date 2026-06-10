package network.ermis.genstreamui.domain.usecase.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.req.ReqResendOtpDTO
import network.ermis.genstreamui.domain.model.dto.res.ResResendOtp
import network.ermis.genstreamui.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase gửi lại mã OTP theo email. Port theo GenPlayAndroid.
 */
class ResendOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(email: String) = flow<UiState<ResResendOtp>> {
        emit(UiState.Loading)
        try {
            when (val response = authRepository.resendOtp(ReqResendOtpDTO(email = email))) {
                is ResultWrapper.Success -> emit(UiState.Success(response.value))
                is ResultWrapper.GenericError -> emit(
                    UiState.Error(
                        message = response.message ?: "Gửi lại mã thất bại",
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
